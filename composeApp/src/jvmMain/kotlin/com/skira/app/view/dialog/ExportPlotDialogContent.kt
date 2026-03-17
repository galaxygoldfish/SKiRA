package com.skira.app.view.dialog

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.traceEventStart
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.components.HoverAware
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_arrow_down
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.composeapp.generated.resources.icon_eyedropper
import com.skira.app.composeapp.generated.resources.plot_export_dialog_download
import com.skira.app.composeapp.generated.resources.plot_export_dialog_invert_colors
import com.skira.app.composeapp.generated.resources.plot_export_dialog_plot_title_label
import com.skira.app.composeapp.generated.resources.plot_export_dialog_section_colors
import com.skira.app.composeapp.generated.resources.plot_export_dialog_section_elements
import com.skira.app.composeapp.generated.resources.plot_export_dialog_show_axes
import com.skira.app.composeapp.generated.resources.plot_export_dialog_show_title
import com.skira.app.composeapp.generated.resources.plot_export_dialog_transparent_background
import com.skira.app.composeapp.generated.resources.plot_option_section_labels_expression
import com.skira.app.composeapp.generated.resources.timepoint_stage_52hpf
import com.skira.app.composeapp.generated.resources.timepoint_stage_72hpf
import com.skira.app.composeapp.generated.resources.timepoint_stage_96hpf
import com.skira.app.composeapp.generated.resources.timepoint_stage_115hpf
import com.skira.app.composeapp.generated.resources.timepoint_stage_all
import com.skira.app.structures.DownloadFormat
import com.skira.app.structures.TimepointHPF
import com.skira.app.utilities.parseHexToColor
import com.skira.app.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.Cursor
import javax.swing.JColorChooser

