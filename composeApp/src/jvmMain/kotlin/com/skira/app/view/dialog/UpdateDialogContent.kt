package com.skira.app.view.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.structures.UpdateInfo
import com.skira.app.utilities.downloadUpdateAsset
import com.skira.app.utilities.launchInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class DownloadState { IDLE, DOWNLOADING, COMPLETE, ERROR }

/**
 * Dialog content shown when a newer version is available on GitHub Releases.
 *
 * Flow:
 * 1. IDLE        — shows version info + "Update now" / "Later" buttons.
 * 2. DOWNLOADING — shows a progress bar while the asset is being fetched.
 * 3. COMPLETE    — shows "Install now & restart"; clicking launches the
 *                  installer and exits the app.
 * 4. ERROR       — shows the failure message + "Retry" button.
 *
 * @param updateInfo      Metadata about the available release.
 * @param onDismiss       Called when the user chooses "Later" (or after error dismissal).
 * @param exitApplication Called after the installer is launched so the app exits cleanly.
 */
@Composable
fun UpdateDialogContent(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    exitApplication: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var downloadState by remember { mutableStateOf(DownloadState.IDLE) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    // MutableStateFlow is thread-safe — written from the IO coroutine, read on Main.
    val progressFlow = remember { MutableStateFlow(0f) }
    val progress by progressFlow.asStateFlow().collectAsState()

    fun startDownload() {
        downloadState = DownloadState.DOWNLOADING
        errorMessage  = null
        progressFlow.value = 0f

        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    downloadUpdateAsset(
                        url        = updateInfo.downloadUrl,
                        fileName   = updateInfo.assetName,
                        totalSize  = updateInfo.assetSize,
                        onProgress = { p -> progressFlow.value = p }
                    )
                }
                downloadedFile = file
                downloadState  = DownloadState.COMPLETE
            } catch (e: Exception) {
                errorMessage  = e.message ?: "Unknown error"
                downloadState = DownloadState.ERROR
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 10.dp)
            .widthIn(max = 420.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────────
        Text(
            text  = "Update available",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ── Version line ────────────────────────────────────────────────────────
        Text(
            text     = "Version ${updateInfo.latestVersion} is available — you're on ${updateInfo.currentVersion}",
            style    = MaterialTheme.typography.bodyLarge,
            color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 10.dp)
        )

        // ── Release notes (optional) ────────────────────────────────────────────
        if (updateInfo.releaseNotes.isNotBlank()) {
            Text(
                text     = "What's new",
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text     = updateInfo.releaseNotes.lines().take(8).joinToString("\n").trim(),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .heightIn(max = 130.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Dynamic lower section ────────────────────────────────────────────────
        AnimatedContent(targetState = downloadState, label = "update-state") { state ->
            when (state) {

                DownloadState.IDLE -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionTextButton(
                            text    = "Later",
                            color   = MaterialTheme.colorScheme.onBackground,
                            filled  = false,
                            onClick = onDismiss
                        )
                        ActionTextButton(
                            text    = "Update now",
                            color   = MaterialTheme.colorScheme.onBackground,
                            filled  = true,
                            onClick = { startDownload() }
                        )
                    }
                }

                DownloadState.DOWNLOADING -> {
                    Column {
                        Text(
                            text  = "Downloading update…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        LinearProgressIndicator(
                            progress   = { progress },
                            modifier   = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .height(8.dp),
                            color      = MaterialTheme.colorScheme.onBackground,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                        )
                        Text(
                            text     = "${(progress * 100).toInt()}%",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                DownloadState.COMPLETE -> {
                    Column {
                        Text(
                            text     = "Download complete",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        ActionTextButton(
                            text    = "Install now & restart",
                            color   = MaterialTheme.colorScheme.onBackground,
                            filled  = true,
                            onClick = {
                                downloadedFile?.let { launchInstaller(it) }
                                exitApplication()
                            }
                        )
                    }
                }

                DownloadState.ERROR -> {
                    Column {
                        Text(
                            text     = "Download failed: ${errorMessage ?: "Unknown error"}",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionTextButton(
                                text    = "Later",
                                color   = MaterialTheme.colorScheme.onBackground,
                                filled  = false,
                                onClick = onDismiss
                            )
                            ActionTextButton(
                                text    = "Retry",
                                color   = MaterialTheme.colorScheme.onBackground,
                                filled  = true,
                                onClick = { startDownload() }
                            )
                        }
                    }
                }
            }
        }
    }
}
