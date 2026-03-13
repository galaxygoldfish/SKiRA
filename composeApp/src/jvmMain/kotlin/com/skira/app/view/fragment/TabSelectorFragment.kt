package com.skira.app.view.fragment

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skira.app.components.HoverAware
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_add
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.Cursor
import kotlin.math.roundToInt

@Composable
fun TabSelectorFragment(viewModel: HomeViewModel) {
    var draggedTabId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    val tabWidthsPx = remember { mutableStateMapOf<Long, Float>() }

    val tabRowHeight = 45.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(tabRowHeight)
            .padding(top = 10.dp, end = 10.dp, start = 10.dp)
    ) {
        val tabCount = (viewModel.tabEntryList.size).coerceAtLeast(1)
        val addButtonWidth = 37.dp
        val spacing = 5.dp
        val effectiveCount = if (tabCount <= 6) tabCount else 6
        val totalSpacing = spacing * (effectiveCount)
        val lazyRowState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val isScrollable = remember { derivedStateOf { viewModel.tabEntryList.size > 6 } }
        val showLeft by remember {
            derivedStateOf { isScrollable.value && (lazyRowState.firstVisibleItemIndex > 0 || lazyRowState.firstVisibleItemScrollOffset > 0) }
        }
        val showRight by remember {
            derivedStateOf {
                if (!isScrollable.value) return@derivedStateOf false
                val info = lazyRowState.layoutInfo
                val visible = info.visibleItemsInfo
                val total = info.totalItemsCount
                if (visible.isEmpty() || total <= 0) {
                    false
                } else {
                    val lastVisible = visible.last().index
                    (lastVisible < total - 1) || (lastVisible == total - 1 && lazyRowState.layoutInfo.visibleItemsInfo.last().offset < lazyRowState.layoutInfo.viewportSize.width - maxWidth.value / 6)
                }
            }
        }
        val edgeWidth = 35.dp
        Row {
            Box(modifier = Modifier.weight(1f).height(tabRowHeight)) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val tabsAreaWidth = this.maxWidth
                    val tabWidth = if (effectiveCount > 0) (tabsAreaWidth - totalSpacing) / effectiveCount else tabsAreaWidth - 5.dp
                    val animatedTabWidth by animateDpAsState(
                        targetValue = tabWidth,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tab-width"
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 5.dp)
                            .height(tabRowHeight)
                            .animateContentSize(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        state = lazyRowState
                    ) {
                        itemsIndexed(viewModel.tabEntryList, key = { _, item -> item.id }) { index, item ->
                            val isSelected = viewModel.currentTabInView == index
                            val isDragging = draggedTabId == item.id
                            HoverAware { isHovered, interactionSource ->
                                Button(
                                    onClick = {
                                        viewModel.onSwitchTab(index)
                                    },
                                    interactionSource = interactionSource,
                                    border = BorderStroke(
                                        width = (1.5.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.outline
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(0.5F)
                                        }
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) {
                                            if (isHovered) {
                                                MaterialTheme.colorScheme.outline.copy(0.4F)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerLowest
                                            }
                                        } else {
                                            if (isHovered) {
                                                MaterialTheme.colorScheme.outline.copy(0.3F)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.7F)
                                            }
                                        }
                                    ),
                                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                                    elevation = buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                                    modifier = Modifier
                                        .width(animatedTabWidth)
                                        .height(tabRowHeight)
                                        .onGloballyPositioned { coordinates ->
                                            tabWidthsPx[item.id] = coordinates.size.width.toFloat()
                                        }
                                        .offset {
                                            if (isDragging) IntOffset(dragOffsetX.roundToInt(), 0) else IntOffset.Zero
                                        }
                                        .pointerInput(item.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedTabId = item.id
                                                    dragOffsetX = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    if (draggedTabId != item.id) return@detectDragGesturesAfterLongPress
                                                    change.consume()
                                                    dragOffsetX += dragAmount.x

                                                    val currentIndex = viewModel.tabEntryList.indexOfFirst { it.id == item.id }
                                                    if (currentIndex < 0) return@detectDragGesturesAfterLongPress

                                                    val width = tabWidthsPx[item.id] ?: return@detectDragGesturesAfterLongPress
                                                    val threshold = width * 0.5f

                                                    if (dragOffsetX > threshold && currentIndex < viewModel.tabEntryList.lastIndex) {
                                                        val rightId = viewModel.tabEntryList[currentIndex + 1].id
                                                        val rightWidth = tabWidthsPx[rightId] ?: width
                                                        viewModel.moveTab(currentIndex, currentIndex + 1)
                                                        dragOffsetX -= rightWidth
                                                    } else if (dragOffsetX < -threshold && currentIndex > 0) {
                                                        val leftId = viewModel.tabEntryList[currentIndex - 1].id
                                                        val leftWidth = tabWidthsPx[leftId] ?: width
                                                        viewModel.moveTab(currentIndex, currentIndex - 1)
                                                        dragOffsetX += leftWidth
                                                    }
                                                },
                                                onDragEnd = {
                                                    if (draggedTabId == item.id) {
                                                        draggedTabId = null
                                                        dragOffsetX = 0f
                                                    }
                                                },
                                                onDragCancel = {
                                                    if (draggedTabId == item.id) {
                                                        draggedTabId = null
                                                        dragOffsetX = 0f
                                                    }
                                                }
                                            )
                                        }
                                        .animateContentSize()
                                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        BasicTextField(
                                            value = item.title,
                                            onValueChange = { newValue ->
                                                viewModel.updateTabTitleAt(index, newValue)
                                            },
                                            singleLine = true,
                                            modifier = Modifier.align(Alignment.Center).padding(start = 20.dp),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                                    + TextStyle(
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                                } else {
                                                    MaterialTheme.colorScheme.onBackground.copy(0.4F)
                                                }
                                            ),
                                            enabled = isSelected
                                        )

                                        if (viewModel.tabEntryList.size > 1) {
                                            MinimalIconButton(
                                                onClick = {
                                                    viewModel.removeTabById(item.id)
                                                },
                                                icon = {
                                                    Image(
                                                        painter = painterResource(Res.drawable.icon_close),
                                                        contentDescription = null,
                                                        colorFilter = ColorFilter.tint(
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.onBackground.copy(0.9F)
                                                            } else {
                                                                MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                                            }
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (showLeft) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(edgeWidth)
                                .align(Alignment.CenterStart)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainerLowest,
                                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f)
                                        )
                                    )
                                )
                        ) {}
                    }
                    if (showRight) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(edgeWidth)
                                .align(Alignment.CenterEnd)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f),
                                            MaterialTheme.colorScheme.surfaceContainerLowest
                                        )
                                    )
                                )
                        ) {}
                    }
                }
            }
            HoverAware { isHovered, interactionSource ->
                Button(
                    onClick = {
                        viewModel.addTabAndSwitch()
                        if (viewModel.tabEntryList.size > 6) {
                            scope.launch {
                                lazyRowState.animateScrollToItem(
                                    index = viewModel.tabEntryList.size - 1,
                                    scrollOffset = 1
                                )
                            }
                        }
                    },
                    border = BorderStroke((1.5.dp), MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHovered) {
                            MaterialTheme.colorScheme.outline.copy(0.6F)
                        } else MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                    elevation = buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .size(addButtonWidth)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_add),
                        contentDescription = null
                    )
                }
            }
        }
    }
}