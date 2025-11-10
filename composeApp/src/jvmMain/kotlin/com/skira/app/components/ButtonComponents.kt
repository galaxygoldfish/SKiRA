package com.skira.app.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_arrow_down
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.composeapp.generated.resources.icon_maximize
import com.skira.app.composeapp.generated.resources.icon_minimize
import com.skira.app.utilities.isRunningOnMac
import org.jetbrains.compose.resources.painterResource


@Composable
fun ActionTextButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    contentDescription: String? = null,
    filled: Boolean = true,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) color else MaterialTheme.colorScheme.background,
            contentColor = if (filled) MaterialTheme.colorScheme.background else color,
            disabledContainerColor = if (filled) color.copy(0.3F) else MaterialTheme.colorScheme.background,
            disabledContentColor = if (filled) MaterialTheme.colorScheme.background else color.copy(0.3F)
        ),
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp, start = 15.dp, end = 15.dp),
        border = if (filled) null else BorderStroke(width = 1.5.dp, color = color),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) MaterialTheme.colorScheme.background else color
        )
        icon?.let {
            Image(
                painter = it,
                contentDescription = contentDescription,
                modifier = Modifier.padding(start = 10.dp)
                    .size(10.dp),
                colorFilter = ColorFilter.tint(if (filled) MaterialTheme.colorScheme.background else color)
            )
        }
    }
}

@Composable
fun WindowControlButton(
    onClick: () -> Unit,
    hoverColor: Color = Color.Black.copy(alpha = 0.1f),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .size(30.dp)
            .hoverable(interactionSource)
            .background(
                color = if (isHovered) hoverColor else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ExpansionMenuButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    sectionExpanded: Boolean
) {
    Button(
        onClick = {
            onClick()
        },
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
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 5.dp)
            )
            AnimatedContent(targetState = sectionExpanded) { expanded ->
                Image(
                    painter = painterResource(Res.drawable.icon_arrow_down),
                    contentDescription = null,
                    modifier = Modifier.rotate(
                        animateFloatAsState(if (expanded) 180F else 0F).value
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MinimalIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    smallSize: Boolean = true
) {
    var hovered by remember { mutableStateOf(false) }
    Box(
        modifier = (if (smallSize) modifier.size(28.dp) else modifier)
            .background(
                if (hovered) MaterialTheme.colorScheme.onBackground.copy(0.05F) else Color.Transparent,
                shape = MaterialTheme.shapes.extraSmall
            )
            .pointerMoveFilter(
                onEnter = {
                    hovered = true
                    false
                },
                onExit = {
                    hovered = false
                    false
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Contains the group of window control buttons used in the Windows style title bar.
 * Typically placed at the top end of a window
 *
 * @param windowState The current state of the window, used to control its placement.
 * @param onMinimize A callback function to be invoked when the minimize button is clicked.
 * @param exitApplication A callback function to be invoked when the close button is clicked.
 */
@Composable
fun WindowsNavigationButtonGroup(
    windowState: WindowState,
    onMinimize: () -> Unit,
    exitApplication: () -> Unit
) {
    Row {
        WindowControlButton(
            onClick = onMinimize,
            hoverColor = Color.Black.copy(alpha = 0.05F),
            modifier = Modifier.padding(end = 7.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_minimize),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
            )
        }
        WindowControlButton(
            onClick = {
                windowState.placement =
                    if (windowState.placement == WindowPlacement.Maximized) {
                        WindowPlacement.Floating
                    } else {
                        WindowPlacement.Maximized
                    }
            },
            hoverColor = Color.Black.copy(alpha = 0.05F),
            modifier = Modifier.padding(end = 7.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_maximize),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
            )
        }
        WindowControlButton(
            onClick = exitApplication,
            hoverColor = Color(0xFFE81123).copy(alpha = 0.9F),
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_close),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
            )
        }
    }
}

/**
 * Contains the group of window control buttons used in the macOS style title bar.
 * Typically placed at the top start of a window
 *
 * @param windowState The current state of the window, used to control its placement.
 * @param onMinimize A callback function to be invoked when the minimize button is clicked.
 * @param exitApplication A callback function to be invoked when the close button is clicked.
 */
@Composable
fun MacNavigationButtonGroup(
    windowState: WindowState,
    onMinimize: () -> Unit,
    exitApplication: () -> Unit
) {
    Row {
        Box(
            modifier = Modifier.padding(start = 20.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Red)
                .clickable {
                    exitApplication()
                }
        ) { }
        Box(
            modifier = Modifier.padding(start = 10.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Yellow)
                .clickable {
                    onMinimize()
                }
        ) { }
        Box(
            modifier = Modifier.padding(start = 10.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Green)
                .clickable {
                    windowState.placement =
                        if (windowState.placement == WindowPlacement.Maximized) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Maximized
                        }
                }
        )
    }
}

@Composable
fun DownloadIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val ratio =
        if (painter.intrinsicSize.width > 0f && painter.intrinsicSize.height > 0f)
            painter.intrinsicSize.width / painter.intrinsicSize.height
        else 1f
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.padding(end = 5.dp)
            .height(22.dp)
            .aspectRatio(ratio)
            .clickable {
                onClick()
            },
        contentScale = ContentScale.Fit
    )
}