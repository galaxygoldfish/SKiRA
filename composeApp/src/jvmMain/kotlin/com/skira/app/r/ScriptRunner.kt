package com.skira.app.r

import com.skira.app.structures.AssayMetadata
import com.skira.app.utilities.PreferenceManager
import com.skira.app.structures.PreferenceKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedWriter
import java.io.OutputStreamWriter


data class PlotOutputs(
    val featureBytes: ByteArray? = null,
    val dimBytes: ByteArray? = null
)


/**
 * Persistent plot worker integration.
 * Uses platform wrapper scripts that source a shared worker core and print:
 * - PROGRESS: n
 * - IMAGE_BASE64: <base64>
 * - DONE
 * - Or ERROR: <message>
 */
object PlotWorker {
    private val mutex = Mutex()
    private val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var stderrDrainJob: Job? = null
    private var process: Process? = null
    private var stdin: BufferedWriter? = null
    private var stdoutReader: java.io.BufferedReader? = null
    private var stderrReader: java.io.BufferedReader? = null
    private var scriptFile: File? = null
    private var scriptDir: File? = null
    private var isInitialized = false

    private fun startStderrDrainer() {
        stderrDrainJob?.cancel()
        val reader = stderrReader ?: return
        stderrDrainJob = workerScope.launch {
            try {
                reader.useLines { lines -> lines.forEach { } }
            } catch (_: Throwable) {
            }
        }
    }

    suspend fun requestMetadata(onProgress: (Int) -> Unit): Result<AssayMetadata> = mutex.withLock {
        withContext(Dispatchers.IO) {
            // Check if we need to start the process
            if (process?.isAlive != true || stdin == null || stdoutReader == null || !isInitialized) {
                runCatching { process?.destroyForcibly() }
                runCatching { scriptFile?.delete() }
                runCatching { scriptDir?.deleteRecursively() }
                isInitialized = false

                // Select platform-specific plot worker script
                val isWindows = System.getProperty("os.name").lowercase().contains("win")
                val scriptResourceName = if (isWindows) "plot_worker_windows_wrapper.R" else "plot_worker_macos_wrapper.R"
                val tempDir = extractResourcesToTempDir(
                    resourceNames = listOf(scriptResourceName, "plot_worker_core.R"),
                    prefix = "plot_worker_"
                )
                val script = File(tempDir, scriptResourceName)
                scriptFile = script
                scriptDir = tempDir

                val folderRObjPath = PreferenceManager.getString(PreferenceKey.R_DATASET_FOLDER)!!

                val (cmd, baseArgs) = resolveRInvoker()
                    ?: return@withContext Result.failure(IllegalStateException("R/Rscript not found on PATH or RSCRIPT env"))

                val args = if (baseArgs.contains("--use-rscript")) {
                    listOf(cmd, "--vanilla", "--slave", script.absolutePath) + listOfNotNull(folderRObjPath)
                } else {
                    listOf(cmd) + baseArgs + listOf("--vanilla", "--slave", "-f", script.absolutePath, "--args") + listOfNotNull(folderRObjPath)
                }

                val proc = ProcessBuilder(args).redirectErrorStream(false).start()

                process = proc
                stdin = BufferedWriter(OutputStreamWriter(proc.outputStream, Charsets.UTF_8))
                stdoutReader = proc.inputStream.bufferedReader(Charsets.UTF_8)
                stderrReader = proc.errorStream.bufferedReader(Charsets.UTF_8)

                // Drain stderr in a detached worker scope (not as a child of this request).
                startStderrDrainer()

                val deadline = System.nanoTime() + 120_000_000_000L
                var ready = false

                while (System.nanoTime() < deadline) {
                    val line = stdoutReader?.readLine()
                    if (line == null) {
                        return@withContext Result.failure(IllegalStateException("R worker process ended"))
                    }
                    val trimmed = line.trim()

                    if (trimmed.startsWith("PROGRESS:")) {
                        val prog = trimmed.removePrefix("PROGRESS:").trim().toIntOrNull()
                        if (prog != null) {
                            onProgress(prog.coerceIn(0, 100))
                        }
                    } else if (trimmed == "READY") {
                        ready = true
                        break
                    }
                }

                if (!ready) {
                    runCatching { proc.destroyForcibly() }
                    return@withContext Result.failure(IllegalStateException("R worker failed to initialize"))
                }

                isInitialized = true
            }

            val writer = stdin ?: return@withContext Result.failure(IllegalStateException("plot_worker stdin not available"))
            val reader = stdoutReader ?: return@withContext Result.failure(IllegalStateException("plot_worker stdout not available"))

            // Now send the metadata request
            val req = """{"action":"metadata"}"""
            runCatching {
                writer.write(req); writer.write("\n"); writer.flush()
            }.onFailure { err ->
                return@withContext Result.failure(IllegalStateException("Failed to write to plot_worker: ${err.message}"))
            }

            val deadline = System.nanoTime() + 180_000_000_000L
            var metadataLine: String?

            while (System.nanoTime() < deadline) {
                val line = runCatching { reader.readLine() }.getOrNull()
                if (line == null) {
                    break
                }
                val t = line.trim()
                when {
                    t.startsWith("PROGRESS:") -> {
                        val progressStr = t.removePrefix("PROGRESS:").trim()
                        val prog = progressStr.toIntOrNull()
                        if (prog != null) {
                            onProgress(prog.coerceIn(0, 100))
                        }
                    }
                    t.startsWith("METADATA:") -> {
                        metadataLine = t.removePrefix("METADATA:").trim()

                        if (metadataLine.isBlank()) {
                            return@withContext Result.failure(IllegalStateException("plot_worker did not return metadata"))
                        }

                        val json = Json { ignoreUnknownKeys = true }
                        val root = runCatching { json.parseToJsonElement(metadataLine) }.getOrElse {
                            return@withContext Result.failure(IllegalStateException("Failed to parse metadata JSON: ${it.message}"))
                        }.jsonObject

                        val genes = root["genes"]?.jsonArray?.map { it.jsonPrimitive.contentOrNull ?: it.toString() } ?: emptyList()
                        val timepoints = root["timepoints"]?.jsonArray?.map { it.jsonPrimitive.contentOrNull ?: it.toString() } ?: emptyList()

                        return@withContext Result.success(AssayMetadata(genes, timepoints))
                    }
                }
            }
            Result.failure(IllegalStateException("plot_worker did not return metadata in time probably ?"))
        }
    }

