package com.skira.app.view.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DialogType
import org.jetbrains.compose.resources.stringResource

@Composable
fun DownloadOnboardDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
        Text(
            text = stringResource(Res.string.download_onboard_dialog_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.download_onboard_dialog_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.33F)
        )
        Text(
            text = stringResource(Res.string.download_onboard_dialog_disk_space),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
            modifier = Modifier.padding(top = 20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.33F)
                .padding(top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionTextButton(
                text = stringResource(Res.string.dialog_navigate_button_already_have),
                onClick = { onNavigationRequest(DialogType.SELECT_EXISTING_OBJECT) },
                color = MaterialTheme.colorScheme.onBackground.copy(0.4F),
                filled = false
            )
            ActionTextButton(
                text = stringResource(Res.string.dialog_navigate_button_continue),
                onClick = {
                    onNavigationRequest(DialogType.SELECT_DOWNLOAD_PATH)
                },
                color = MaterialTheme.colorScheme.onBackground,
                filled = true
            )
        }
    }
}
