package com.skira.app.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Placeholder composable that displays a constant shimmer animation when
 * visible, to be used to communicate loading states to the user
 *
 * @param modifier The [Modifier] to be applied to the placeholder
 */
@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier.shimmer(
            customShimmer = rememberShimmer(
                theme = ShimmerTheme(
                    animationSpec = infiniteRepeatable(
                        animation = shimmerSpec(
                            durationMillis = 1600,
                            easing = LinearEasing,
                            delayMillis = 0,
                        ),
                        repeatMode = RepeatMode.Restart,
                    ),
                    blendMode = BlendMode.DstIn,
                    rotation = 30.0f,
                    shaderColors = listOf(
                        Color.White.copy(alpha = 0.2F),
                        Color.White.copy(alpha = 0.4F),
                        Color.White.copy(alpha = 0.2F),
                    ),
                    shaderColorStops = listOf(0.0F, 0.5F, 1.0F),
                    shimmerWidth = 500.dp,
                ),
                shimmerBounds = ShimmerBounds.View
            )
        )
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    )
}

@Composable
fun SmoothProgressBar(
    reportedProgress: Int,
    modifier: Modifier = Modifier
) {
    val clamped = reportedProgress.coerceIn(0, 100)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(clamped) {
        val target = clamped / 100f
        val delta = abs(anim.value - target)
        val duration = ( (delta * 100f).roundToInt() * 12 ).coerceIn(120, 2000)
        anim.animateTo(
            target,
            animationSpec = tween(durationMillis = duration, easing = LinearEasing)
        )
    }
    LinearProgressIndicator(
        modifier = modifier,
        trackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
        color = Color(0XFFC7CED7),
        progress = anim.value
    )
}