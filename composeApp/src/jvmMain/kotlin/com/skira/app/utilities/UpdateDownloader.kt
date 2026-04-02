package com.skira.app.utilities

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

private val downloadClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)   // No read timeout — file may be large
    .build()

/**
 * Downloads the update asset at [url] into a temp directory and streams progress
 * to [onProgress] as a value in 0.0..1.0.
 *
 * Run this from a background thread / [kotlinx.coroutines.Dispatchers.IO] coroutine.
 *
 * @param url       Direct download URL (e.g. a GitHub release asset URL)
 * @param fileName  Destination file name (e.g. "SKiRA-1.4.0.dmg")
 * @param totalSize Expected total byte count from GitHub metadata (0 = unknown, fallback to Content-Length)
 * @param onProgress Callback invoked with progress fraction 0.0..1.0 as bytes arrive.
 *                   Called from the IO thread — use thread-safe state (e.g. MutableStateFlow).
 *
 * @return The downloaded [File] on success.
 * @throws Exception on any network or I/O error.
 */
fun downloadUpdateAsset(
    url: String,
    fileName: String,
    totalSize: Long,
    onProgress: (Float) -> Unit
): File {
    val tempDir = File(System.getProperty("java.io.tmpdir"), "skira_update").also { it.mkdirs() }
    val target  = File(tempDir, fileName)

    val request = Request.Builder().url(url).build()
    downloadClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("Download failed: HTTP ${response.code}")

        val body        = response.body ?: throw Exception("Empty response body")
        val knownLength = if (totalSize > 0) totalSize
                          else response.headers["Content-Length"]?.toLongOrNull() ?: -1L

        target.outputStream().use { out ->
            body.byteStream().use { input ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead = 0L
                var n: Int
                while (input.read(buffer).also { n = it } != -1) {
                    out.write(buffer, 0, n)
                    bytesRead += n
                    if (knownLength > 0) {
                        onProgress((bytesRead.toFloat() / knownLength).coerceIn(0f, 1f))
                    }
                }
            }
        }
    }

    onProgress(1.0f)
    return target
}

/**
 * Opens the downloaded installer so the user can proceed with installation,
 * then returns immediately (the installer runs independently of the app process).
 *
 * - **macOS**: opens the DMG in Finder via `open`.
 * - **Windows**: launches the MSI with `msiexec /i` or runs the EXE directly.
 * - **Other**: falls back to [java.awt.Desktop.open].
 */
fun launchInstaller(file: File) {
    val os = System.getProperty("os.name")?.lowercase() ?: ""
    when {
        os.contains("mac") ->
            ProcessBuilder("open", file.absolutePath).start()

        os.contains("win") ->
            if (file.name.endsWith(".msi", ignoreCase = true))
                ProcessBuilder("msiexec", "/i", file.absolutePath).start()
            else
                ProcessBuilder(file.absolutePath).start()

        else ->
            runCatching { java.awt.Desktop.getDesktop().open(file) }
    }
}

