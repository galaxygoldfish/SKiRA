package com.skira.app.view.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.status_bar_percent
import com.skira.app.composeapp.generated.resources.update_dialog_button_install
import com.skira.app.composeapp.generated.resources.update_dialog_button_later
import com.skira.app.composeapp.generated.resources.update_dialog_button_retry
import com.skira.app.composeapp.generated.resources.update_dialog_button_update
import com.skira.app.composeapp.generated.resources.update_dialog_downloading
import com.skira.app.composeapp.generated.resources.update_dialog_error_message
import com.skira.app.composeapp.generated.resources.update_dialog_release_notes_label
import com.skira.app.composeapp.generated.resources.update_dialog_title
import com.skira.app.composeapp.generated.resources.update_dialog_unknown_error
import com.skira.app.structures.UpdateInfo
import com.skira.app.utilities.downloadUpdateAsset
import com.skira.app.utilities.launchInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
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
    val unknownErrorText = stringResource(Res.string.update_dialog_unknown_error)
    var downloadState by remember { mutableStateOf(DownloadState.IDLE) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
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
                errorMessage  = e.message ?: unknownErrorText
                downloadState = DownloadState.ERROR
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 10.dp)
            .fillMaxWidth(0.4F)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = stringResource(Res.string.update_dialog_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                modifier = Modifier.clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = updateInfo.currentVersion,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
        if (updateInfo.releaseNotes.isNotBlank()) {
            Text(
                text = stringResource(Res.string.update_dialog_release_notes_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = updateInfo.releaseNotes.lines().take(8).joinToString("\n").trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .heightIn(max = 130.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(Modifier.height(24.dp))

        AnimatedContent(targetState = downloadState, label = "update-state") { state ->
            when (state) {

                DownloadState.IDLE -> {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ActionTextButton(
                            text = stringResource(Res.string.update_dialog_button_update),
                            color = MaterialTheme.colorScheme.onBackground,
                            filled = true,
                            onClick = { startDownload() }
                        )
                        ActionTextButton(
                            text = stringResource(Res.string.update_dialog_button_later),
                            color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                            filled = false,
                            onClick = onDismiss
                        )
                    }
                }

                DownloadState.DOWNLOADING -> {
                    Column {
                        Text(
                            text  = stringResource(Res.string.update_dialog_downloading),
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
                            text     = stringResource(Res.string.status_bar_percent, (progress * 100).toInt()),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                DownloadState.COMPLETE -> {
                    Column {
                        ActionTextButton(
                            text    = stringResource(Res.string.update_dialog_button_install),
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
                            text     = stringResource(
                                Res.string.update_dialog_error_message,
                                errorMessage ?: unknownErrorText
                            ),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionTextButton(
                                text    = stringResource(Res.string.update_dialog_button_later),
                                color   = MaterialTheme.colorScheme.onBackground,
                                filled  = false,
                                onClick = onDismiss
                            )
                            ActionTextButton(
                                text    = stringResource(Res.string.update_dialog_button_retry),
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
