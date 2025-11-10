package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.skira.app.components.DownloadIcon
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DownloadFormat
import com.skira.app.structures.PlotDownloadState
import com.skira.app.structures.PlotViewState
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.PreferenceManager
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Paths

/**
 * Fragment corresponding to the section directly above plots in the UI, showing progress updates and status
 * When plots are showing, this contains the plot open/download button sections
 *
 * @param viewModel The HomeViewModel instance managing the state and logic for the home screen
 */
@Composable
fun StatusBarFragment(viewModel: HomeViewModel) {
    AnimatedContent(
        targetState = Pair(viewModel.viewState, viewModel.isLoadingMeta),
        modifier = Modifier.padding(bottom = 10.dp)
    ) { target ->
        if (target.second) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Text(
                    text = if (PreferenceManager.getBoolean(PreferenceKey.ONBOARDING_COMPLETE)) {
                        stringResource(Res.string.status_bar_loading_initializing_state)
                    } else {
                        stringResource(Res.string.status_bar_pending_onboarding_state)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.3F)
                        .height(17.dp),
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                    color = Color(0XFFC7CED7)
                )
            }
        } else {
            when (target.first) {
                PlotViewState.Loading -> {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.status_bar_generating_plot_state),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val target = (viewModel.plotGenerationTaskProgress.coerceIn(0, 100)) / 100f
                            val animated by animateFloatAsState(
                                targetValue = target,
                                animationSpec = tween(durationMillis = 450),
                                label = "progressAnim"
                            )
                            LinearProgressIndicator(
                                progress = { animated },
                                modifier = Modifier.fillMaxWidth(0.3F)
                                    .height(17.dp),
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                                color = Color(0XFFC7CED7),
                                drawStopIndicator = {}
                            )
                            Text(
                                text = "${viewModel.plotGenerationTaskProgress}%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                    }
                }

                is PlotViewState.Error -> {
                    Text(
                        text = viewModel.loadError.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error.copy(0.7F)
                    )
                }

                else -> {
                    val statusText = when (target.first) {
                        PlotViewState.Ready -> stringResource(Res.string.status_bar_ready_state)
                        PlotViewState.SelectGene -> stringResource(Res.string.status_bar_choose_gene_state)
                        PlotViewState.SelectTime -> stringResource(Res.string.status_bar_choose_timepoint_state)
                        PlotViewState.SelectGeneAndTime -> stringResource(Res.string.status_bar_choose_both_state)
                        else -> ""
                    }
                    if (viewModel.dimPlotBitmap != null && viewModel.plotBitmap != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.5F)
                                        .padding(end = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    Row {
                                        DownloadViewButtonCluster(plotType = true, viewModel = viewModel)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    DownloadViewButtonCluster(plotType = false, viewModel = viewModel)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The cluster of buttons allowing the user to download the plot or open it in a new tab
 *
 * @param plotType Boolean indicating whether it's for the expression (true) or cell type (false) plot
 * @param viewModel The HomeViewModel instance from the parent composable
 */
@Composable
fun DownloadViewButtonCluster(plotType: Boolean, viewModel: HomeViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val downloadFilename = if (plotType) {
        "${viewModel.selectedGene}-${viewModel.selectedTimepoint}"
    } else {
        "cell-types-${viewModel.selectedTimepoint}"
    }
    val downloadFile = if (plotType) {
        viewModel.dimPlotBitmap
    } else {
        viewModel.plotBitmap
    }
    val relevantState = if (plotType) {
        viewModel.dimPlotDownloadState
    } else {
        viewModel.expressionPlotDownloadState
    }
    Row {
        AnimatedContent(
            targetState = if (plotType) {
                viewModel.showingExpressionDownloadMenu
            } else {
                viewModel.showingDimPlotDownloadMenu
            }
        ) { showDownload ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!showDownload) {
                    AnimatedContent(
                        targetState = if (plotType) {
                            viewModel.pendingOpenFeaturePlot
                        } else {
                            viewModel.pendingOpenDimPlot
                        }
                    ) { state ->
                        MinimalIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.apply {
                                        pendingOpenDimPlot = true
                                        openBitmapInSystemImageApp(viewModel.dimPlotBitmap)
                                        pendingOpenDimPlot = false
                                    }
                                }
                            },
                            icon = {
                                if (state) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeCap = StrokeCap.Round,
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(Res.drawable.icon_open_in_new_tab),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showDownload) {
                        AnimatedContent(targetState = if (plotType) viewModel.expressionPlotDownloadState else viewModel.dimPlotDownloadState) { downloadState ->

                            /* When the plot is downloaded, show success for 5 seconds then go back to idle state */
                            LaunchedEffect(downloadState) {
                                if (downloadState == PlotDownloadState.DOWNLOAD_SUCCESS) {
                                    delay(5000L)
                                    viewModel.dimPlotDownloadState = PlotDownloadState.IDLE
                                    viewModel.showingDimPlotDownloadMenu = false
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (downloadState) {
                                    PlotDownloadState.IDLE -> {
                                        DownloadIcon(
                                            painterResource(Res.drawable.icon_png_download),
                                            modifier = Modifier.padding(start = 5.dp),
                                            onClick = {
                                                viewModel.exportBitmapToDownloadDirectory(
                                                    image = downloadFile,
                                                    ext = ".${DownloadFormat.PNG}",
                                                    baseName = downloadFilename,
                                                    openFolder = false,
                                                    isFeaturePlot = plotType
                                                )
                                            }
                                        )
                                        DownloadIcon(
                                            painterResource(Res.drawable.icon_jpg_download),
                                            onClick = {
                                                viewModel.exportBitmapToDownloadDirectory(
                                                    image = downloadFile,
                                                    ext = ".${DownloadFormat.JPG}",
                                                    baseName = downloadFilename,
                                                    openFolder = false,
                                                    isFeaturePlot = plotType
                                                )
                                            }
                                        )
                                        DownloadIcon(
                                            painterResource(Res.drawable.icon_pdf_download),
                                            onClick = {
                                                viewModel.exportBitmapToDownloadDirectory(
                                                    image = downloadFile,
                                                    ext = ".${DownloadFormat.PDF}",
                                                    baseName = downloadFilename,
                                                    openFolder = false,
                                                    isFeaturePlot = plotType
                                                )
                                            }
                                        )
                                    }

                                    PlotDownloadState.DOWNLOADING_JPG -> {
                                        Text(
                                            text = stringResource(Res.string.status_bar_downloading_jpg_state),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                        )
                                    }

                                    PlotDownloadState.DOWNLOADING_PNG -> {
                                        Text(
                                            text = stringResource(Res.string.status_bar_downloading_png_state),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                        )
                                    }

                                    PlotDownloadState.DOWNLOADING_PDF -> {
                                        Text(
                                            text = stringResource(Res.string.status_bar_downloading_pdf_state),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                        )
                                    }

                                    PlotDownloadState.DOWNLOAD_SUCCESS -> {
                                        Text(
                                            text = "Downloaded to ${
                                                PreferenceManager.getString(
                                                    key = PreferenceKey.DATASET_DOWNLOAD_PATH,
                                                    default = Paths.get(
                                                        System.getProperty("user.home"),
                                                        "Downloads",
                                                        "SKiRA"
                                                    ).toString()
                                                )!!
                                            }..",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(color = 0X1C4B1D).copy(0.5F),
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    MinimalIconButton(
                        onClick = {
                            if (relevantState == PlotDownloadState.DOWNLOAD_SUCCESS
                                || relevantState == PlotDownloadState.DOWNLOAD_FAILURE
                            ) {
                                if (plotType) {
                                    viewModel.expressionPlotDownloadState = PlotDownloadState.IDLE
                                } else {
                                    viewModel.dimPlotDownloadState = PlotDownloadState.IDLE
                                }
                            } else {
                                if (plotType) {
                                    viewModel.showingExpressionDownloadMenu = !viewModel.showingExpressionDownloadMenu
                                } else {
                                    viewModel.showingDimPlotDownloadMenu = !viewModel.showingDimPlotDownloadMenu
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = if (showDownload) {
                                    painterResource(Res.drawable.icon_close_round)
                                } else {
                                    painterResource(Res.drawable.icon_download)
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}