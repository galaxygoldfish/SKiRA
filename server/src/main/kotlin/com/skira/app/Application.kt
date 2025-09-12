package com.skira.app

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit
// ... existing code ...

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("127.0.0.1:8081", schemes = listOf("http")) // optional, if you serve the UI on 127.0.0.1
        // ... existing code ...
        allowHost("localhost:8080", schemes = listOf("http"))   // allow frontend origin
        allowHost("127.0.0.1:8080", schemes = listOf("http"))   // optional: if UI is served on 127.0.0.1
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        // allowCredentials = true // enable only if you need cookies/credentials
    }


    routing {
        // A simple root route to avoid "No matched subtrees" when someone hits "/"
        get("/") {
            call.respondText("SKiRA server is running", ContentType.Text.Plain)
        }

        // return genes and timepoints (JSON) by running metadata.R
        get("/getAssayMeta") {
            val topDirOverride = call.request.queryParameters["topDir"]?.trim().orEmpty()
            val topDir = when {
                topDirOverride.isNotEmpty() -> topDirOverride
                System.getenv("TOP_DIR")?.isNotBlank() == true -> System.getenv("TOP_DIR")
                else -> "C:/Users/Sebastian/Documents/Research/KillifishEmbryogenesis_scRNAseq"
            }

            val scriptTemp = extractResourceToTemp("metadata.R", "meta", ".R")
            try {
                val (cmd, baseArgs) = resolveRInvoker() ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "R runtime not found. Configure one of: env RSCRIPT, -Drscript.path=..., or include R/Rscript on PATH."
                    )
                    return@get
                }

                val args = when {
                    // Using Rscript: Rscript metadata.R <topDir>
                    baseArgs.contains("--use-rscript") ->
                        listOf(cmd, scriptTemp.absolutePath, topDir)
                    else -> {
                        // Using R: R --vanilla -f metadata.R --args <topDir>
                        listOf(cmd) + baseArgs + listOf("-f", scriptTemp.absolutePath, "--args", topDir)
                    }
                }

                val pb = ProcessBuilder(args).redirectErrorStream(true)
                val proc = withContext(Dispatchers.IO) { pb.start() }

                val stdout = withContext(Dispatchers.IO) { proc.inputStream.readAll() }

                val finished = proc.waitFor(60, TimeUnit.SECONDS)
                if (!finished) {
                    proc.destroyForcibly()
                    call.respond(HttpStatusCode.RequestTimeout, "R metadata query timed out")
                    return@get
                }

                if (proc.exitValue() != 0) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to get assay metadata (exit ${proc.exitValue()}): $stdout"
                    )
                    return@get
                }

                call.respondText(stdout, ContentType.Application.Json)
            } catch (t: Throwable) {
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${t.message}")
            } finally {
                runCatching { scriptTemp.delete() }
            }
        }

        // Route that runs the R script and returns a PNG
        get("/plot") {
            val gene = call.request.queryParameters["gene"]?.trim().orEmpty()
            val timepoint = call.request.queryParameters["timepoint"]?.trim().orEmpty()

            if (gene.isEmpty() || timepoint.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing required query parameters: gene and timepoint"
                )
                return@get
            }

            // Extract plot.R from resources to a temp file (resources are not real files in a jar)
            val scriptTemp = extractResourceToTemp("plot.R", "plot", ".R")
            try {
                // Resolve an invoker: either Rscript or R with proper flags
                val (cmd, baseArgs) = resolveRInvoker() ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "R runtime not found. Configure one of the following:\n" +
                            "- Set environment variable RSCRIPT to full path of Rscript or R (e.g., C:\\\\Program Files\\\\R\\\\R-4.x.x\\\\bin\\\\R.exe)\n" +
                            "- Or set JVM property -Drscript.path=<path to Rscript or R>\n" +
                            "- Or add Rscript or R to the system PATH"
                    )
                    return@get
                }

                val args = when {
                    // Using Rscript: Rscript <script> gene timepoint
                    baseArgs.contains("--use-rscript") -> listOf(cmd, scriptTemp.absolutePath, gene, timepoint)
                    else -> {
                        // Using R: R --vanilla -f <script> --args gene timepoint
                        listOf(cmd) + baseArgs + listOf("-f", scriptTemp.absolutePath, "--args", gene, timepoint)
                    }
                }

                val pb = ProcessBuilder(args).redirectErrorStream(true)
                val proc = withContext(Dispatchers.IO) { pb.start() }

                // Capture stdout fully (plot.R writes the PNG path to stdout)
                val stdout = withContext(Dispatchers.IO) {
                    proc.inputStream.readAll()
                }

                val finished = proc.waitFor(120, TimeUnit.SECONDS)
                if (!finished) {
                    proc.destroyForcibly()
                    call.respond(HttpStatusCode.RequestTimeout, "R script timed out")
                    return@get
                }

                if (proc.exitValue() != 0) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "R invocation failed (exit ${proc.exitValue()}): $stdout"
                    )
                    return@get
                }

                // plot.R prints the generated outfile path to stdout; take the last non-empty line
                val outfilePath = stdout
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .lastOrNull()

                if (outfilePath.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "R script did not output an image path"
                    )
                    return@get
                }

                val pngFile = File(outfilePath)
                if (!pngFile.exists() || !pngFile.isFile) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Image not found at: $outfilePath"
                    )
                    return@get
                }

                val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(pngFile.toPath()) }

                // Best-effort cleanup
                @Suppress("BlockingMethodInNonBlockingContext")
                withContext(Dispatchers.IO) {
                    runCatching { pngFile.delete() }
                }

                call.respondBytes(
                    bytes = bytes,
                    contentType = ContentType.Image.PNG
                )
            } catch (t: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Server error: ${t.message}"
                )
            } finally {
                runCatching { scriptTemp.delete() }
            }
        }
    }
}

