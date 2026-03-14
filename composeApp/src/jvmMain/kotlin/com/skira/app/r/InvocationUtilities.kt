package com.skira.app.r

import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

fun extractResourcesToTempDir(resourceNames: List<String>, prefix: String): File {
    val tempDir = createTempDir(prefix = prefix)
    tempDir.deleteOnExit()
    // In a packaged macOS app the thread context class loader can be null;
    // fall back to the class loader of this compilation unit.
    val cl = Thread.currentThread().contextClassLoader
        ?: object {}.javaClass.classLoader
        ?: ClassLoader.getSystemClassLoader()
    resourceNames.forEach { resourceName ->
        val stream: InputStream = cl.getResourceAsStream(resourceName)
            ?: error("Resource not found: $resourceName (classloader: $cl)")
        val outFile = File(tempDir, resourceName.substringAfterLast('/'))
        stream.use { input -> outFile.outputStream().use { output -> input.copyTo(output) } }
        outFile.deleteOnExit()
    }
    return tempDir
}

fun resolveRInvoker(): Pair<String, List<String>>? {
    System.getenv("RSCRIPT")?.let { return it to listOf("--use-rscript") }
    // Try Rscript-style; include absolute paths for Apple Silicon (Homebrew) and Intel (CRAN/Homebrew)
    // since macOS GUI apps launched from Finder do NOT inherit the user's shell PATH
    listOf(
        "Rscript",
        "rscript",
        "/opt/homebrew/bin/Rscript",   // Apple Silicon Homebrew
        "/usr/local/bin/Rscript",      // Intel Homebrew / CRAN installer
        "/usr/bin/Rscript"             // Some Linux installs
    ).firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--use-rscript")
    }
    // Fallback to R
    listOf(
        "R",
        "/opt/homebrew/bin/R",         // Apple Silicon Homebrew
        "/usr/local/bin/R",            // Intel Homebrew / CRAN
        "/usr/local/bin/r"             // Original (case-insensitive on macOS)
    ).firstOrNull { canInvoke(it, "--version") }?.let {
        return it to listOf("--vanilla")
    }
    return null
}

fun canInvoke(cmd: String, vararg args: String): Boolean = try {
    val p = ProcessBuilder(listOf(cmd) + args).redirectErrorStream(true).start()
    p.waitFor(3, TimeUnit.SECONDS)
    true
} catch (_: Exception) { false }