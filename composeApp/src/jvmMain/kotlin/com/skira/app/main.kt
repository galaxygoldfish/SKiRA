package com.skira.app


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.FlatLightLaf
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.skira_outer_icon
import com.skira.app.utilities.isRunningOnMac
import com.skira.app.view.HomeView
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import javax.swing.UIManager


fun main() {
    if (isRunningOnMac()) {
        System.setProperty("apple.awt.application.name", "SKiRA")
        System.setProperty("apple.awt.fullWindowContent", "true")
        System.setProperty("apple.awt.transparentTitleBar", "true")
    }
    application(
        exitProcessOnExit = true,
        content = {
            if (!isRunningOnMac()) {
                FlatLightLaf.setup()
                UIManager.put("TitlePane.showIcon", false)
                UIManager.put("TitlePane.showTitle", false)
                UIManager.put("TitlePane.closeIcon", null)
                UIManager.put("TitlePane.iconifyIcon", null)
                UIManager.put("TitlePane.maximizeIcon", null)
                UIManager.put("TitlePane.restoreIcon", null)
                UIManager.put("TitlePane.buttonSize", Dimension(0, 0))
                UIManager.put("TitlePane.menuBarEmbedded", true)
                val awtPrimary = java.awt.Color(0xE8, 0xEF, 0xF5, 0xFF)
                javax.swing.JFrame.setDefaultLookAndFeelDecorated(true)
                UIManager.put("TitlePane.background", awtPrimary)
                UIManager.put("TitlePane.backgroundStart", awtPrimary)
                UIManager.put("TitlePane.backgroundEnd", awtPrimary)
                UIManager.put("TitlePane.backgroundInactive", awtPrimary.darker())
            }

            val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

            Window(
                onCloseRequest = ::exitApplication,
                icon = painterResource(Res.drawable.skira_outer_icon),
                undecorated = false,
                title = "",
                state = windowState,
                onPreviewKeyEvent = { false }
            ) {
                if (isRunningOnMac()) {
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    window.rootPane.border = javax.swing.BorderFactory.createEmptyBorder(25, 0, 0, 0)
                } else {
                    window.rootPane.border = javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
                }

                window.minimumSize = Dimension(800, 600)
                HomeView(windowState, ::exitApplication, window as java.awt.Frame)
            }
        }
    )
}
