package com.skira.app.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendMode.Companion
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.shimmerSpec

@Composable
fun Modifier.skiraShimmer() = shimmer(
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

@Composable
fun ShimmerPlaceholder(
    height: Dp,
    width: Dp,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.skiraShimmer()
            .size(width, height)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    )
}

@Composable
fun ShimmerPlaceholder(
    height: Float,
    width: Float,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.skiraShimmer()
            .fillMaxHeight(height)
            .fillMaxWidth(width)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    )
}