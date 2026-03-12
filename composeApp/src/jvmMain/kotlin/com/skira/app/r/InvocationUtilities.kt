package com.skira.app.r

import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

fun extractResourcesToTempDir(resourceNames: List<String>, prefix: String): File {
    val tempDir = createTempDir(prefix = prefix)
    tempDir.deleteOnExit()
    resourceNames.forEach { resourceName ->
        val stream: InputStream = Thread.currentThread().contextClassLoader
            .getResourceAsStream(resourceName)
            ?: error("Resource not found: $resourceName")
        val outFile = File(tempDir, resourceName.substringAfterLast('/'))
        stream.use { input -> outFile.outputStream().use { output -> input.copyTo(output) } }
        outFile.deleteOnExit()
    }
    return tempDir
}

fun resolveRInvoker(): Pair<String, List<String>>? {
    System.getenv("RSCRIPT")?.let { return it to listOf("--use-rscript") }
    listOf("Rscript", "Rscript.exe", "rscript").firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--use-rscript")
    }
    listOf("R", "R.exe", "/usr/local/bin/r").firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--vanilla")
    }
    return null
}

fun canInvoke(cmd: String, vararg args: String): Boolean = try {
    val p = ProcessBuilder(listOf(cmd) + args).redirectErrorStream(true).start()
    p.waitFor(3, TimeUnit.SECONDS)
    true
} catch (_: Exception) { false }