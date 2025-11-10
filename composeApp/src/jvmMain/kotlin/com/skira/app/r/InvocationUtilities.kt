package com.skira.app.r

import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

fun extractResourceToTemp(resourceName: String, prefix: String, suffix: String): File {
    val stream: InputStream = Thread.currentThread().contextClassLoader
        .getResourceAsStream(resourceName)
        ?: error("Resource not found: $resourceName")
    val temp = File.createTempFile(prefix, suffix)
    temp.deleteOnExit()
    stream.use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
    return temp
}

fun resolveRInvoker(): Pair<String, List<String>>? {
    System.getenv("RSCRIPT")?.let { return it to listOf("--use-rscript") }
    listOf("Rscript", "Rscript.exe").firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--use-rscript")
    }
    listOf("R", "R.exe").firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--vanilla")
    }
    return null
}

private fun canInvoke(cmd: String, vararg args: String): Boolean = try {
    val p = ProcessBuilder(listOf(cmd) + args).redirectErrorStream(true).start()
    p.waitFor(3, TimeUnit.SECONDS)
    true
} catch (_: Exception) { false }