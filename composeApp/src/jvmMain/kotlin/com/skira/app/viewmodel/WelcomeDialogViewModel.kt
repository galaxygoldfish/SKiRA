package com.skira.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.skira.app.utilities.PreferenceManager
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.unzipToDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ViewModel corresponding to the entire onboarding process, starting with the WelcomeDialogContent
 * This also encompasses the dataset selection, download, configuration and extraction process
 * (DownloadFetchDialogContent, DownloadOnboardDialogContent, DownloadSetPathDialogContent,
 * DownloadSelectExistingDialogContent)
 */
class WelcomeDialogViewModel : ViewModel() {

    /* This is where our scRNA-seq dataset is fetched from (it's public, so we don't need to obfuscate) */
    val remoteDatasetFetchURL: String =
        "https://pub-ac9bdea7af4d4e7e81d0657580402e34.r2.dev/KillifishEmbryogenesis_scRNAseq.zip"

    /* Status indicators for the remote dataset download part of onboarding (in DownloadFetchDialogContent) */
    var downloadProgress by mutableStateOf(0F)
    var contentTransferComplete by mutableStateOf(false)
    var isUnzippingContent by mutableStateOf(false)

    /* User-selected path which indicates where we download and unzip the dataset if needed */
    val downloadFolderPath: String
        get() = PreferenceManager.getString(
            key = PreferenceKey.DATASET_DOWNLOAD_PATH,
            default = System.getProperty("java.io.tmpdir")
        )!!

    /**
     * Begin the dataset download process to the user's selected download path
     * Updates the downloadProgress, isUnzippingContent and contentTransferComplete states along the way
     * It is not yet tested what would happen if the user closes the app or if the connection is dropped while fetching
     */
    suspend fun initiateDatasetDownload() {
        val downloadFolder = File(downloadFolderPath).apply { if (!exists()) mkdirs() }
        val zipFile = File(downloadFolder, "download-skira.zip")
        downloadUrlToFile(
            url = remoteDatasetFetchURL,
            dest = zipFile,
            onProgress = { progress ->
                downloadProgress = progress
            }
        )
        isUnzippingContent = true
        val unzippedSuccessful = withContext(Dispatchers.IO) {
            try {
                unzipToDirectory(zipFile, downloadFolder)
            } catch (_: Throwable) {
                false
            }
        }
        if (unzippedSuccessful) {
            PreferenceManager.putString(
                PreferenceKey.R_DATASET_FOLDER,
                downloadFolder.absolutePath + "/KillifishEmbryogenesis_scRNAseq"
            )
            if (zipFile.exists()) {
                zipFile.delete()
            }
            withContext(Dispatchers.Main) {
                isUnzippingContent = false
                contentTransferComplete = true
            }
        }
    }

    /**
     * Downloads a file from a given URL to a destination File object, reporting progress via a callback
     *
     * @param url The URL to download the file from
     * @param dest The destination File to save the downloaded content
     * @param onProgress A callback function that receives progress updates as a Float between 0 and 1
     *
     * @return True if the download was successful, false otherwise
     */
    private suspend fun downloadUrlToFile(
        url: String,
        dest: File,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
        val req = Request.Builder()
            .url(url)
            .get()
            .header("Accept-Encoding", "identity")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext false
            val body = resp.body ?: return@withContext false
            val contentLength = body.contentLength()
            try {
                var bytesReadInIteration: Int
                var totalBytesRead = 0L
                var lastReportedFraction = -1f
                dest.outputStream().use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8 * 1024)
                        while (input.read(buffer).also { bytesReadInIteration = it } != -1) {
                            out.write(buffer, 0, bytesReadInIteration)
                            totalBytesRead += bytesReadInIteration
                            val frac = if (contentLength > 0) {
                                (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            if (frac == 1f || frac - lastReportedFraction >= 0.01f) {
                                lastReportedFraction = frac
                                withContext(Dispatchers.Main) { onProgress(frac) }
                            }
                        }
                    }
                }
                if (lastReportedFraction < 1f) withContext(Dispatchers.Main) { onProgress(1f) }
                return@withContext true
            } catch (_: IOException) {
                return@withContext false
            }
        }
    }

}