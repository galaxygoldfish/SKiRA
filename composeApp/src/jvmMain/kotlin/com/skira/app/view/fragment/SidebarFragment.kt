package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.default_colormap
import com.skira.app.composeapp.generated.resources.featured_gene_list
import com.skira.app.composeapp.generated.resources.icon_add
import com.skira.app.composeapp.generated.resources.icon_arrow_back
import com.skira.app.composeapp.generated.resources.icon_check
import com.skira.app.composeapp.generated.resources.icon_close_panel
import com.skira.app.composeapp.generated.resources.icon_information
import com.skira.app.composeapp.generated.resources.icon_regenerate
import com.skira.app.composeapp.generated.resources.icon_search
import com.skira.app.composeapp.generated.resources.icon_trash
import com.skira.app.composeapp.generated.resources.inferno_colormap
import com.skira.app.composeapp.generated.resources.magma_colormap
import com.skira.app.composeapp.generated.resources.plasma_colormap
import com.skira.app.composeapp.generated.resources.plot_option_generate_button
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme_cell_type_timepoint
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme_cell_type_title
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme_cell_type_type
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme_expression_title
import com.skira.app.composeapp.generated.resources.plot_option_section_color_scheme_yours
import com.skira.app.composeapp.generated.resources.plot_option_section_density
import com.skira.app.composeapp.generated.resources.plot_option_section_density_cell_type
import com.skira.app.composeapp.generated.resources.plot_option_section_density_expression
import com.skira.app.composeapp.generated.resources.plot_option_section_density_verbose
import com.skira.app.composeapp.generated.resources.plot_option_section_labels
import com.skira.app.composeapp.generated.resources.plot_option_section_labels_cell_type
import com.skira.app.composeapp.generated.resources.plot_option_section_labels_disclaimer
import com.skira.app.composeapp.generated.resources.plot_option_section_labels_expression
import com.skira.app.composeapp.generated.resources.plot_option_section_labels_verbose
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_gene_featured
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_gene_selection
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_gene_title
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_gene_title_verbose
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_timepoint_title
import com.skira.app.composeapp.generated.resources.plot_option_section_selection_timepoint_verbose
import com.skira.app.structures.DialogType
import com.skira.app.structures.PlotColor
import com.skira.app.structures.PreferenceKey
import com.skira.app.structures.SidebarPage
import com.skira.app.structures.TimepointHPF
import com.skira.app.utilities.PreferenceManager
import com.skira.app.utilities.denormalizeToInt
import com.skira.app.utilities.normalizeValueToFloat
import com.skira.app.utilities.parseHexToColor
import com.skira.app.utilities.safeGradientColors
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SidebarFragment(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier.fillMaxHeight()
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .weight(1F, fill = true)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border((1.5).dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
        ) {

            AnimatedContent(viewModel.currentSidebarPage) { page ->
                when (page) {
                    SidebarPage.DEFAULT -> SidebarDefaultContent(viewModel)
                    SidebarPage.GENE -> SidebarGeneSelectorContent(viewModel)
                    SidebarPage.TIMEPOINT -> SidebarTimepointSelectorContent(viewModel)
                    SidebarPage.COLOR -> SidebarColorSelectorContent(viewModel)
                    SidebarPage.DENSITY -> SidebarDpiSelectorContent(viewModel)
                    SidebarPage.LABELS -> SidebarLabelToggleContent(viewModel)
                }
            }
        }
        val scope = rememberCoroutineScope()
        val selectionsComplete = viewModel.currentGene != "Select" && viewModel.currentTimepoint != "Select"
        val canClick = selectionsComplete && !viewModel.isLoadingPlot
        if (viewModel.currentSidebarPage == SidebarPage.DEFAULT) {
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
                shape = MaterialTheme.shapes.small,
                border = BorderStroke((1.5).dp, Color(0XFFDBE2E7)),
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.5F)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                enabled = canClick
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_regenerate),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 7.dp),
                        alpha = 0.7F
                    )
                    Text(
                        text = stringResource(Res.string.plot_option_generate_button),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarDefaultContent(viewModel: HomeViewModel) {
    Column {
        MinimalIconButton(
            onClick = {},
            icon = {
                Image(
                    painter = painterResource(Res.drawable.icon_close_panel),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
            },
            modifier = Modifier.padding(7.dp)
        )
        Button(
            onClick = { viewModel.currentSidebarPage = SidebarPage.GENE },
            shape = MaterialTheme.shapes.small,
            border = BorderStroke((1.25).dp, Color(0XFFE8E8E8)),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(start = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_selection_gene_title),
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = viewModel.currentGene,
                    modifier = Modifier.padding(end = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Button(
            onClick = { viewModel.currentSidebarPage = SidebarPage.TIMEPOINT },
            shape = MaterialTheme.shapes.small,
            border = BorderStroke((1.25).dp, Color(0XFFE8E8E8)),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(start = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_selection_timepoint_title),
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = viewModel.currentTimepoint,
                    modifier = Modifier.padding(end = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Button(
            onClick = { viewModel.currentSidebarPage = SidebarPage.COLOR },
            shape = MaterialTheme.shapes.small,
            border = BorderStroke((1.25).dp, Color(0XFFE8E8E8)),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(start = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_color_scheme),
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                    style = MaterialTheme.typography.bodyLarge
                )
                // Dynamic preview: show preset image for built-in schemes, or a gradient for custom schemes
                val exprColor = viewModel.currentExpressionPlotColor
                if (exprColor.startsWith("custom:")) {
                    val idx = exprColor.removePrefix("custom:").toIntOrNull()
                    val scheme = idx?.let { PreferenceManager.getColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES).getOrNull(it) }
                    val gradientColors = scheme?.mapNotNull { parseHexToColor(it).takeIf { c -> c != Color.Unspecified } } ?: listOf(Color.LightGray, Color.LightGray)
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(height = 20.dp, width = 35.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.2F),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Brush.horizontalGradient(safeGradientColors(gradientColors)))
                            .alpha(0.5F),
                        contentAlignment = Alignment.Center
                    ) {}
                } else {
                    val res = when (exprColor) {
                        PlotColor.Magma -> Res.drawable.magma_colormap
                        PlotColor.Plasma -> Res.drawable.plasma_colormap
                        PlotColor.Inferno -> Res.drawable.inferno_colormap
                        PlotColor.Viridis -> Res.drawable.default_colormap
                        else -> Res.drawable.default_colormap
                    }
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(height = 20.dp, width = 35.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.2F),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .clip(MaterialTheme.shapes.extraSmall)
                            .alpha(0.5F),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        Button(
            onClick = { viewModel.currentSidebarPage = SidebarPage.LABELS },
            shape = MaterialTheme.shapes.small,
            border = BorderStroke((1.25).dp, Color(0XFFE8E8E8)),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(start = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_labels),
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${if (viewModel.showDimPlotClusterLabels) "on" else "off"} / ${if (viewModel.showExpressionClusterLabels) "on" else "off"}",
                    modifier = Modifier.padding(end = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Button(
            onClick = { viewModel.currentSidebarPage = SidebarPage.DENSITY },
            shape = MaterialTheme.shapes.small,
            border = BorderStroke((1.25).dp, Color(0XFFE8E8E8)),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(start = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_density),
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${viewModel.cellTypePlotDpi} / ${viewModel.expressionPlotDpi}",
                    modifier = Modifier.padding(end = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SidebarGeneSelectorContent(viewModel: HomeViewModel) {
    val searchFocusRequester = remember { FocusRequester() }
    val query = rememberTextFieldState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MinimalIconButton(
                onClick = {
                    viewModel.currentSidebarPage = SidebarPage.DEFAULT
                },
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                modifier = Modifier.padding(7.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_selection_gene_title_verbose),
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
            )
        }

        val featuredGenes = stringArrayResource(Res.array.featured_gene_list)
        val normalizedQuery: String by remember(query.text) {
            derivedStateOf { query.text.trim().toString().lowercase() }
        }
        val filtered by remember(viewModel.metadataGeneList, normalizedQuery) {
            derivedStateOf {
                if (normalizedQuery.isEmpty()) viewModel.metadataGeneList
                else viewModel.metadataGeneList.filter { it.lowercase().contains(normalizedQuery) }
            }
        }
        Box(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = (1.5).dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.extraSmall
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_search),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.5F)),
                        modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp, bottom = 10.dp)
                            .size(20.dp)
                    )
                    Box {
                        BasicTextField(
                            state = query,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                                .focusRequester(searchFocusRequester)
                        )
                        if (query.text.isEmpty()) {
                            Text(
                                text = "Search 20,000+ genes",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                            )
                        }
                    }
                }
                Box {
                    if (query.text.isEmpty() && viewModel.currentGene !== "Select") {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = listState
                    ) {
                        item {
                            if (viewModel.currentGene != "Select" && query.text.isEmpty()) {
                                Text(
                                    text = stringResource(Res.string.plot_option_section_selection_gene_selection),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 15.dp, bottom = 5.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.currentGene = "Select"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small,
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.icon_check),
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 10.dp)
                                            .size(15.dp)
                                    )
                                    Text(
                                        text = viewModel.currentGene,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.65F),
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        if (query.text.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(Res.string.plot_option_section_selection_gene_featured),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 15.dp, bottom = 5.dp)
                                )
                            }
                        } else {
                            item { Spacer(Modifier.height(5.dp)) }
                        }
                        items(if (query.text.isEmpty()) featuredGenes else filtered) { gene ->
                            if (query.text.isEmpty() && gene != viewModel.currentGene || query.text.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        viewModel.currentGene = gene
                                        if (query.text.isEmpty()) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(0)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.currentGene == gene) {
                                            MaterialTheme.colorScheme.primary
                                        } else Color(0XFFF3F4F5)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                                ) {
                                    if (viewModel.currentGene == gene) {
                                        Image(
                                            painter = painterResource(Res.drawable.icon_check),
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 10.dp)
                                                .size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = gene,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.65F),
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(50.dp)) }
                    }
                    val isScrolled by remember(listState) {
                        derivedStateOf {
                            listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 1
                        }
                    }
                    if (isScrolled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainerLowest,
                                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun SidebarTimepointSelectorContent(viewModel: HomeViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MinimalIconButton(
                onClick = {
                    viewModel.currentSidebarPage = SidebarPage.DEFAULT
                },
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                modifier = Modifier.padding(7.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_selection_timepoint_verbose),
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
            )
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            listOf(
                TimepointHPF.TIMEPOINT_52HPF, TimepointHPF.TIMEPOINT_72HPF, TimepointHPF.TIMEPOINT_96HPF,
                TimepointHPF.TIMEPOINT_115HPF, TimepointHPF.TIMEPOINT_ALL
            ).forEach { timepoint ->
                val selected = viewModel.currentTimepoint == timepoint
                val color = animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5),
                    animationSpec = tween(200)
                )
                Button(
                    onClick = {
                        viewModel.currentTimepoint = timepoint
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = color.value),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    AnimatedVisibility(selected) {
                        Image(
                            painter = painterResource(Res.drawable.icon_check),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp)
                                .size(15.dp)
                        )
                    }
                    Text(
                        text = timepoint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.65F),
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarColorSelectorContent(viewModel: HomeViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MinimalIconButton(
                onClick = {
                    viewModel.currentSidebarPage = SidebarPage.DEFAULT
                },
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                modifier = Modifier.padding(7.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_color_scheme),
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
            )
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            val isDimPlotDisabled = viewModel.currentTimepoint != "All timepoints"
            val dimPlotAlpha = if (isDimPlotDisabled) 0.35F else 1F
            Row(modifier = Modifier.fillMaxWidth(0.85F).alpha(dimPlotAlpha)) {
                Text(
                    text = stringResource(Res.string.plot_option_section_color_scheme_cell_type_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            AnimatedContent(targetState = viewModel.currentDimPlotColor) { state ->
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .height(40.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .border(
                            width = (1.2).dp,
                            color = if (!isDimPlotDisabled) {
                                MaterialTheme.colorScheme.onBackground.copy(0.2F)
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
                            .padding(4.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isDimPlotDisabled
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_option_section_color_scheme_cell_type_type),
                            style = MaterialTheme.typography.bodyMedium
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
                            .padding(top = 4.dp, bottom = 4.dp, end = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isDimPlotDisabled
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_option_section_color_scheme_cell_type_timepoint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Text(
                text = stringResource(Res.string.plot_option_section_color_scheme_expression_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                modifier = Modifier.padding(top = 15.dp)
            )
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(
                        width = (1.2).dp,
                        color = if (viewModel.currentExpressionPlotColor == PlotColor.Magma) {
                            MaterialTheme.colorScheme.onBackground.copy(0.7F)
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(0.2F)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        viewModel.currentExpressionPlotColor = PlotColor.Magma
                    }
            ) {
                Image(
                    painter = painterResource(Res.drawable.magma_colormap),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(
                        width = (1.2).dp,
                        color = if (viewModel.currentExpressionPlotColor == PlotColor.Plasma) {
                            MaterialTheme.colorScheme.onBackground.copy(0.7F)
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(0.2F)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        viewModel.currentExpressionPlotColor = PlotColor.Plasma
                    }
            ) {
                Image(
                    painter = painterResource(Res.drawable.plasma_colormap),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(
                        width = (1.2).dp,
                        color = if (viewModel.currentExpressionPlotColor == PlotColor.Inferno) {
                            MaterialTheme.colorScheme.onBackground.copy(0.7F)
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(0.2F)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        viewModel.currentExpressionPlotColor = PlotColor.Inferno
                    }
            ) {
                Image(
                    painter = painterResource(Res.drawable.inferno_colormap),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(
                        width = (1.2).dp,
                        color = if (viewModel.currentExpressionPlotColor == PlotColor.Viridis) {
                            MaterialTheme.colorScheme.onBackground.copy(0.7F)
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(0.2F)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        viewModel.currentExpressionPlotColor = PlotColor.Viridis
                    }
            ) {
                Image(
                    painter = painterResource(Res.drawable.default_colormap),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = stringResource(Res.string.plot_option_section_color_scheme_yours),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                modifier = Modifier.padding(top = 15.dp)
            )
            val borderColor = MaterialTheme.colorScheme.onBackground.copy(0.25F)
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = borderColor,
                            style = Stroke(
                                width = 1.2.dp.toPx(),
                                pathEffect = dashPathEffect(
                                    intervals = floatArrayOf(10f, 10f),
                                    phase = 0f
                                )
                            ),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable {
                        viewModel.currentDialogToShow = DialogType.COLOR_CREATION
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painterResource(Res.drawable.icon_add),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                            .size(25.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.7F))
                    )
                }
            }
            val savedSchemes by remember { PreferenceManager.colorSchemesState }
            Column(modifier = Modifier.padding(top = 10.dp)) {
                savedSchemes.forEachIndexed { schemeIndex, scheme ->
                    val isSelected = viewModel.currentExpressionPlotColor == "custom:$schemeIndex"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(70.dp)
                            .clip(MaterialTheme.shapes.small)
                            .border(
                                width = (1.2).dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                } else {
                                    MaterialTheme.colorScheme.onBackground.copy(0.2F)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { viewModel.currentExpressionPlotColor = "custom:$schemeIndex" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(7.dp)
                                .fillMaxHeight()
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(Brush.horizontalGradient(safeGradientColors(scheme.map { parseHexToColor(it) })))
                        ) {
                            if (isSelected) {
                                Image(
                                    painter = painterResource(Res.drawable.icon_check),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.8F))
                                )
                            }
                        }

                        MinimalIconButton(
                            onClick = {
                                PreferenceManager.removeColorScheme(PreferenceKey.CUSTOM_COLOR_SCHEMES, schemeIndex)
                            },
                            icon = {
                                Image(
                                    painter = painterResource(Res.drawable.icon_trash),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.4F))
                                )
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarLabelToggleContent(viewModel: HomeViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MinimalIconButton(
                onClick = {
                    viewModel.currentSidebarPage = SidebarPage.DEFAULT
                },
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                modifier = Modifier.padding(7.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_labels_verbose),
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (viewModel.showDimPlotClusterLabels) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5))
                .clickable(true) {
                    viewModel.showDimPlotClusterLabels = !viewModel.showDimPlotClusterLabels
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.plot_option_section_labels_cell_type),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 15.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
            )
            Switch(
                checked = viewModel.showDimPlotClusterLabels,
                onCheckedChange = {
                    viewModel.showDimPlotClusterLabels = it
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.15F),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(0.7F),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                    uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    checkedBorderColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.padding(end = 15.dp, top = 5.dp, bottom = 5.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (viewModel.showExpressionClusterLabels) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5))
                .clickable(true) {
                    viewModel.showExpressionClusterLabels = !viewModel.showExpressionClusterLabels
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.plot_option_section_labels_expression),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 15.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
            )
            Switch(
                checked = viewModel.showExpressionClusterLabels,
                onCheckedChange = {
                    viewModel.showExpressionClusterLabels = it
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.15F),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(0.7F),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                    uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    checkedBorderColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.padding(end = 15.dp, top = 5.dp, bottom = 5.dp)
            )
        }
        Row {
            Image(
                painter = painterResource(Res.drawable.icon_information),
                contentDescription = null,
                modifier = Modifier.padding(start = 14.dp, top = 20.dp)
                    .size(13.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_labels_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 5.dp, top = 17.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
            )
        }
    }
}

@Composable
fun SidebarDpiSelectorContent(viewModel: HomeViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            MinimalIconButton(
                onClick = {
                    viewModel.currentSidebarPage = SidebarPage.DEFAULT
                },
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                modifier = Modifier.padding(7.dp)
            )
            Text(
                text = stringResource(Res.string.plot_option_section_density_verbose),
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(0XFFF3F4F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_density_cell_type),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                )
                Text(
                    text = " ${viewModel.cellTypePlotDpi} DPI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
            }
            Slider(
                value = viewModel.cellTypePlotDpi.normalizeValueToFloat(100, 250),
                onValueChange = {
                    viewModel.cellTypePlotDpi = it.denormalizeToInt(100, 250)
                },
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                steps = ((250 - 100) / 10) - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                    inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    activeTickColor = Color.Transparent
                )
            )
        }
        Column(modifier = Modifier.fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0XFFF3F4F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.plot_option_section_density_expression),
                    style = MaterialTheme.typography.bodyLarge,
                     color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                )
                Text(
                    text = " ${viewModel.expressionPlotDpi} DPI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
            }
            Slider(
                value = viewModel.expressionPlotDpi.normalizeValueToFloat(100, 250),
                onValueChange = {
                    viewModel.expressionPlotDpi = it.denormalizeToInt(100, 250)
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(10.dp),
                steps = ((250 - 100) / 10) - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                    inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    activeTickColor = Color.Transparent
                )
            )
        }
    }
}
