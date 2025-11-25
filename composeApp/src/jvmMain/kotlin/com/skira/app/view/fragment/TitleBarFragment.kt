package com.skira.app.view.fragment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.skira.app.components.MacNavigationButtonGroup
import com.skira.app.components.MinimalIconButton
import com.skira.app.components.WindowsNavigationButtonGroup
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DialogType
import com.skira.app.utilities.doubleTapWindowGestureDetector
import com.skira.app.utilities.isRunningOnMac
import com.skira.app.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(color = MaterialTheme.colorScheme.primary)
                .doubleTapWindowGestureDetector(windowState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.skira_logo_small),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 15.dp)
                        .size(30.dp)
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineLarge + TextStyle(fontSize = 18.sp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                ) {
                    Text(
                        text = stringResource(Res.string.app_beta),
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