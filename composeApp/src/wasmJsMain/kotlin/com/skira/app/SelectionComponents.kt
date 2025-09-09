package com.skira.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import skira.composeapp.generated.resources.Res
import skira.composeapp.generated.resources.icon_check

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
            .border(width = 1.5.dp, color = MaterialTheme.colorScheme.onBackground, shape = MaterialTheme.shapes.extraSmall)
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