@Composable
fun ExportPlotDialogContent(onDismissRequest: () -> Unit) {
    val viewModel: HomeViewModel = viewModel()
    val exportingFeaturePlot = viewModel.exportDialogForFeaturePlot
    val sourceBitmap = if (exportingFeaturePlot) viewModel.plotBitmap else viewModel.dimPlotBitmap

    var transparentBackground by remember { mutableStateOf(true) }
    var showAxes by remember { mutableStateOf(true) }
    var showTitle by remember { mutableStateOf(true) }
    var invertOverlayColors by remember { mutableStateOf(false) }
    var selectedBackground by remember { mutableStateOf("white") }
    var customBackgroundHex by remember { mutableStateOf("#FFFFFF") }
    var selectedFormat by remember(viewModel.exportDialogPreferredFormat) {
        mutableStateOf(viewModel.exportDialogPreferredFormat)
    }
    var showFormatDropdown by remember { mutableStateOf(false) }
    var isRenderingPreview by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf(sourceBitmap) }
    var mainContentRowWidthPx by remember { mutableStateOf(0) }
    val availableFormats = remember { listOf(DownloadFormat.PNG, DownloadFormat.SVG, DownloadFormat.PDF) }
    val density = LocalDensity.current

    val timepointStageLabel = when (viewModel.selectedTimepoint) {
        TimepointHPF.TIMEPOINT_52HPF -> stringResource(Res.string.timepoint_stage_52hpf)
        TimepointHPF.TIMEPOINT_72HPF -> stringResource(Res.string.timepoint_stage_72hpf)
        TimepointHPF.TIMEPOINT_96HPF -> stringResource(Res.string.timepoint_stage_96hpf)
        TimepointHPF.TIMEPOINT_115HPF -> stringResource(Res.string.timepoint_stage_115hpf)
        TimepointHPF.TIMEPOINT_ALL -> stringResource(Res.string.timepoint_stage_all)
        else -> viewModel.selectedTimepoint
    }
    val titleText = if (exportingFeaturePlot) {
        "${viewModel.selectedGene} @ ${viewModel.selectedTimepoint}"
    } else {
        "Cell types / $timepointStageLabel"
    }
    var customTitle by remember(titleText) { mutableStateOf(titleText) }
    val downloadBaseName = if (exportingFeaturePlot) {
        "${viewModel.selectedGene}-${viewModel.selectedTimepoint}"
    } else {
        "cell-types-${viewModel.selectedTimepoint}"
    }
    val backgroundHex = when (selectedBackground) {
        "black" -> "#111111"
        "custom" -> customBackgroundHex
        else -> "#FFFFFF"
    }

    LaunchedEffect(
        sourceBitmap,
        transparentBackground,
        showAxes,
        showTitle,
        invertOverlayColors,
        backgroundHex,
        customTitle
    ) {
        isRenderingPreview = true
        previewBitmap = viewModel.buildClientExportPreview(
            image = sourceBitmap,
            showAxes = showAxes,
            showTitle = showTitle,
            transparentBackground = transparentBackground,
            backgroundHex = backgroundHex,
            titleText = customTitle,
            invertOverlayColors = invertOverlayColors,
            isFeaturePlot = exportingFeaturePlot
        )
        isRenderingPreview = false
    }

    LaunchedEffect(selectedFormat) {
        if (selectedFormat !in availableFormats) {
            selectedFormat = DownloadFormat.PNG
        }
        if (viewModel.exportDialogPreferredFormat != selectedFormat) {
            viewModel.exportDialogPreferredFormat = selectedFormat
        }
    }

    val controlsEnabled = !isRenderingPreview && sourceBitmap != null

    Column(
        modifier = Modifier.fillMaxHeight(0.8F)
    ) {
        Row(
            modifier = if (mainContentRowWidthPx > 0) {
                Modifier.width(with(density) { mainContentRowWidthPx.toDp() })
            } else {
                Modifier
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Export plot", style = MaterialTheme.typography.headlineLarge)
            MinimalIconButton(
                onClick = onDismissRequest,
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.icon_close),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                    )
                }
            )
        }

        Spacer(Modifier.height(30.dp))

        Text(
            text = stringResource(Res.string.plot_export_dialog_section_elements),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
        )
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .onSizeChanged { size ->
                    if (size.width > 0 && size.width != mainContentRowWidthPx) {
                        mainContentRowWidthPx = size.width
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 325.dp)
                    .alpha(if (controlsEnabled) 1f else 0.55f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(if (showAxes) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5))
                            .clickable(true) {
                                showAxes = !showAxes
                            }
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_export_dialog_show_axes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 15.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                        )
                        Switch(
                            checked = showAxes,
                            onCheckedChange = {
                                showAxes = it
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
                            .padding(top = 10.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (showTitle) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5))
                            .clickable(true) {
                                showTitle = !showTitle
                            }
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_export_dialog_show_title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 15.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                        )
                        Switch(
                            checked = showTitle,
                            onCheckedChange = {
                                showTitle = it
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
                    AnimatedVisibility(showTitle) {
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text(stringResource(Res.string.plot_export_dialog_plot_title_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 5.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                                disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                                focusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                                disabledLabelColor = MaterialTheme.colorScheme.onBackground.copy(0.3F),
                                cursorColor = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                            )
                        )
                    }
                    Text(
                        text = stringResource(Res.string.plot_export_dialog_section_colors),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                        modifier = Modifier.padding(top = 20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val selectedBlack = !transparentBackground && selectedBackground == "black"
                        val selectedWhite = !transparentBackground && selectedBackground == "white"
                        val selectedCustom = !transparentBackground && selectedBackground == "custom"
                        val customSwatchColor = parseHexToColor(customBackgroundHex).takeIf { it != Color.Unspecified }
                            ?: Color.White
                        val eyedropperColorFilter = if (invertOverlayColors) {
                            ColorFilter.colorMatrix(
                                ColorMatrix(
                                    floatArrayOf(
                                        -1f, 0f, 0f, 0f, 255f,
                                        0f, -1f, 0f, 0f, 255f,
                                        0f, 0f, -1f, 0f, 255f,
                                        0f, 0f, 0f, 1f, 0f
                                    )
                                )
                            )
                        } else {
                            null
                        }
                        Column(
                            modifier = Modifier.weight(1F)
                                .height(70.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    width = if (selectedBlack) 2.dp else 1.dp,
                                    color = if (selectedBlack) {
                                        MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(0.3F)
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable(!transparentBackground) {
                                    selectedBackground = "black"
                                }
                        ) {
                            Spacer(
                                Modifier.fillMaxSize()
                                    .padding(5.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(Color.Black.copy(if (transparentBackground) 0.3f else 1f))
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1F)
                                .height(70.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    width = if (selectedWhite) 2.dp else 1.dp,
                                    color = if (selectedWhite) {
                                        MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(0.3F)
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable(!transparentBackground) {
                                    selectedBackground = "white"
                                }
                        ) {
                            Spacer(
                                Modifier.fillMaxSize()
                                    .padding(5.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(Color.White.copy(if (transparentBackground) 0.3f else 1f))
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1F)
                                .height(70.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    width = if (selectedCustom) 2.dp else 1.dp,
                                    color = if (selectedCustom) {
                                        MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(0.3F)
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    pickColor(customBackgroundHex)?.let { picked ->
                                        customBackgroundHex = picked
                                    }
                                    selectedBackground = "custom"
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Spacer(
                                    Modifier.fillMaxSize()
                                        .padding(5.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(customSwatchColor.copy(if (transparentBackground) 0.3f else 1f))
                                )
                                Image(
                                    painter = painterResource(Res.drawable.icon_eyedropper),
                                    contentDescription = null,
                                    colorFilter = eyedropperColorFilter
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (transparentBackground) MaterialTheme.colorScheme.primary else Color(
                                    0XFFF3F4F5
                                )
                            )
                            .clickable(true) {
                                transparentBackground = !transparentBackground
                            }
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_export_dialog_transparent_background),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 15.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                        )
                        Switch(
                            checked = transparentBackground,
                            onCheckedChange = {
                                transparentBackground = it
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
                            .padding(top = 10.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (invertOverlayColors) MaterialTheme.colorScheme.primary else Color(0XFFF3F4F5))
                            .clickable(true) {
                                invertOverlayColors = !invertOverlayColors
                            }
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.plot_export_dialog_invert_colors),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 15.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                        )
                        Switch(
                            checked = invertOverlayColors,
                            onCheckedChange = {
                                invertOverlayColors = it
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
                }
                Column {
                    val downloadEnabled = controlsEnabled && previewBitmap != null
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBitmapToDownloadDirectory(
                                    image = previewBitmap,
                                    ext = selectedFormat,
                                    baseName = downloadBaseName,
                                    isFeaturePlot = exportingFeaturePlot,
                                    includeAxesOverlay = false
                                )
                                onDismissRequest()
                            },
                            enabled = downloadEnabled,
                            shape = RoundedCornerShape(8.dp, 5.dp, 5.dp, 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                                disabledContainerColor = Color.Black.copy(0.3f),
                                disabledContentColor = MaterialTheme.colorScheme.background.copy(0.3f)
                            ),
                            contentPadding = PaddingValues(horizontal = 15.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            ),
                            modifier = Modifier.weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "${stringResource(Res.string.plot_export_dialog_download)} ${selectedFormat.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Box {
                            Button(
                                onClick = { showFormatDropdown = true },
                                enabled = controlsEnabled,
                                shape = RoundedCornerShape(5.dp, 8.dp, 8.dp, 5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onBackground,
                                    disabledContainerColor = Color.Black.copy(0.3f),
                                    disabledContentColor = MaterialTheme.colorScheme.background.copy(0.3f)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp,
                                    focusedElevation = 0.dp,
                                    hoveredElevation = 0.dp
                                ),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.icon_arrow_down),
                                    contentDescription = "Select export format",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showFormatDropdown,
                                onDismissRequest = { showFormatDropdown = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                availableFormats.forEach { format ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = format.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedFormat = format
                                            showFormatDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(25.dp))
            Box(
                modifier = Modifier
                    .aspectRatio(1F)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.6F))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (transparentBackground) Modifier.subtleTransparencyCheckerboard() else Modifier)
                            .padding(15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = previewBitmap!!,
                            contentDescription = "Export preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Text(
                        text = "No plot available to export",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                    )
                }
                if (isRenderingPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.5.dp)
                    }
                }
            }
        }
    }
}

private fun Modifier.subtleTransparencyCheckerboard(): Modifier = drawWithCache {
    val tileSize = 14.dp.toPx()
    val light = Color.White.copy(alpha = 0.045f)
    val dark = Color.Black.copy(alpha = 0.025f)

    onDrawBehind {
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = 0f
            var column = 0
            while (x < size.width) {
                drawRect(
                    color = if ((row + column) % 2 == 0) light else dark,
                    topLeft = Offset(x, y),
                    size = Size(
                        width = minOf(tileSize, size.width - x),
                        height = minOf(tileSize, size.height - y)
                    )
                )
                x += tileSize
                column++
            }
            y += tileSize
            row++
        }
    }
}

private fun pickColor(initialHex: String): String? {
    val initialCompose = parseHexToColor(initialHex).takeIf { it != Color.Unspecified } ?: Color.White
    val initial = java.awt.Color(
        (initialCompose.red * 255f).toInt().coerceIn(0, 255),
        (initialCompose.green * 255f).toInt().coerceIn(0, 255),
        (initialCompose.blue * 255f).toInt().coerceIn(0, 255)
    )
    val chosen = JColorChooser.showDialog(null, "Pick color", initial)
    return chosen?.let { String.format("#%02X%02X%02X", it.red, it.green, it.blue) }
}

@Composable
private fun ColorChoiceButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        )
    ) {
        Text(label)
    }
}