package com.skira.app.view.fragment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.skira.app.components.QuadrantLabelledImage
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.TimepointHPF
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Fragment displaying the visualization and DAPI stain images side by side for a selected timepoint
 *
 * @param selectedTimepoint The selected timepoint to display images for (use TimepointHPF constants)
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VisualizationFragment(selectedTimepoint: String) {
    val timepointToStage = mapOf(
        TimepointHPF.TIMEPOINT_52HPF to stringResource(Res.string.timepoint_stage_52hpf),
        TimepointHPF.TIMEPOINT_72HPF to stringResource(Res.string.timepoint_stage_72hpf),
        TimepointHPF.TIMEPOINT_96HPF to stringResource(Res.string.timepoint_stage_96hpf),
        TimepointHPF.TIMEPOINT_115HPF to stringResource(Res.string.timepoint_stage_115hpf),
        TimepointHPF.TIMEPOINT_ALL to stringResource(Res.string.timepoint_stage_all)
    )
    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            if (selectedTimepoint == stringResource(Res.string.timepoint_stage_all)) {
                QuadrantLabelledImage(
                    modifier = Modifier
                        .fillMaxHeight(0.87F)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium),
                    q1Label = TimepointHPF.TIMEPOINT_52HPF,
                    q2Label = TimepointHPF.TIMEPOINT_72HPF,
                    q3Label = TimepointHPF.TIMEPOINT_96HPF,
                    q4Label = TimepointHPF.TIMEPOINT_115HPF,
                    image = {
                        Image(
                            painter = painterResource(Res.drawable.dapi_all),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            } else {
                Image(
                    painter = painterResource(
                        when (selectedTimepoint) {
                            TimepointHPF.TIMEPOINT_52HPF -> Res.drawable.dapi_52hpf
                            TimepointHPF.TIMEPOINT_72HPF -> Res.drawable.dapi_72hpf
                            TimepointHPF.TIMEPOINT_96HPF -> Res.drawable.dapi_96hpf
                            TimepointHPF.TIMEPOINT_115HPF -> Res.drawable.dapi_115hpf
                            else -> Res.drawable.dapi_all
                        }
                    ),
                    contentDescription = "${timepointToStage[selectedTimepoint]} ($selectedTimepoint)",
                    modifier = Modifier.fillMaxHeight(0.87F)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.visualization_dapi_label),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                if (selectedTimepoint != stringResource(Res.string.timepoint_stage_all)) {
                    Text(
                        text = " ($selectedTimepoint)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight().padding(start = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.87F)
                    .aspectRatio(1F)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(0.5F)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTimepoint == stringResource(Res.string.timepoint_stage_all)) {
                    QuadrantLabelledImage(
                        modifier = Modifier
                            .fillMaxHeight(0.87F)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primary.copy(0.5F)),
                        q1Label = TimepointHPF.TIMEPOINT_52HPF,
                        q2Label = TimepointHPF.TIMEPOINT_72HPF,
                        q3Label = TimepointHPF.TIMEPOINT_96HPF,
                        q4Label = TimepointHPF.TIMEPOINT_115HPF,
                        image = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1F, fill = true)
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.visualization_52hpf),
                                        contentDescription = null,
                                        modifier = Modifier.weight(1F, fill = true)
                                    )
                                    Row(modifier = Modifier.height(15.dp)) {}
                                    Image(
                                        painter = painterResource(Res.drawable.visualization_96hpf),
                                        contentDescription = null,
                                        modifier = Modifier.weight(1F, fill = true)
                                    )
                                }
                                Row(modifier = Modifier.width(15.dp)) {}
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1F, fill = true)
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.visualization_72hpf),
                                        contentDescription = null,
                                        modifier = Modifier.weight(1F, fill = true)
                                    )
                                    Row(modifier = Modifier.height(15.dp)) {}
                                    Image(
                                        painter = painterResource(Res.drawable.visualization_115hpf),
                                        contentDescription = null,
                                        modifier = Modifier.weight(1F, fill = true)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    val vizRes = when (selectedTimepoint) {
                        TimepointHPF.TIMEPOINT_52HPF -> Res.drawable.visualization_52hpf
                        TimepointHPF.TIMEPOINT_72HPF -> Res.drawable.visualization_72hpf
                        TimepointHPF.TIMEPOINT_96HPF -> Res.drawable.visualization_96hpf
                        else -> Res.drawable.visualization_115hpf
                    }
                    val vizPainter = painterResource(vizRes)
                    Image(
                        painter = vizPainter,
                        contentDescription = "${timepointToStage[selectedTimepoint]} ($selectedTimepoint)",
                        modifier = Modifier.fillMaxSize(0.80F)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${timepointToStage[selectedTimepoint]}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}