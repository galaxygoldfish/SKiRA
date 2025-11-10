package com.skira.app.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DialogType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * This dialog content is shown to users when they have not yet completed the onboarding process.
 * From here, they are directed to the download process after pressing the start button.
 */
@Composable
fun WelcomeDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    val uriHandler = LocalUriHandler.current
    Row {
        Image(
            painter = painterResource(Res.drawable.skira_logo),
            contentDescription = null,
            modifier = Modifier.padding(10.dp)
                .size(60.dp)
        )
        Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
            Text(
                text = stringResource(Res.string.welcome_dialog_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(Res.string.welcome_dialog_subtitle),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = stringResource(Res.string.welcome_dialog_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.33F)
            )
            Text(
                text = stringResource(Res.string.welcome_dialog_begin_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.33F)
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.33F)
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionTextButton(
                    text = stringResource(Res.string.welcome_dialog_start_button),
                    icon = painterResource(Res.drawable.icon_arrow_end),
                    contentDescription = null,
                    onClick = { onNavigationRequest(DialogType.DOWNLOAD_ONBOARD) },
                    color = MaterialTheme.colorScheme.onBackground,
                    filled = true
                )
                ActionTextButton(
                    text = stringResource(Res.string.welcome_dialog_abitua_lab_button),
                    onClick = { uriHandler.openUri("https://abitua.org") },
                    color = MaterialTheme.colorScheme.outline,
                    filled = false
                )
            }
        }
    }
}