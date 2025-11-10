package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.skira.app.components.DropdownSelector
import com.skira.app.components.ExpansionMenuButton
import com.skira.app.components.ShimmerPlaceholder
import com.skira.app.viewmodel.HomeViewModel
import com.skira.app.structures.PlotColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.default_colormap
import com.skira.app.composeapp.generated.resources.inferno_colormap
import com.skira.app.composeapp.generated.resources.magma_colormap
import com.skira.app.composeapp.generated.resources.plasma_colormap
import com.skira.app.composeapp.generated.resources.plot_option_generate_button
import com.skira.app.structures.PlotOptionSection
import com.skira.app.utilities.denormalizeToInt
import com.skira.app.utilities.normalizeValueToFloat
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotOptionFragment(viewModel: HomeViewModel) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    AnimatedContent(
        targetState = !viewModel.isLoadingMeta
                && viewModel.metadataTimepointList.isNotEmpty()
                && viewModel.metadataGeneList.isNotEmpty()
    ) { doneLoading ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (doneLoading) {

                var containerHeightPx by remember { mutableStateOf(0) }
                var contentHeightPx by remember { mutableStateOf(0) }
                var lastExpandedContentHeightPx by remember { mutableStateOf(0) }
                val hysteresisPx = remember { 50 }
                var contentExceedsHeight by remember { mutableStateOf(false) }

                LaunchedEffect(containerHeightPx, contentHeightPx) {
                    if (!contentExceedsHeight) {
                        if (contentHeightPx > containerHeightPx) {
                            lastExpandedContentHeightPx = contentHeightPx
                            contentExceedsHeight = true
                        }
                    } else {
                        val canFitExpanded = containerHeightPx >= (lastExpandedContentHeightPx - hysteresisPx)
                        if (canFitExpanded) {
                            contentExceedsHeight = false
                        } else {
                            if (contentHeightPx > lastExpandedContentHeightPx) {
                                lastExpandedContentHeightPx = contentHeightPx
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95F)
                        .onSizeChanged { containerHeightPx = it.height }
                ) {
                    val expandedSections = remember { mutableStateListOf<Int>() }

                    val toggleSection: (Int) -> Unit = { id ->
                        if (expandedSections.contains(id)) {
                            expandedSections.remove(id)
                        } else {
                            expandedSections.add(id)
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.onSizeChanged { contentHeightPx = it.height }
                        ) {
                            if (contentExceedsHeight) {
                                ExpansionMenuButton(
                                    onClick = { toggleSection(PlotOptionSection.SELECTION) },
                                    text = "Selection",
                                    modifier = Modifier.fillMaxWidth(),
                                    sectionExpanded = expandedSections.contains(PlotOptionSection.SELECTION),
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = "Selection",
                                        modifier = Modifier.padding(start = 15.dp, top = 7.dp, bottom = 7.dp),
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                    )
                                }
                            }
                            AnimatedVisibility(!contentExceedsHeight || expandedSections.contains(PlotOptionSection.SELECTION)) {
                                Column(
                                    modifier = Modifier
                                        .padding(bottom = if (contentExceedsHeight) 10.dp else 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.85F),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = 25.dp)
                                        ) {
                                            Text(
                                                text = "Timepoint",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Text(
                                                text = "(hours post-fertilization)",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                                modifier = Modifier.padding(start = 5.dp)
                                            )
                                        }
                                        Box {
                                            DropdownSelector(
                                                selectedItem = viewModel.currentTimepoint,
                                                onSelectionChange = {
                                                    viewModel.currentTimepoint = it
                                                    if (it != "All timepoints" && viewModel.currentDimPlotColor == 1) {
                                                        viewModel.currentDimPlotColor = 0
                                                    }
                                                },
                                                availableItems = viewModel.metadataTimepointList
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.85F),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Gene",
                                                style = MaterialTheme.typography.headlineMedium,
                                                modifier = Modifier.padding(top = 10.dp)
                                            )
                                        }
                                        Box {
                                            DropdownSelector(
                                                selectedItem = viewModel.currentGene,
                                                onSelectionChange = {
                                                    viewModel.currentGene = it
                                                },
                                                availableItems = viewModel.metadataGeneList,
                                                searchable = true
                                            )
                                        }
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.padding(top = if (contentExceedsHeight) 15.dp else 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (contentExceedsHeight) {
                                    ExpansionMenuButton(
                                        onClick = { toggleSection(PlotOptionSection.COLORS) },
                                        text = "Color scheme",
                                        modifier = Modifier.fillMaxWidth(),
                                        sectionExpanded = expandedSections.contains(PlotOptionSection.COLORS),
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(
                                            text = "Color scheme",
                                            modifier = Modifier.padding(start = 15.dp, top = 7.dp, bottom = 7.dp),
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                        )
                                    }
                                }
                                AnimatedVisibility(!contentExceedsHeight || expandedSections.contains(PlotOptionSection.COLORS)) {
                                    Column(
                                        modifier = Modifier
                                            .padding(bottom = if (contentExceedsHeight) 10.dp else 0.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(0.85F)) {
                                            Text(
                                                text = "Expression plot colormap",
                                                style = MaterialTheme.typography.headlineMedium,
                                                modifier = Modifier.padding(top = 20.dp)
                                            )
                                        }
                                        AnimatedContent(targetState = viewModel.currentExpressionPlotColor) { state ->
                                            Column(modifier = Modifier.padding(top = 15.dp).fillMaxWidth(0.85F)) {
                                                Row {
                                                    Column(
                                                        modifier = Modifier
                                                            .clip(MaterialTheme.shapes.small)
                                                            .border(
                                                                width = (2).dp,
                                                                color = if (state == PlotColor.Plasma) {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                                                } else {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.2F)
                                                                },
                                                                shape = MaterialTheme.shapes.small
                                                            )
                                                            .weight(1F, fill = true)
                                                            .height(50.dp)
                                                            .clickable {
                                                                viewModel.currentExpressionPlotColor = PlotColor.Plasma
                                                            }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(Res.drawable.plasma_colormap),
                                                            contentDescription = "Plasma colormap",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp)
                                                                .clip(MaterialTheme.shapes.extraSmall),
                                                            contentScale = ContentScale.FillBounds
                                                        )
                                                    }
                                                    Row(modifier = Modifier.width(10.dp)) { }
                                                    Column(
                                                        modifier = Modifier
                                                            .clip(MaterialTheme.shapes.small)
                                                            .border(
                                                                width = (2).dp,
                                                                color = if (state == PlotColor.Inferno) {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                                                } else {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.2F)
                                                                },
                                                                shape = MaterialTheme.shapes.small
                                                            )
                                                            .weight(1F, fill = true)
                                                            .height(50.dp)
                                                            .clickable {
                                                                viewModel.currentExpressionPlotColor = PlotColor.Inferno
                                                            }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(Res.drawable.inferno_colormap),
                                                            contentDescription = "Inferno colormap",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp)
                                                                .clip(MaterialTheme.shapes.extraSmall),
                                                            contentScale = ContentScale.FillBounds
                                                        )
                                                    }
                                                }
                                                Row(modifier = Modifier.padding(top = 10.dp)) {
                                                    Column(
                                                        modifier = Modifier
                                                            .clip(MaterialTheme.shapes.small)
                                                            .border(
                                                                width = (2).dp,
                                                                color = if (state == PlotColor.Viridis) {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                                                } else {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.2F)
                                                                },
                                                                shape = MaterialTheme.shapes.small
                                                            )
                                                            .weight(1F, fill = true)
                                                            .height(50.dp)
                                                            .clickable {
                                                                viewModel.currentExpressionPlotColor = PlotColor.Viridis
                                                            }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(Res.drawable.default_colormap),
                                                            contentDescription = "Viridis colormap",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp)
                                                                .clip(MaterialTheme.shapes.extraSmall),
                                                            contentScale = ContentScale.FillBounds
                                                        )
                                                    }
                                                    Row(modifier = Modifier.width(10.dp)) { }
                                                    Column(
                                                        modifier = Modifier
                                                            .clip(MaterialTheme.shapes.extraSmall)
                                                            .border(
                                                                width = (2).dp,
                                                                color = if (state == PlotColor.Magma) {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                                                } else {
                                                                    MaterialTheme.colorScheme.onBackground.copy(0.2F)
                                                                },
                                                                shape = MaterialTheme.shapes.small
                                                            )
                                                            .weight(1F, fill = true)
                                                            .height(50.dp)
                                                            .clickable {
                                                                viewModel.currentExpressionPlotColor = PlotColor.Magma
                                                            }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(Res.drawable.magma_colormap),
                                                            contentDescription = "Magma colormap",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp)
                                                                .clip(MaterialTheme.shapes.extraSmall),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        val isDimPlotDisabled = viewModel.currentTimepoint != "All timepoints"
                                        val dimPlotAlpha = if (isDimPlotDisabled) 0.35F else 1F

                                        Row(modifier = Modifier.fillMaxWidth(0.85F).alpha(dimPlotAlpha)) {
                                            Text(
                                                text = "Color cell clusters by",
                                                style = MaterialTheme.typography.headlineMedium,
                                                modifier = Modifier.padding(top = 20.dp)
                                            )
                                        }
                                        AnimatedContent(targetState = viewModel.currentDimPlotColor) { state ->
                                            Row(
                                                modifier = Modifier
                                                    .padding(top = 15.dp)
                                                    .height(40.dp)
                                                    .fillMaxWidth(0.85F)
                                                    .clip(MaterialTheme.shapes.small)
                                                    .border(
                                                        width = (1.5).dp,
                                                        color = if (!isDimPlotDisabled) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.3F)
                                                        } else {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.1F)
                                                        },
                                                        shape = MaterialTheme.shapes.small
                                                    )
                                                    .alpha(dimPlotAlpha)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.currentDimPlotColor = 0 },
                                                    shape = MaterialTheme.shapes.extraSmall,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (state == 0) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.1F)
                                                        } else Color.Transparent,
                                                        contentColor = if (state == 0) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                                        } else {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                                        },
                                                        disabledContainerColor = if (state == 0) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.1F)
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth(0.5F)
                                                        .fillMaxHeight()
                                                        .padding(5.dp),
                                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                                                    contentPadding = PaddingValues(0.dp),
                                                    enabled = !isDimPlotDisabled
                                                ) {
                                                    Text(
                                                        text = "Cell type",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                Button(
                                                    onClick = { viewModel.currentDimPlotColor = 1 },
                                                    shape = MaterialTheme.shapes.extraSmall,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (state == 1) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.1F)
                                                        } else Color.Transparent,
                                                        contentColor = if (state == 1) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                                        } else {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                                        },
                                                        disabledContainerColor = if (state == 1) {
                                                            MaterialTheme.colorScheme.onBackground.copy(0.1F)
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth(1F)
                                                        .fillMaxHeight()
                                                        .padding(top = 5.dp, bottom = 5.dp, end = 5.dp),
                                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                                                    contentPadding = PaddingValues(0.dp),
                                                    enabled = !isDimPlotDisabled
                                                ) {
                                                    Text(
                                                        text = "Timepoint",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.padding(top = if (contentExceedsHeight) 15.dp else 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (contentExceedsHeight) {
                                    ExpansionMenuButton(
                                        onClick = { toggleSection(PlotOptionSection.LABELS) },
                                        text = "Cluster labels",
                                        modifier = Modifier.fillMaxWidth(),
                                        sectionExpanded = expandedSections.contains(PlotOptionSection.LABELS),
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(
                                            text = "Cluster labels",
                                            modifier = Modifier.padding(start = 15.dp, top = 7.dp, bottom = 7.dp),
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                        )
                                    }
                                }
                                AnimatedVisibility(!contentExceedsHeight || expandedSections.contains(PlotOptionSection.LABELS)) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = if (contentExceedsHeight) 10.dp else 0.dp)
                                                .clip(MaterialTheme.shapes.extraSmall)
                                                .clickable {
                                                    viewModel.showExpressionClusterLabels =
                                                        !viewModel.showExpressionClusterLabels
                                                },
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(0.85F)
                                                    .padding(vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Expression plot labels",
                                                    style = MaterialTheme.typography.headlineMedium
                                                )
                                                Switch(
                                                    checked = viewModel.showExpressionClusterLabels,
                                                    onCheckedChange = {
                                                        viewModel.showExpressionClusterLabels = it
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        ),
                                                        uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.1F
                                                        ),
                                                        checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        ),
                                                        uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.05F
                                                        ),
                                                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.1F
                                                        ),
                                                        checkedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clip(MaterialTheme.shapes.extraSmall)
                                                .clickable {
                                                    viewModel.showDimPlotClusterLabels =
                                                        !viewModel.showDimPlotClusterLabels
                                                },
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(0.85F)
                                                    .padding(vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Cell type plot labels",
                                                    style = MaterialTheme.typography.headlineMedium
                                                )
                                                Switch(
                                                    checked = viewModel.showDimPlotClusterLabels,
                                                    onCheckedChange = {
                                                        viewModel.showDimPlotClusterLabels = it
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        ),
                                                        uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.1F
                                                        ),
                                                        checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        ),
                                                        uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.05F
                                                        ),
                                                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.1F
                                                        ),
                                                        checkedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.2F
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.padding(top = if (contentExceedsHeight) 15.dp else 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (contentExceedsHeight) {
                                    ExpansionMenuButton(
                                        onClick = { toggleSection(PlotOptionSection.SCALE_SIZE) },
                                        text = "Plot density",
                                        modifier = Modifier.fillMaxWidth(),
                                        sectionExpanded = expandedSections.contains(PlotOptionSection.SCALE_SIZE),
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(
                                            text = "Plot density",
                                            modifier = Modifier.padding(start = 15.dp, top = 7.dp, bottom = 7.dp),
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                        )
                                    }
                                }
                                AnimatedVisibility(!contentExceedsHeight || expandedSections.contains(PlotOptionSection.SCALE_SIZE)) {
                                    Column(
                                        modifier = Modifier
                                            .padding(bottom = if (contentExceedsHeight) 10.dp else 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.85F)
                                                .padding(top = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Gene expression",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Text(
                                                text = " ${viewModel.expressionPlotDpi} DPI",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                            )
                                        }
                                        Slider(
                                            value = viewModel.expressionPlotDpi.normalizeValueToFloat(100, 250),
                                            onValueChange = {
                                                viewModel.expressionPlotDpi = it.denormalizeToInt(100, 250)
                                            },
                                            modifier = Modifier.fillMaxWidth(0.85F)
                                                .padding(top = 5.dp),
                                            steps = ((250 - 100) / 10) - 1,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.onBackground.copy(0.25F),
                                                activeTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.25F),
                                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                                activeTickColor = Color.Transparent
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.85F)
                                                .padding(top = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Cell types",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Text(
                                                text = " ${viewModel.cellTypePlotDpi} DPI",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                            )
                                        }
                                        Slider(
                                            value = viewModel.cellTypePlotDpi.normalizeValueToFloat(100, 250),
                                            onValueChange = {
                                                viewModel.cellTypePlotDpi = it.denormalizeToInt(100, 250)
                                            },
                                            modifier = Modifier.fillMaxWidth(0.85F)
                                                .padding(top = 5.dp),
                                            steps = ((250 - 100) / 10) - 1,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.onBackground.copy(0.25F),
                                                activeTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.25F),
                                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                                activeTickColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // fading edge at bottom of scrollable content
                    val showFading = scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .align(Alignment.BottomCenter)
                            .alpha(if (showFading) 1f else 0f)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                }

                val selectionsComplete = viewModel.currentGene != "Select" && viewModel.currentTimepoint != "Select"
                val canClick = selectionsComplete && !viewModel.isLoadingPlot
                AnimatedContent(
                    targetState = canClick,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.startPlotJob(
                                        gene = viewModel.currentGene,
                                        timepoint = viewModel.currentTimepoint,
                                        expressionDpiParam = viewModel.expressionPlotDpi,
                                        cellTypeDpiParam = viewModel.cellTypePlotDpi,
                                        expressionColorParam = viewModel.currentExpressionPlotColor,
                                        dimColorByParam = viewModel.currentDimPlotColor,
                                        showDimLabelsParam = viewModel.showDimPlotClusterLabels,
                                        showExpressionLabelsParam = viewModel.showExpressionClusterLabels
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.extraSmall,
                            enabled = it,
                            colors = if (it) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(0.6F),
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                        0.5f
                                    )
                                )
                            },
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.plot_option_generate_button),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column {
                        ShimmerPlaceholder(
                            modifier = Modifier.padding(bottom = 15.dp)
                                .height(50.dp)
                                .fillMaxWidth()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85F),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.padding(bottom = 15.dp)
                                        .height(20.dp)
                                        .fillMaxWidth(0.33F)
                                )
                            }
                            ShimmerPlaceholder(
                                modifier = Modifier.padding(bottom = 15.dp)
                                    .height(30.dp)
                                    .fillMaxWidth(0.85F)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85F),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.padding(bottom = 15.dp)
                                        .height(20.dp)
                                        .fillMaxWidth(0.33F)
                                )
                            }
                            ShimmerPlaceholder(
                                modifier = Modifier.padding(bottom = 50.dp)
                                    .height(50.dp)
                                    .fillMaxWidth(0.85F)
                            )
                        }

                        ShimmerPlaceholder(
                            modifier = Modifier.padding(bottom = 15.dp)
                                .height(50.dp)
                                .fillMaxWidth()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85F),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.padding(bottom = 15.dp)
                                        .height(20.dp)
                                        .fillMaxWidth(0.33F)
                                )
                            }
                            ShimmerPlaceholder(
                                modifier = Modifier.padding(bottom = 50.dp)
                                    .height(100.dp)
                                    .fillMaxWidth(0.85F)
                            )
                        }

                        ShimmerPlaceholder(
                            modifier = Modifier.padding(bottom = 15.dp)
                                .height(50.dp)
                                .fillMaxWidth()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85F),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.padding(bottom = 15.dp)
                                        .height(20.dp)
                                        .fillMaxWidth(0.33F)
                                )
                            }
                            ShimmerPlaceholder(
                                modifier = Modifier.padding(bottom = 15.dp)
                                    .height(30.dp)
                                    .fillMaxWidth(0.85F)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85F),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.padding(bottom = 15.dp)
                                        .height(20.dp)
                                        .fillMaxWidth(0.33F)
                                )
                            }
                            ShimmerPlaceholder(
                                modifier = Modifier.padding(bottom = 15.dp)
                                    .height(30.dp)
                                    .fillMaxWidth(0.85F)
                            )
                        }
                    }
                    ShimmerPlaceholder(
                        modifier = Modifier.padding(top = 40.dp)
                            .height(40.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}