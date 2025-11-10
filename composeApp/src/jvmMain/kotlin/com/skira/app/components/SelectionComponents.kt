package com.skira.app.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_arrow_down
import com.skira.app.composeapp.generated.resources.icon_search
import kotlin.math.floor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DropdownSelector(
    selectedItem: String,
    onSelectionChange: (String) -> Unit,
    availableItems: List<String>,
    modifier: Modifier = Modifier,
    searchable: Boolean = false,
) {
    val rowHeight = 36.dp
    val windowExtraRows = 20
    val maxMenuHeight = 320.dp

    var showingDropdown by remember { mutableStateOf(false) }
    var selectionItems by remember { mutableStateOf(availableItems) }
    val searchFocusRequester = remember { FocusRequester() }
    var query = rememberTextFieldState()

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val rowHeightPx = with(density) { rowHeight.toPx() }
    var columnSize by remember { mutableStateOf<IntSize?>(null) }

    val normalizedQuery: String by remember(query.text) {
        derivedStateOf { query.text.trim().toString().lowercase() }
    }
    val filtered by remember(selectionItems, normalizedQuery) {
        derivedStateOf {
            if (normalizedQuery.isEmpty()) selectionItems
            else selectionItems.filter { it.lowercase().contains(normalizedQuery) }
        }
    }

    LaunchedEffect(availableItems) {
        selectionItems = availableItems
        query = TextFieldState("")
    }

    // Fix for backspace key not working (focus the text field when it's showing)
    LaunchedEffect(showingDropdown) {
        if (showingDropdown && searchable && query.text.isNotEmpty()) {
            searchFocusRequester.requestFocus()
        }
    }


    Box(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { showingDropdown = !showingDropdown },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground.copy(0.7F)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.3F)
            ),
            modifier = Modifier.padding(top = 10.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedItem,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 5.dp)
                )
                Image(
                    painter = painterResource(Res.drawable.icon_arrow_down),
                    contentDescription = null
                )
            }
        }
        DropdownMenu(
            expanded = showingDropdown,
            onDismissRequest = { showingDropdown = false },
            properties = PopupProperties(usePlatformDefaultWidth = false, focusable = true),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(0.187F)
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxMenuHeight)
                        .onSizeChanged { size ->
                            columnSize = size
                        }
                ) {
                    if (searchable) {
                        Row(
                            modifier = Modifier.padding(10.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.onBackground.copy(0.1F))
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.icon_search),
                                contentDescription = null,
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp, bottom = 10.dp)
                            )
                            Box {
                                BasicTextField(
                                    state = query,
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    textStyle = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .focusRequester(searchFocusRequester)
                                )
                                if (query.text.isEmpty()) {
                                    Text(
                                        text = "Search...",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Manual virtualization state
                    // Estimate how many rows fit in the viewport, then add a buffer
                    val estimatedRowsInView by remember(maxMenuHeight, rowHeight) {
                        derivedStateOf {
                            val rows = (maxMenuHeight / rowHeight).toInt().coerceAtLeast(1)
                            rows
                        }
                    }
                    var firstIndex by remember(filtered, rowHeightPx) { mutableStateOf(0) }

                    // Update first visible index as user scrolls
                    LaunchedEffect(filtered, rowHeightPx) {
                        scrollState.scrollTo(0)
                    }
                    LaunchedEffect(scrollState, filtered, rowHeightPx) {
                        snapshotFlow { scrollState.value }
                            .map { value ->
                                if (rowHeightPx > 0f) floor(value / rowHeightPx).toInt() else 0
                            }
                            .distinctUntilChanged()
                            .collectLatest { idx ->
                                val maxStart = (filtered.size - 1).coerceAtLeast(0)
                                firstIndex = idx.coerceIn(0, maxStart)
                            }
                    }

                    val windowSize = (estimatedRowsInView + windowExtraRows).coerceAtLeast(estimatedRowsInView)
                    val start = firstIndex.coerceAtLeast(0)
                    val end = (start + windowSize).coerceAtMost(filtered.size)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxMenuHeight)
                            .verticalScroll(scrollState)
                    ) {
                        for (i in start until end) {
                            val item = filtered[i]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(rowHeight)
                                    .clickable {
                                        onSelectionChange(item)
                                        showingDropdown = false
                                    }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                columnSize?.let { size ->
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                            .height(with(LocalDensity.current) { size.height.toDp() })
                            .padding(top = 50.dp),
                        adapter = rememberScrollbarAdapter(scrollState),
                        style = ScrollbarStyle(
                            minimalHeight = 5.dp,
                            thickness = 3.dp,
                            shape = MaterialTheme.shapes.small,
                            hoverDurationMillis = 1,
                            unhoverColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                            hoverColor = MaterialTheme.colorScheme.onBackground.copy(0.2F)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoverTooltipImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var showTooltip by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .pointerMoveFilter(
                onEnter = {
                    showTooltip = true
                    true
                },
                onExit = {
                    showTooltip = false
                    true
                }
            )
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
        PopupText(
            text = contentDescription,
            show = showTooltip,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun PopupText(
    text: String?,
    show: Boolean,
    modifier: Modifier
) {
    AnimatedVisibility(
        visible = show && !text.isNullOrEmpty(),
        modifier = modifier
    ) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(focusable = false),
        ) {
            Surface(
                color = Color.Black,
                modifier = Modifier.clip(MaterialTheme.shapes.small)
            ) {
                Text(
                    text = text!!,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuadrantLabelledImage(
    image: @Composable () -> Unit,
    q1Label: String,
    q2Label: String,
    q3Label: String,
    q4Label: String,
    modifier: Modifier = Modifier
) {
    val hoveredQuadrant = remember { mutableStateOf<String?>(null) }
    val popupOffset = remember { mutableStateOf(Offset.Zero) }
    val boxSize = remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }
    val popupSize = remember { mutableStateOf(IntSize(0, 0)) }
    val popupHovered = remember { mutableStateOf(false) }
    val popupVisible = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val gapPx = with(density) { 10.dp.toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val interactiveModifier = Modifier
            .onPointerEvent(PointerEventType.Move) {
                val p1 = it.changes.first().position
                val width1 = boxSize.value.width.toFloat()
                val height1 = boxSize.value.height.toFloat()
                val midX1 = width1 / 2f
                val midY1 = height1 / 2f
                val leftBoundary1 = midX1 - gapPx / 2f
                val rightBoundary1 = midX1 + gapPx / 2f
                val topBoundary1 = midY1 - gapPx / 2f
                val bottomBoundary1 = midY1 + gapPx / 2f

                if (p1.x in leftBoundary1..rightBoundary1 || p1.y in topBoundary1..bottomBoundary1) {
                    if (popupVisible.value) popupVisible.value = false
                    return@onPointerEvent
                }
                val newName1 = when {
                    p1.x < leftBoundary1 && p1.y < topBoundary1 -> "Q1"
                    p1.x >= rightBoundary1 && p1.y < topBoundary1 -> "Q2"
                    p1.x < leftBoundary1 && p1.y >= bottomBoundary1 -> "Q3"
                    else -> "Q4"
                }
                val current1 = hoveredQuadrant.value
                if (current1 == newName1) {
                    return@onPointerEvent
                }
                val leftCenterX1 = midX1 / 2f
                val rightCenterX1 = midX1 + (width1 - midX1) / 2f
                val topCenterY1 = midY1 / 2f
                val bottomCenterY1 = midY1 + (height1 - midY1) / 2f
                val (centerX, centerY) = when (newName1) {
                    "Q1" -> Pair(leftCenterX1, topCenterY1)
                    "Q2" -> Pair(rightCenterX1, topCenterY1)
                    "Q3" -> Pair(leftCenterX1, bottomCenterY1)
                    else -> Pair(rightCenterX1, bottomCenterY1)
                }
                hoveredQuadrant.value = newName1
                popupOffset.value = Offset(centerX, centerY)
                popupVisible.value = true
            }
            .onPointerEvent(PointerEventType.Enter) { }
            .onPointerEvent(PointerEventType.Exit) {
                scope.launch {
                    delay(150)
                    if (!popupHovered.value) {
                        popupVisible.value = false
                        delay(200)
                        if (!popupHovered.value) {
                            hoveredQuadrant.value = null
                        }
                    }
                }
            }

        Box(modifier = interactiveModifier.onSizeChanged { boxSize.value = it }) {
            image()
            val q = hoveredQuadrant.value
            if (q != null) {
                val adjustedOffset = androidx.compose.ui.unit.IntOffset(
                    popupOffset.value.x.toInt() - popupSize.value.width / 2,
                    popupOffset.value.y.toInt() - popupSize.value.height / 2
                )
                AnimatedVisibility(visible = popupVisible.value) {
                    PopupText(
                        text = when (q) {
                            "Q1" -> q1Label
                            "Q2" -> q2Label
                            "Q3" -> q3Label
                            else -> q4Label
                        },
                        show = popupVisible.value,
                        modifier = Modifier.offset { adjustedOffset }
                    )
                }
            }
        }
    }
}