    suspend fun runPlot(
        gene: String,
        timepoint: String,
        expressionDpi: Int,
        cellTypeDpi: Int,
        expressionPlotColor: String,
        dimPlotColorBy: Int,
        cellTypeLabelFontSizePx: Int,
        showDimLabels: Boolean,
        showExprLabels: Boolean,
        onProgress: (Int) -> Unit = {}
    ): Result<PlotOutputs> = mutex.withLock {
        withContext(Dispatchers.IO) {
            require(gene.isNotBlank() && timepoint.isNotBlank()) { "gene/timepoint must be set" }

            // Check if R worker is initialized (should be from requestMetadata)
            if (process?.isAlive != true || !isInitialized) {
                return@withContext Result.failure(IllegalStateException("R worker not initialized. Call requestMetadata first."))
            }

            val json = Json { encodeDefaults = true }

            // If expressionPlotColor is a custom scheme marker like "custom:3", convert it into a CUSTOM:<json-array> payload
            var exprColorToSend = expressionPlotColor
            if (expressionPlotColor.startsWith("custom:")) {
                val idx = expressionPlotColor.removePrefix("custom:").toIntOrNull()
                idx?.let {
                    val schemes = PreferenceManager.getColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES)
                    if (it >= 0 && it < schemes.size) {
                        val colors = schemes[it]
                        val colorsJson = json.encodeToString(colors)
                        exprColorToSend = "CUSTOM:" + colorsJson
                    }
                }
            }

            @Serializable
            data class PlotReq(
                val gene: String,
                val timepoint: String,
                val dpiExpr: Int,
                val dpiCType: Int,
                val colorExpr: String,
                val colorCType: Int,
                val labelSizePx: Int,
                val labelsExpr: Boolean,
                val labelsDim: Boolean
            )

            val req = json.encodeToString(PlotReq(
                gene = gene,
                timepoint = timepoint,
                dpiExpr = expressionDpi,
                dpiCType = cellTypeDpi,
                colorExpr = exprColorToSend,
                colorCType = dimPlotColorBy,
                labelSizePx = cellTypeLabelFontSizePx,
                labelsExpr = showExprLabels,
                labelsDim = showDimLabels
            ))
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

            val deadline = System.nanoTime() + 180_000_000_000L // look into this
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