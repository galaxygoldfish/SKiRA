package com.skira.app


import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.util.SystemInfo
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.skira_outer_icon
import com.skira.app.utilities.isRunningOnMac
import com.skira.app.view.HomeView
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.UIManager


fun main() {
    if (isRunningOnMac()) {
        System.setProperty("apple.awt.application.name", "SKiRA")
    }
    application(
        exitProcessOnExit = true,
        content = {
            FlatLightLaf.setup()
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
                JFrame.setDefaultLookAndFeelDecorated(true)
                UIManager.put("TitlePane.background", awtPrimary)
                UIManager.put("TitlePane.backgroundStart", awtPrimary)
                UIManager.put("TitlePane.backgroundEnd", awtPrimary)
                UIManager.put("TitlePane.backgroundInactive", awtPrimary.darker())
            }

            val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

            Window(
                onCloseRequest = ::exitApplication,
                icon = if (isRunningOnMac()) null else painterResource(Res.drawable.skira_outer_icon),
                undecorated = false,
                title = "",
                state = windowState,
                onPreviewKeyEvent = { false }
            ) {
                if (isRunningOnMac()) {
                    val rootPane = window.rootPane
                    rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                    rootPane.putClientProperty(
                        FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING,
                        FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING_LARGE
                    )
                }

                window.rootPane.border = javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)

                window.minimumSize = Dimension(800, 600)
                HomeView(windowState, ::exitApplication, window as java.awt.Frame)
            }
        }
    )
}
