package com.skira.app.view.fragment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.skira.app.components.MinimalIconButton
import com.skira.app.components.WindowsNavigationButtonGroup
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DialogType
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.PreferenceManager
import com.skira.app.utilities.doubleTapWindowGestureDetector
import com.skira.app.utilities.isRunningOnMac
import com.skira.app.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.Cursor

/**
 * This fragment is the content of our title bar, which has the app title, navigation buttons, and settings
 *
 * @param windowState The [WindowState] from the parent application
 * @param onMinimize Callback invoked to minimize the application (_ button)
 * @param exitApplication Callback invoked to exit the application (x button)
 * @param viewModel The current HomeViewModel instance from the parent/caller
 */
@Composable
fun WindowScope.TitleBarFragment(
    windowState: WindowState,
    onMinimize: () -> Unit,
    exitApplication: () -> Unit,
    viewModel: HomeViewModel
) {
    WindowDraggableArea {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        val compactTabsEnabled by remember(viewModel.currentDialogToShow) {
            derivedStateOf {
                PreferenceManager.getBoolean(PreferenceKey.PREFERENCE_USE_COMPACT_TABS, false)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compactTabsEnabled) 60.dp else if (isRunningOnMac()) 50.dp else 45.dp)
                .background(color = MaterialTheme.colorScheme.primary)
                .doubleTapWindowGestureDetector(windowState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (isRunningOnMac()) Modifier.padding(start = 90.dp) else Modifier
            ) {
                Image(
                    painter = painterResource(Res.drawable.skira_logo),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.bodyLarge + TextStyle(fontSize = 18.sp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(Modifier.weight(1F)) {
                if (compactTabsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 15.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        TabSelectorFragment(viewModel = viewModel)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                MinimalIconButton(
                    icon = {
                        Image(
                            painter = painterResource(Res.drawable.icon_gear),
                            contentDescription = null,
                            modifier = Modifier.padding(all = 5.dp)
                                .size(size = 20.dp),
                            colorFilter = ColorFilter.tint(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F)
                            )
                        )
                    },
                    onClick = { viewModel.currentDialogToShow = DialogType.SETTINGS },
                    smallSize = false,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Row(
                    modifier = Modifier.padding(end = 15.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(Color.White)
                        .clickable { uriHandler.openUri("https://abitua.org") }
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                ) {
                    Text(
                        text = stringResource(Res.string.welcome_dialog_abitua_lab),
                        style = MaterialTheme.typography.headlineLarge + TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
                if (!isRunningOnMac()) { WindowsNavigationButtonGroup(windowState, onMinimize, exitApplication) }
            }
        }
    }
}