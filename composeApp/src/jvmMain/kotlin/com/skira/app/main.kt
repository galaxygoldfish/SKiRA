package com.skira.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.FlatLightLaf
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.skira_outer_icon
import com.skira.app.view.HomeView
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import javax.swing.UIManager

fun main() {
    application(
        exitProcessOnExit = true,
        content = {
            FlatLightLaf.setup()
            UIManager.put("TitlePane.showIcon", false)
            UIManager.put("TitlePane.showTitle", false)
            UIManager.put("TitlePane.closeIcon", null)
            UIManager.put("TitlePane.iconifyIcon", null)
            UIManager.put("TitlePane.maximizeIcon", null)
            UIManager.put("TitlePane.restoreIcon", null)
            UIManager.put("TitlePane.buttonSize", Dimension(0, 0))
            UIManager.put("TitlePane.menuBarEmbedded", false)

            val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

            Window(
                onCloseRequest = ::exitApplication,
                icon = painterResource(Res.drawable.skira_outer_icon),
                undecorated = false, // This will keep system window movement animations
                title = "",
                state = windowState
            ) {
                window.minimumSize = Dimension(800, 600)
                HomeView(windowState, ::exitApplication)
            }
        }
    )
}