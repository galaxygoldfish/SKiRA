package com.skira.app.utilities

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

/**
 * Used to determine the operating system the application is running on.
 *
 * @return true if the application is running on macOS, false for all other OS
 */
fun isRunningOnMac(): Boolean {
    return System.getProperty("os.name")?.lowercase()?.contains("mac") ?: false
}

/**
 * Enables double-tap gesture detection on the window to toggle between maximized and floating placements
 * Keeps in mind that the user can still double-click and drag the window title bar to move the app
 *
 * @param windowState The [WindowState] of the current application window to use for resizing operations
 */
@Composable
fun Modifier.doubleTapWindowGestureDetector(windowState: WindowState): Modifier {
    return pointerInput(Unit) {
        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            val firstDownTime = System.currentTimeMillis()
            val firstDownPosition = firstDown.position
            try {
                val up = withTimeout(viewConfiguration.doubleTapTimeoutMillis) {
                    waitForUpOrCancellation()
                }
                if (up != null) {
                    val upTime = System.currentTimeMillis()
                    val timeDiff = upTime - firstDownTime
                    val dragDistance = (up.position - firstDownPosition).getDistance()
                    val wasDrag = dragDistance > viewConfiguration.touchSlop
                    if (!wasDrag && timeDiff < viewConfiguration.doubleTapTimeoutMillis) {
                        val secondDown = withTimeoutOrNull(
                            timeMillis = viewConfiguration.doubleTapTimeoutMillis
                        ) {
                            awaitFirstDown(requireUnconsumed = false)
                        }
                        if (secondDown != null) {
                            val secondDownPosition = secondDown.position
                            val secondUp = withTimeoutOrNull(
                                timeMillis = viewConfiguration.doubleTapTimeoutMillis
                            ) {
                                waitForUpOrCancellation()
                            }
                            if (secondUp != null) {
                                val secondDragDistance = (secondUp.position - secondDownPosition).getDistance()
                                val wasSecondDrag = secondDragDistance > viewConfiguration.touchSlop
                                if (!wasSecondDrag) {
                                    windowState.placement =
                                        if (windowState.placement == WindowPlacement.Maximized) {
                                            WindowPlacement.Floating
                                        } else {
                                            WindowPlacement.Maximized
                                        }
                                }
                            }
                        }
                    }
                }
            } catch (_: PointerEventTimeoutCancellationException) { /* Timeout - do nothing */ }
        }
    }
}