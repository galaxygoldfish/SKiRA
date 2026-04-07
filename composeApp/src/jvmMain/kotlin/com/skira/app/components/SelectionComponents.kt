package com.skira.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Accessible popup text component with a high contrast background suitable
 * for usage over images or content where the background color is dynamic
 * @param text Text to display on the popup
 * @param show Whether or not the popup is currently visible
 * @param modifier The Modifier to be applied to the popup
 */
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

/**
 * A fragment composed of 4 images placed in each quadrant of a square with a tooltip
 * appearing upon hover of any of the quadrants, showing the corresponding label
 * @param image The composable content to display as the primary image. This should include
 *              all 4 quadrants, and the layout is assumed to be equal division into 4 squares
 * @param q1Label, q2Label, q3Label, q4Label The labels to show in the tooltip when hovering
 *                 over quadrants 1-4 respectively
 * @param modifier Modifier to be applied to the whole fragment
 */
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