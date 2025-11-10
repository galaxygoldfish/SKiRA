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
import com.skira.app.utilities.openSystemFolderPicker
import com.skira.app.viewmodel.SettingsDialogViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsDialogContent(onDismissRequest: () -> Unit) {
    val viewModel: SettingsDialogViewModel = viewModel()
    LaunchedEffect(true) {
        viewModel.loadDownloadDirectory()
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
                     viewModel.saveDownloadDirectory()
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
         Text(
             text = "Plot download directory",
             style = MaterialTheme.typography.bodyLarge,
             modifier = Modifier.padding(top = 30.dp)
         )
         Row(
             modifier = Modifier.padding(top = 15.dp, bottom = 25.dp)
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
             androidx.compose.material3.Text(
                 text = viewModel.selectedDownloadFolder,
                 modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp),
                 color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
             )
             Image(
                 painter = painterResource(Res.drawable.icon_folder),
                 contentDescription = null,
                 modifier = Modifier.padding(end = 20.dp)
                     .size(20.dp)
             )
         }
     }
}