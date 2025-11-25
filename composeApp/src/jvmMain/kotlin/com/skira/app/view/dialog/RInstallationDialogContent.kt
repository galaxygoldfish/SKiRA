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
import com.skira.app.utilities.isRunningOnMac
import javax.swing.Action

@Composable
fun RInstallationDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
        Text(
            text = "Could not find R",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "We weren't able to find the location of your R installation. \nSKiRA requires R to be installed on your computer before running the app.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 15.dp).fillMaxWidth(0.33F)
        )
        if (!isRunningOnMac()) {
            Row(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    text = "Already installed R?",
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
                Text(
                    text = "Click here to set your R path",
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
            }
        }
        ActionTextButton(
            text = "Download R",
            color = MaterialTheme.colorScheme.onBackground,
            onClick = { uriHandler.openUri("https://cloud.r-project.org") },
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}