private fun resolveRInvoker(): Pair<String, List<String>>? {
    // 1) Explicit env var
    System.getenv("RSCRIPT")?.let { path ->
        if (canInvoke(path, "--version")) {
            return if (isRscriptName(path)) path to listOf("--use-rscript")
            else path to listOf("--vanilla") // assume plain R
        }
    }
    // 2) JVM property
    System.getProperty("rscript.path")?.let { path ->
        if (canInvoke(path, "--version")) {
            return if (isRscriptName(path)) path to listOf("--use-rscript")
            else path to listOf("--vanilla")
        }
    }
    // 3) Try PATH for Rscript first
    val rscriptCandidates = listOf("Rscript", "Rscript.exe")
    for (c in rscriptCandidates) {
        if (canInvoke(c, "--version")) return c to listOf("--use-rscript")
    }
    // 4) Try PATH for R
    val rCandidates = listOf("R", "R.exe")
    for (c in rCandidates) {
        if (canInvoke(c, "--version")) return c to listOf("--vanilla")
    }
    return null
}

private fun isRscriptName(pathOrName: String): Boolean {
    val name = File(pathOrName).name.lowercase()
    return name == "rscript" || name == "rscript.exe"
}

private fun canInvoke(cmd: String, vararg args: String): Boolean {
    return try {
        val p = ProcessBuilder(listOf(cmd) + args).redirectErrorStream(true).start()
        p.waitFor(5, TimeUnit.SECONDS)
        true
    } catch (_: Exception) {
        false
    }
}

private fun extractResourceToTemp(resourceName: String, prefix: String, suffix: String): File {
    val stream: InputStream = Application::class.java.classLoader.getResourceAsStream(resourceName)
        ?: error("Resource not found: $resourceName")

    val temp = File.createTempFile(prefix, suffix)
    temp.deleteOnExit()
    stream.use { input ->
        temp.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return temp
}

private fun InputStream.readAll(): String =
    this.bufferedReader(Charsets.UTF_8).use { it.readText() }