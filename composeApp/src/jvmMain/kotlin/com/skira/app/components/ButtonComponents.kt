package com.skira.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.composeapp.generated.resources.icon_maximize
import com.skira.app.composeapp.generated.resources.icon_minimize
import org.jetbrains.compose.resources.painterResource


/**
 * A general rectangular text button for primary usage
 * @param text The text to be displayed in the button
 * @param modifier Modifier to apply to the button as a whole
 * @param icon Painter resource for an optional icon to be displayed at text end
 * @param contentDescription Content description of the icon, only required if the icon is present
 * @param filled Whether the button container is filled or outlined
 * @param color The color to use either for the foreground if outlined, or the container if filled
 * @param onClick Callback on user click
 * @param enabled Whether the button can be interacted with or not
 */
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
            style = MaterialTheme.typography.bodyMedium,
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

/**
 * Larger version of MinimalIconButton designed for window control in the title bar on
 * the Windows version of the app, with customizable hover background color
 * @param onClick Callback to be invoked on user click
 * @param hoverColor Color of the background to be applied when the button is hovered
 * @param modifier Modifier to be applied to the button
 * @param content The composable content representing the icon of the button
 */
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

/**
 * Since many components do not communicate their hover state on desktop by default,
 * wrapping any Composable content in a HoverAware provides the opacity change effect
 * on mouse hover
 * @param content The composable content to be wrapped with a forced hover state
 * Note that you must pass at minimum the interactionSource to the component which you
 * want to have the hover effect within the content parameter
 */
@Composable
fun HoverAware(
    content: @Composable (isHovered: Boolean, interactionSource: MutableInteractionSource) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    content(isHovered, interactionSource)
}

/**
 * A simple icon only button with hover effect
 * @param onClick To be invoked on user click
 * @param icon The composable primary content of the button
 * @param modifier The Modifier to be applied to the button
 * @param smallSize Whether or not to enforce a 28 dp max size
 */
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