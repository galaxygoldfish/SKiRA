package com.skira.app.view.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.r_installation_dialog_already_installed_subtitle
import com.skira.app.composeapp.generated.resources.r_installation_dialog_already_installed_title
import com.skira.app.composeapp.generated.resources.r_installation_dialog_download_button
import com.skira.app.composeapp.generated.resources.r_installation_dialog_subtitle
import com.skira.app.composeapp.generated.resources.r_installation_dialog_title
import com.skira.app.utilities.isRunningOnMac
import org.jetbrains.compose.resources.stringResource
import javax.swing.Action

@Composable
fun RInstallationDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
        Text(
            text = stringResource(Res.string.r_installation_dialog_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.r_installation_dialog_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 15.dp).fillMaxWidth(0.33F)
        )
        if (!isRunningOnMac()) {
            Row(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    text = stringResource(Res.string.r_installation_dialog_already_installed_title),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
                Text(
                    text = stringResource(Res.string.r_installation_dialog_already_installed_subtitle),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
            }
        }
        ActionTextButton(
            text = stringResource(Res.string.r_installation_dialog_download_button),
            color = MaterialTheme.colorScheme.onBackground,
            onClick = { uriHandler.openUri("https://cloud.r-project.org") },
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}