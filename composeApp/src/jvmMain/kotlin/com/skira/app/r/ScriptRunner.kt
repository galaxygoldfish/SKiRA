package com.skira.app.r

import com.skira.app.structures.AssayMetadata
import com.skira.app.utilities.PreferenceManager
import com.skira.app.structures.PreferenceKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedWriter
import java.io.OutputStreamWriter


// Add a result carrier for both plots
data class PlotOutputs(
    val featureBytes: ByteArray,
    val dimBytes: ByteArray? = null
)


/**
 * Runs metadata.R from resources and returns parsed AssayMeta.
 * The script must print a compact JSON object with "genes" and "timepoints" arrays on stdout.
 */
suspend fun getMetadata(): Result<AssayMetadata> = withContext(Dispatchers.IO) {
    val script = extractResourceToTemp("metadata.R", "meta", ".R")
    try {
        val (cmd, baseArgs) = resolveRInvoker()
            ?: return@withContext Result.failure(IllegalStateException("R/Rscript not found on PATH or RSCRIPT env"))

        val folderRObjPath = PreferenceManager.getString(PreferenceKey.R_DATASET_FOLDER)!!

        // Ensure the folder path is passed as a trailing script argument for both invocations.
        // For Rscript: `Rscript script.R <top.dir>`
        // For R (with -f): `R ... -f script.R --args <top.dir>`
        val args = if (baseArgs.contains("--use-rscript")) {
            listOf(cmd, script.absolutePath) + listOfNotNull(folderRObjPath)
        } else {
            listOf(cmd) + baseArgs + listOf("-f", script.absolutePath, "--args") + listOfNotNull(folderRObjPath)
        }
        val pb = ProcessBuilder(args).redirectErrorStream(true)
        val proc = pb.start()

        fun InputStream.readAllText(): String =
            this.bufferedReader(Charsets.UTF_8).use { it.readText() }

        val stdout = proc.inputStream.readAllText()

        val finished = proc.waitFor(60, TimeUnit.SECONDS)
        if (!finished) {
            proc.destroyForcibly()
            return@withContext Result.failure(IllegalStateException("metadata.R timed out"))
        }
        if (proc.exitValue() != 0) {
            return@withContext Result.failure(IllegalStateException("metadata.R exit ${proc.exitValue()}: $stdout"))
        }

        // Parse JSON
        val json = Json { ignoreUnknownKeys = true }
        val root = runCatching { json.parseToJsonElement(stdout) }.getOrElse {
            return@withContext Result.failure(IllegalStateException("Failed to parse metadata JSON: ${it.message}"))
        }.jsonObject

        val genes = root["genes"]?.jsonArray?.map { it.jsonPrimitive.contentOrNull ?: it.toString() } ?: emptyList()
        val timepoints = root["timepoints"]?.jsonArray?.map { it.jsonPrimitive.contentOrNull ?: it.toString() } ?: emptyList()

        Result.success(AssayMetadata(genes, timepoints))
    } catch (t: Throwable) {
        Result.failure(t)
    } finally {
        runCatching { script.delete() }
    }
}

/**
 * Persistent plot worker integration.
 * Uses plot_worker.R which reads JSON lines from stdin and prints:
 * - PROGRESS: n
 * - IMAGE_BASE64: <base64>
 * - DONE
 * - Or ERROR: <message>
 */
object PlotWorker {
    private val mutex = Mutex()
    private var process: Process? = null
    private var stdin: BufferedWriter? = null
    private var stdoutReader: java.io.BufferedReader? = null
    private var stderrReader: java.io.BufferedReader? = null
    private var scriptFile: File? = null

    private suspend fun ensureStarted(): Result<Unit> = withContext(Dispatchers.IO) {
        if (process?.isAlive == true && stdin != null && stdoutReader != null) {
            return@withContext Result.success(Unit)
        }


        runCatching { process?.destroyForcibly() }
        runCatching { scriptFile?.delete() }

        val script = extractResourceToTemp("plot_worker.R", "plot_worker", ".R")
        scriptFile = script

        val folderRObjPath = PreferenceManager.getString(PreferenceKey.R_DATASET_FOLDER)!!

        val (cmd, baseArgs) = resolveRInvoker()
            ?: return@withContext Result.failure(IllegalStateException("R/Rscript not found on PATH or RSCRIPT env"))

        val args = if (baseArgs.contains("--use-rscript")) {
            listOf(cmd, script.absolutePath) + listOfNotNull(folderRObjPath)
        } else {
            listOf(cmd) + baseArgs + listOf("-f", script.absolutePath, "--args") + listOfNotNull(folderRObjPath)
        }

        val proc = ProcessBuilder(args)
            .redirectErrorStream(false) // worker writes progress to stdout (and maybe stderr); we read both
            .start()

        process = proc
        stdin = BufferedWriter(OutputStreamWriter(proc.outputStream, Charsets.UTF_8))
        stdoutReader = proc.inputStream.bufferedReader(Charsets.UTF_8)
        stderrReader = proc.errorStream.bufferedReader(Charsets.UTF_8)

        // Optional: ping the worker to ensure it's responsive
//        val pingResult = runCatching {
//            stdin!!.apply { write("PING\n"); flush() }
//            val deadline = System.nanoTime() + 120_000_000_000L
//            var line: String?
//            do {
//                line = stdoutReader!!.readLine()
//                if (line == null) break
//                if (line.trim() == "PONG") return@runCatching true
//            } while (System.nanoTime() < deadline)
//            false
//        }.getOrElse { false }
//        if (!pingResult) {
//            runCatching { proc.destroyForcibly() }
//            return@withContext Result.failure(IllegalStateException("plot_worker did not respond to PING"))
//        }


        // Drain stderr asynchronously (so buffer doesn't block). We ignore content except progress echoes.
        launch(Dispatchers.IO) {
            try {
                stderrReader?.useLines { /* drain */ _ -> }
            } catch (_: Throwable) {
            }
        }
        Result.success(Unit)
    }

