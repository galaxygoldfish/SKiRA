package com.skira.app.view.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.utilities.PreferenceManager
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.PreferenceKey
import com.skira.app.structures.DialogType
import com.skira.app.viewmodel.WelcomeDialogViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
fun DownloadFetchDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    val viewModel: WelcomeDialogViewModel = viewModel()
    LaunchedEffect(true) {
        viewModel.initiateDatasetDownload()
    }
    AnimatedContent(targetState = viewModel.contentTransferComplete) { downloaded ->
        Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
            Text(
                text = stringResource(
                    resource = if (downloaded) {
                        Res.string.download_dataset_dialog_title_complete
                    } else {
                        Res.string.download_dataset_dialog_title_in_progress
                    }
                ),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    resource = if (downloaded) {
                        Res.string.download_dataset_dialog_message_complete
                    } else {
                        Res.string.download_dataset_dialog_message_in_progress
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 30.dp).fillMaxWidth(0.33F)
            )
            Text(
                text = if (downloaded) {
                    stringResource(
                        resource = Res.string.download_dataset_dialog_path_complete,
                        viewModel.downloadFolderPath
                    )
                } else {
                    stringResource(
                        resource = Res.string.download_dataset_dialog_path_in_progress,
                        viewModel.downloadFolderPath
                    )
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                modifier = Modifier.padding(top = 5.dp).fillMaxWidth(0.33F)
            )

            if (!downloaded) {
                if (viewModel.isUnzippingContent) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .fillMaxWidth(0.33F)
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(0.15F)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.33F)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Unpacking file contents",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                        )
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { viewModel.downloadProgress },
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .fillMaxWidth(0.33F)
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(0.15F)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.33F)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row {
                            Text(
                                text = "${(viewModel.downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(
                                    Res.string.download_dataset_dialog_progress_percentage,
                                    round(viewModel.downloadProgress * 2.92 * 100) / 100.0
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(0.33F)
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!downloaded) {
                    ActionTextButton(
                        text = stringResource(Res.string.dialog_navigate_button_cancel),
                        onClick = {
                            onNavigationRequest(DialogType.SELECT_DOWNLOAD_PATH)
                        },
                        color = MaterialTheme.colorScheme.error.copy(0.4F),
                        filled = false
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                ActionTextButton(
                    text = stringResource(Res.string.dialog_navigate_button_continue),
                    onClick = {
                        PreferenceManager.putBoolean(PreferenceKey.ONBOARDING_COMPLETE, true)
                        onNavigationRequest(DialogType.NONE)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    filled = true,
                    enabled = downloaded
                )
            }
        }
    }
}