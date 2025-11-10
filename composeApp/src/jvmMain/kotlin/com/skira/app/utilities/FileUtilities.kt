package com.skira.app.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

/**
 * Opens a system folder picker dialog and returns the selected folder path.
 *
 * @return The absolute path of the selected folder, or null if no folder was selected.
 */
fun openSystemFolderPicker(): String? {
    val chooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        isAcceptAllFileFilterUsed = false
    }
    var result = -1
    try {
        if (SwingUtilities.isEventDispatchThread()) {
            result = chooser.showOpenDialog(null)
        } else {
            SwingUtilities.invokeAndWait { result = chooser.showOpenDialog(null) }
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.selectedFile?.absolutePath
        }
    } catch (_: Exception) { /* TODO Add error logging/handling at some point */ }
    return null
}

/**
 * Verifies whether a given dataset directory contains the required files.
 * Only if the given directory contains merge.rds and pip.list.rds files, it is considered valid.
 *
 * @param path The path to the dataset directory. (This must be a folder)
 *
 * @return True if the directory contains the required files, false otherwise.
 */
fun verifySelectedDataset(path: String): Boolean {
    val datasetDir = File(path)
    if (!datasetDir.isDirectory) return false
    val required = listOf("pip.list.rds", "merge.rds")
    val missing = required.filter { fileName -> !File(datasetDir, fileName).exists() }
    return missing.isEmpty()
}

/**
 * Unizps a .ZIP file to a given directory asynchronously.
 *
 * @param zipFile The path to the ZIP file to unzip.
 * @param targetDir The directory to unzip the contents into.
 *
 * @return True if the operation was successful, false otherwise.
 */
suspend fun unzipToDirectory(zipFile: File, targetDir: File): Boolean = withContext(Dispatchers.IO) {
    try {
        if (!targetDir.exists()) targetDir.mkdirs()
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            val buffer = ByteArray(8 * 1024)
            while (entry != null) {
                val name = entry.name
                if (name.contains("..") || File(name).isAbsolute) {
                    zis.closeEntry()
                    entry = zis.nextEntry
                    continue
                }
                val outFile = File(targetDir, name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.let {
                        if (!it.exists()) {
                            it.mkdirs()
                        }
                    }
                    FileOutputStream(outFile).use { fos ->
                        var len: Int
                        var totalWritten = 0L
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                            totalWritten += len
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return@withContext true
    } catch (_: Exception) {
        return@withContext false
    }
}