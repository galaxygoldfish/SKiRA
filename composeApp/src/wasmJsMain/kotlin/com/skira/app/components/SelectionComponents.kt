package com.skira.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import skira.composeapp.generated.resources.Res
import skira.composeapp.generated.resources.icon_arrow_down
import skira.composeapp.generated.resources.icon_check
import skira.composeapp.generated.resources.icon_search
import kotlin.math.floor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Checkbox(
    checkedState: Boolean,
    onCheckChange: (Boolean) -> Unit,
    size: Dp = 17.dp
) {
    Box(
        modifier = Modifier.size(size)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(if (checkedState) MaterialTheme.colorScheme.onBackground else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clickable(onClick = { onCheckChange(!checkedState) }),
        contentAlignment = Alignment.Center
    ) {
        Row {
            AnimatedVisibility(
                visible = checkedState,
                enter = fadeIn(animationSpec = tween(durationMillis = 100)),
                exit = fadeOut(animationSpec = tween(durationMillis = 100))
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                    modifier = Modifier.size(size - 7.dp)
                )
            }
        }
    }
}

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


    Box(modifier = modifier.width(300.dp)) {
        Button(
            onClick = { showingDropdown = !showingDropdown },
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onBackground.copy(0.7F)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
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
            shadowElevation = 0.dp,
            modifier = Modifier.width(300.dp)
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .heightIn(max = maxMenuHeight)
                        .onSizeChanged { size ->
                            columnSize = size
                        }
                ) {
                    if (searchable) {
                        Row(
                            modifier = Modifier.padding(10.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
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
                            .width(300.dp)
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