    // Warm up the worker so heavy R code is loaded before plotting
    suspend fun warmUp(): Result<Unit> = mutex.withLock {
        ensureStarted()
    }

    /**
     * Compatibility shim: previously launched a one-shot plot.R.
     * Now we ONLY use the long-lived plot worker.
     * This delegates to requestPlot to avoid breaking existing call sites.
     */
    suspend fun runPlot(
        gene: String,
        timepoint: String,
        expressionDpi: Int,
        cellTypeDpi: Int,
        expressionPlotColor: String,
        dimPlotColorBy: Int,
        showDimLabels: Boolean,
        showExprLabels: Boolean,
        onProgress: (Int) -> Unit = {}
    ): Result<PlotOutputs> = mutex.withLock {
        withContext(Dispatchers.IO) {
            println(dimPlotColorBy)
            require(gene.isNotBlank() && timepoint.isNotBlank()) { "gene/timepoint must be set" }
            ensureStarted().onFailure { return@withContext Result.failure(it) }
            val req = """{"gene":"$gene","timepoint":"$timepoint","dpiExpr":$expressionDpi,"dpiCType":$cellTypeDpi,"colorExpr":"$expressionPlotColor","colorCType":$dimPlotColorBy,"labelsExpr":$showExprLabels,"labelsDim":$showDimLabels}"""
            val writer =
                stdin ?: return@withContext Result.failure(IllegalStateException("plot_worker stdin not available"))
            val reader = stdoutReader
                ?: return@withContext Result.failure(IllegalStateException("plot_worker stdout not available"))

            runCatching {
                writer.write(req); writer.write("\n"); writer.flush()
            }.onFailure { err ->
                return@withContext Result.failure(IllegalStateException("Failed to write to plot_worker: ${err.message}"))
            }

            var outFileFeature: String? = null
            var outFileDim: String? = null
            var errorMsg: String? = null

            val deadline = System.nanoTime() + 180_000_000_000L
            while (System.nanoTime() < deadline) {
                val line = runCatching { reader.readLine() }.getOrNull() ?: break
                val t = line.trim()
                when {
                    t.startsWith("PROGRESS:") ->
                        t.removePrefix("PROGRESS:").trim().toIntOrNull()?.let { onProgress(it.coerceIn(0, 100)) }

                    t.startsWith("OUTFILE2:") && outFileDim == null ->
                        outFileDim = t.removePrefix("OUTFILE2:").trim()

                    t.startsWith("OUTFILE:") && outFileFeature == null ->
                        outFileFeature = t.removePrefix("OUTFILE:").trim()

                    t.startsWith("ERROR:") ->
                        errorMsg = t.removePrefix("ERROR:").trim()

                    t == "DONE" -> break
                    else -> { /* ignore */ }
                }
            }

            if (!errorMsg.isNullOrBlank()) {
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }

            val featPath = outFileFeature?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(IllegalStateException("plot_worker returned no OUTFILE path"))

            val featureBytes = runCatching { Files.readAllBytes(Path.of(featPath)) }.getOrElse {
                return@withContext Result.failure(IllegalStateException("Failed to read OUTFILE image: ${it.message}"))
            }
            // Best-effort cleanup
            runCatching { Files.deleteIfExists(Path.of(featPath)) }

            val dimBytes: ByteArray? = outFileDim?.takeIf { it.isNotBlank() }?.let { p ->
                runCatching { Files.readAllBytes(Path.of(p)) }.onSuccess {
                    runCatching { Files.deleteIfExists(Path.of(p)) }
                }.getOrElse {
                    // Don’t fail whole request if second plot fails; just skip it
                    null
                }
            }

            if (featureBytes.size < 8) {
                return@withContext Result.failure(IllegalStateException("PNG device produced too few bytes (${featureBytes.size})."))
            }

            Result.success(PlotOutputs(featureBytes = featureBytes, dimBytes = dimBytes))
        }
    }
}