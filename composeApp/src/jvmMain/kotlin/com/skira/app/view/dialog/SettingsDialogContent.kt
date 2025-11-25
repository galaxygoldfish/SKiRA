package com.skira.app.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.composeapp.generated.resources.icon_folder
import com.skira.app.composeapp.generated.resources.icon_open_in_new_tab
import com.skira.app.composeapp.generated.resources.skira_icon
import com.skira.app.utilities.openSystemFolderPicker
import com.skira.app.viewmodel.SettingsDialogViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsDialogContent(onDismissRequest: () -> Unit) {
    val viewModel: SettingsDialogViewModel = viewModel()
    LaunchedEffect(true) {
        viewModel.loadPreferences()
    }
     Column(Modifier.fillMaxWidth(0.33F)) {
         Row(
             modifier = Modifier.fillMaxWidth(),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Text(
                 text = "App settings",
                 style = MaterialTheme.typography.headlineLarge
             )
             MinimalIconButton(
                 onClick = {
                     viewModel.apply {
                         saveDownloadDirectory()
                         saveOpenDownloadFolderPreference()
                     }
                     onDismissRequest()
                 },
                 icon = {
                     Icon(
                         painter = painterResource(Res.drawable.icon_close),
                         contentDescription = null,
                         tint = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                         modifier = Modifier.size(20.dp)
                     )
                 }
             )
         }
         Row(
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween,
             modifier = Modifier.fillMaxWidth()
                 .padding(top = 35.dp)
         ) {
             Text(
                 text = "Open download folder after saving plots",
                 style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
             )
             Switch(
                 checked = viewModel.openDownloadFolderPreference,
                 onCheckedChange = {
                     viewModel.openDownloadFolderPreference = it
                 },
                 colors = SwitchDefaults.colors(
                     checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.2F),
                     uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                     checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.2F),
                     uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                     uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                     checkedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.2F)
                 )
             )
         }
         Column {
             Text(
                 text = "Plot download directory",
                 style = MaterialTheme.typography.labelMedium,
                 modifier = Modifier.padding(top = 20.dp),
                 color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
             )
             Row(
                 modifier = Modifier.padding(top = 10.dp, bottom = 25.dp)
                     .clip(MaterialTheme.shapes.extraSmall)
                     .background(MaterialTheme.colorScheme.onBackground.copy(0.05F))
                     .border(
                         width = (1.5).dp,
                         color = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                         shape = MaterialTheme.shapes.extraSmall
                     )
                     .fillMaxWidth()
                     .clickable {
                         openSystemFolderPicker()?.let {
                             viewModel.selectedDownloadFolder = it
                         }
                     },
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Text(
                     text = viewModel.selectedDownloadFolder,
                     modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp),
                     color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                     style = MaterialTheme.typography.labelSmall
                 )
                 Image(
                     painter = painterResource(Res.drawable.icon_folder),
                     contentDescription = null,
                     modifier = Modifier.padding(end = 20.dp)
                         .size(20.dp)
                 )
             }
         }
         Row(
             modifier = Modifier.padding(top = 15.dp)
         ) {
             Image(
                 painter = painterResource(Res.drawable.skira_icon),
                 contentDescription = null,
                 modifier = Modifier.size(15.dp)
             )
             Text(
                 text = "SKiRA v1.0.0",
                 modifier = Modifier.padding(start = 5.dp),
                 style = MaterialTheme.typography.bodyLarge,
                 color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
             )
         }
     }
}