package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skira.app.components.DownloadIcon
import com.skira.app.components.MinimalIconButton
import com.skira.app.components.SmoothProgressBar
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.PlotDownloadState
import com.skira.app.structures.PlotViewState
import com.skira.app.structures.PreferenceKey
import com.skira.app.structures.TimepointHPF
import com.skira.app.utilities.PreferenceManager
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Fragment corresponding to the section directly above plots in the UI, showing progress updates and status
 * When plots are showing, this contains the plot open/download button sections
 *
 * @param viewModel The HomeViewModel instance managing the state and logic for the home screen
 */
@Composable
fun StatusBarFragment(viewModel: HomeViewModel) {

    val reportedProgress = viewModel.metadataLoadingProgress
    val lastReportedProgress = remember { mutableStateOf(0) }
    val bridgeAnim = remember { Animatable(0f) }

    LaunchedEffect(reportedProgress) {
        if (reportedProgress > lastReportedProgress.value) {
            bridgeAnim.stop()
            lastReportedProgress.value = reportedProgress
        }
    }

    LaunchedEffect(lastReportedProgress.value) {
        val currentReported = lastReportedProgress.value
        val nextTarget = when (currentReported) {
            10 -> 50
            50 -> 90
            90 -> 90
            else -> currentReported
        }
        if (currentReported < 90) {
            while (isActive) {
                bridgeAnim.snapTo(currentReported / 100f)
                bridgeAnim.animateTo(
                    targetValue = nextTarget / 100f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
                bridgeAnim.animateTo(
                    targetValue = (nextTarget - 5) / 100f,
                    animationSpec = tween(durationMillis = 800)
                )
            }
        }
    }

    AnimatedContent(
        targetState = viewModel.isLoadingMeta,
        modifier = Modifier.padding(top = 5.dp)
    ) { target ->
        if (target) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        visible = viewModel.isLoadingMeta,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Image(
                            painter = if (reportedProgress < 10) {
                                painterResource(Res.drawable.icon_curve)
                            } else {
                                painterResource(Res.drawable.icon_chip)
                            },
                            contentDescription = null,
                            modifier = Modifier.alpha(0.7F)
                        )
                    }
                    AnimatedContent(
                        targetState = if (PreferenceManager.getBoolean(PreferenceKey.ONBOARDING_COMPLETE)) {
                            if (reportedProgress < 10) {
                                stringResource(Res.string.status_bar_loading_meta_step_1)
                            } else {
                                stringResource(Res.string.status_bar_loading_meta_step_2)
                            }
                        } else {
                            stringResource(Res.string.status_bar_pending_onboarding_state)
                        }
                    ) { statusText ->
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                        )
                    }
                }
                val progressToShow = if (reportedProgress == lastReportedProgress.value) {
                    (bridgeAnim.value * 100).roundToInt()
                } else {
                    reportedProgress
                }
                SmoothProgressBar(
                    reportedProgress = progressToShow,
                    modifier = Modifier.fillMaxWidth(0.3F).height(15.dp)
                )
            }
        } else {
            AnimatedContent(viewModel.viewState) { viewState ->
                when (viewState) {
                    PlotViewState.Loading -> {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.status_bar_generating_plot_state),
                                style = MaterialTheme.typography.bodyLarge,
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
                                    text = stringResource(Res.string.status_bar_percent, viewModel.plotGenerationTaskProgress),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                        }
                    }

                    is PlotViewState.Error -> {
                        Text(
                            text = viewModel.loadError.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error.copy(0.7F)
                        )
                    }

                    else -> {
                        val statusText = when (viewState) {
                            PlotViewState.Ready -> stringResource(Res.string.status_bar_ready_state)
                            PlotViewState.SelectGene -> stringResource(Res.string.status_bar_choose_gene_state)
                            PlotViewState.SelectTime -> stringResource(Res.string.status_bar_choose_timepoint_state)
                            PlotViewState.SelectGeneAndTime -> stringResource(Res.string.status_bar_choose_both_state)
                            else -> ""
                        }
                        if (viewModel.dimPlotBitmap != null && viewModel.plotBitmap != null) {
                            val timepointStageLabel = when (viewModel.selectedTimepoint) {
                                TimepointHPF.TIMEPOINT_52HPF -> stringResource(Res.string.timepoint_stage_52hpf)
                                TimepointHPF.TIMEPOINT_72HPF -> stringResource(Res.string.timepoint_stage_72hpf)
                                TimepointHPF.TIMEPOINT_96HPF -> stringResource(Res.string.timepoint_stage_96hpf)
                                TimepointHPF.TIMEPOINT_115HPF -> stringResource(Res.string.timepoint_stage_115hpf)
                                TimepointHPF.TIMEPOINT_ALL -> stringResource(Res.string.timepoint_stage_all)
                                else -> viewModel.selectedTimepoint
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5F)
                                            .padding(end = 10.dp)
                                    ) {
                                        Text(
                                            text = stringResource(
                                                Res.string.status_bar_feature_plot_title,
                                                viewModel.selectedGene,
                                                viewModel.selectedTimepoint
                                            ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                                            DownloadViewButtonCluster(plotType = true, viewModel = viewModel)
                                        }
                                    }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = stringResource(Res.string.status_bar_cell_types_title, timepointStageLabel),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                                            DownloadViewButtonCluster(plotType = false, viewModel = viewModel)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                            )
                        }
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
                                        if (plotType) {
                                            pendingOpenFeaturePlot = true
                                            openBitmapInSystemImageApp(viewModel.plotBitmap)
                                            pendingOpenFeaturePlot = false
                                        } else {
                                            pendingOpenDimPlot = true
                                            openBitmapInSystemImageApp(viewModel.dimPlotBitmap)
                                            pendingOpenDimPlot = false
                                        }
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
                MinimalIconButton(
                    onClick = {
                        viewModel.openExportPlotDialog(isFeaturePlot = plotType)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.icon_download),
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