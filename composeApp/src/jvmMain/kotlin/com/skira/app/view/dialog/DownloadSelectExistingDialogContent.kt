package com.skira.app.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.skira.app.utilities.PreferenceManager
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.utilities.openSystemFolderPicker
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.verifySelectedDataset
import com.skira.app.structures.DialogType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DownloadSelectExistingDialogContent(onNavigationRequest: (destination: Int) -> Unit) {
    val noPathSelectedMessage = stringResource(Res.string.path_selector_no_path)
    var currentSelectedPathString by remember { mutableStateOf(noPathSelectedMessage) }
    Column(modifier = Modifier.padding(end = 10.dp, start = 20.dp)) {
        Text(
            text = stringResource(Res.string.set_existing_dialog_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.set_existing_dialog_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp).fillMaxWidth(0.33F)
        )
        Row(
            modifier = Modifier.padding(top = 15.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.onBackground.copy(0.05F))
                .border(
                    width = (1.5).dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                    shape = MaterialTheme.shapes.extraSmall
                )
                .fillMaxWidth(0.33F)
                .clickable {
                    openSystemFolderPicker()?.let { currentSelectedPathString = it }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentSelectedPathString,
                modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp)
                    .fillMaxWidth(0.9F),
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Image(
                painter = painterResource(Res.drawable.icon_folder),
                contentDescription = null,
                modifier = Modifier.padding(end = 20.dp)
                    .size(20.dp)
            )
        }
        if (!verifySelectedDataset(currentSelectedPathString)) {
            Text(
                text = stringResource(Res.string.set_existing_dialog_invalid_path),
                color = MaterialTheme.colorScheme.error.copy(0.9F),
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(0.33F)
                .padding(top = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionTextButton(
                text = stringResource(Res.string.dialog_navigate_button_back),
                onClick = {
                    onNavigationRequest(DialogType.DOWNLOAD_ONBOARD)
                },
                color = MaterialTheme.colorScheme.onBackground.copy(0.4F),
                filled = false
            )
            ActionTextButton(
                text = stringResource(Res.string.dialog_navigate_button_continue),
                onClick = {
                    PreferenceManager.apply {
                        putString(PreferenceKey.R_DATASET_FOLDER, currentSelectedPathString)
                        putBoolean(PreferenceKey.ONBOARDING_COMPLETE, true)
                    }
                    onNavigationRequest(DialogType.NONE)
                },
                color = MaterialTheme.colorScheme.onBackground,
                filled = true,
                enabled = currentSelectedPathString != noPathSelectedMessage
                        && verifySelectedDataset(currentSelectedPathString)
            )
        }
    }
}