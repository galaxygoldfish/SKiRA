package com.skira.app.view.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_close
import com.skira.app.composeapp.generated.resources.icon_folder
import com.skira.app.composeapp.generated.resources.icon_information
import com.skira.app.composeapp.generated.resources.settings_dialog_download_dir
import com.skira.app.composeapp.generated.resources.settings_dialog_open_download_folder
import com.skira.app.composeapp.generated.resources.settings_dialog_title
import com.skira.app.composeapp.generated.resources.skira_icon
import com.skira.app.utilities.openSystemFolderPicker
import com.skira.app.viewmodel.SettingsDialogViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.skira.app.build.AppBuildInfo
import com.skira.app.composeapp.generated.resources.abitua
import com.skira.app.composeapp.generated.resources.app_name
import com.skira.app.composeapp.generated.resources.icon_colors
import com.skira.app.composeapp.generated.resources.preview_compact_tab
import com.skira.app.composeapp.generated.resources.preview_noncompact_tab
import com.skira.app.composeapp.generated.resources.settings_dialog_compact_tab_label
import com.skira.app.composeapp.generated.resources.settings_dialog_compact_tabs_title
import com.skira.app.composeapp.generated.resources.settings_dialog_download_section
import com.skira.app.composeapp.generated.resources.settings_dialog_extended_edit
import com.skira.app.composeapp.generated.resources.settings_dialog_extended_edit_verbose
import com.skira.app.composeapp.generated.resources.settings_dialog_info_abitua_title
import com.skira.app.composeapp.generated.resources.settings_dialog_info_abitua_verbose
import com.skira.app.composeapp.generated.resources.settings_dialog_normal_tab_label
import com.skira.app.composeapp.generated.resources.settings_dialog_open_download_verbose
import org.jetbrains.skiko.Cursor

@Composable
fun SettingsDialogContent(onDismissRequest: () -> Unit) {
    val viewModel: SettingsDialogViewModel = viewModel()
    val uriHandler = LocalUriHandler.current
    var selectedPage by remember { mutableStateOf(SettingsPage.STORAGE) }
    LaunchedEffect(true) {
        viewModel.loadPreferences()
    }
     Column(
         modifier = Modifier.fillMaxWidth(0.6F)
             .fillMaxHeight(0.7F)
     ) {
         Row(
             modifier = Modifier.fillMaxWidth(),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Text(
                 text = stringResource(Res.string.settings_dialog_title),
                 style = MaterialTheme.typography.headlineLarge
             )
             MinimalIconButton(
                 onClick = {
                     viewModel.apply {
                         saveDownloadDirectory()
                         saveOpenDownloadFolderPreference()
                         saveUseExtendedEditPreference()
                         saveUseCompactTabsPreference()
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
             modifier = Modifier.fillMaxWidth()
                 .padding(top = 26.dp),
             horizontalArrangement = Arrangement.spacedBy(25.dp),
             verticalAlignment = Alignment.Top
         ) {
             Column(
                 modifier = Modifier.fillMaxWidth(0.25f)
             ) {
                 SettingsPageButton(
                      text = "Storage",
                     iconRes = Res.drawable.icon_folder,
                     selected = selectedPage == SettingsPage.STORAGE,
                     onClick = { selectedPage = SettingsPage.STORAGE }
                 )
                 SettingsPageButton(
                      text = "Appearance",
                     iconRes = Res.drawable.icon_colors,
                     selected = selectedPage == SettingsPage.APPEARANCE,
                     onClick = { selectedPage = SettingsPage.APPEARANCE },
                     modifier = Modifier.padding(top = 5.dp)
                 )
                 SettingsPageButton(
                      text = "Info",
                     iconRes = Res.drawable.icon_information,
                     selected = selectedPage == SettingsPage.INFO,
                     onClick = { selectedPage = SettingsPage.INFO },
                     modifier = Modifier.padding(top = 5.dp)
                 )
             }

             Column(modifier = Modifier.weight(1f)) {
                 AnimatedContent(
                     targetState = selectedPage,
                     transitionSpec = {
                         val movingForward = targetState.ordinal > initialState.ordinal
                         val enterOffset = { fullHeight: Int -> if (movingForward) fullHeight / 3 else -fullHeight / 3 }
                         val exitOffset = { fullHeight: Int -> if (movingForward) -fullHeight / 3 else fullHeight / 3 }
                         (slideInVertically(animationSpec = tween(220), initialOffsetY = enterOffset) + fadeIn(animationSpec = tween(220))).togetherWith(
                             slideOutVertically(animationSpec = tween(180), targetOffsetY = exitOffset) + fadeOut(animationSpec = tween(180))
                         )
                     },
                     label = "settings-page-content",
                     modifier = Modifier.fillMaxSize()
                 ) { page ->
                     when (page) {
                         SettingsPage.STORAGE -> {
                             Column {
                                 Text(
                                     text = stringResource(Res.string.settings_dialog_download_section),
                                     style = MaterialTheme.typography.bodySmall,
                                     color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                 )
                                 Row(
                                     modifier = Modifier.fillMaxWidth()
                                         .padding(top = 10.dp)
                                         .clip(MaterialTheme.shapes.small)
                                         .background(
                                             if (viewModel.openDownloadFolderPreference) MaterialTheme.colorScheme.primary else Color(
                                                 0XFFF3F4F5
                                             )
                                         )
                                         .clickable(true) {
                                             viewModel.openDownloadFolderPreference =
                                                 !viewModel.openDownloadFolderPreference
                                         }
                                         .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                     horizontalArrangement = Arrangement.SpaceBetween,
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     Column(modifier = Modifier.padding(vertical = 15.dp)) {
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_open_download_folder),
                                             style = MaterialTheme.typography.bodyLarge,
                                             modifier = Modifier.padding(start = 15.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                         )
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_open_download_verbose),
                                             style = MaterialTheme.typography.bodySmall,
                                             modifier = Modifier.padding(start = 15.dp, top = 2.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                         )
                                     }
                                     Switch(
                                         checked = viewModel.openDownloadFolderPreference,
                                         onCheckedChange = {
                                             viewModel.openDownloadFolderPreference = it
                                         },
                                         colors = SwitchDefaults.colors(
                                             checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.15F),
                                             uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                             checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(0.7F),
                                             uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                                             uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                             checkedBorderColor = MaterialTheme.colorScheme.secondary
                                         ),
                                         modifier = Modifier.padding(end = 15.dp, top = 5.dp, bottom = 5.dp)
                                     )
                                 }
                                 Row(
                                     modifier = Modifier.fillMaxWidth()
                                         .padding(top = 10.dp)
                                         .clip(MaterialTheme.shapes.small)
                                         .background(
                                             if (viewModel.useExtendedEditPreference) MaterialTheme.colorScheme.primary else Color(
                                                 0XFFF3F4F5
                                             )
                                         )
                                         .clickable(true) {
                                             viewModel.useExtendedEditPreference = !viewModel.useExtendedEditPreference
                                         }
                                         .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                     horizontalArrangement = Arrangement.SpaceBetween,
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     Column(modifier = Modifier.padding(vertical = 15.dp)) {
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_extended_edit),
                                             style = MaterialTheme.typography.bodyLarge,
                                             modifier = Modifier.padding(start = 15.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
                                         )
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_extended_edit_verbose),
                                             style = MaterialTheme.typography.bodySmall,
                                             modifier = Modifier.padding(start = 15.dp, top = 2.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                         )
                                     }
                                     Switch(
                                         checked = viewModel.useExtendedEditPreference,
                                         onCheckedChange = {
                                             viewModel.useExtendedEditPreference = it
                                         },
                                         colors = SwitchDefaults.colors(
                                             checkedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.15F),
                                             uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                             checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(0.7F),
                                             uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                                             uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                             checkedBorderColor = MaterialTheme.colorScheme.secondary
                                         ),
                                         modifier = Modifier.padding(end = 15.dp, top = 5.dp, bottom = 5.dp)
                                     )
                                 }

                                 Text(
                                     text = stringResource(Res.string.settings_dialog_download_dir),
                                     style = MaterialTheme.typography.bodySmall,
                                     modifier = Modifier.padding(top = 20.dp),
                                     color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                 )
                                 Row(
                                     modifier = Modifier.padding(top = 10.dp, bottom = 25.dp)
                                         .clip(MaterialTheme.shapes.small)
                                         .background(MaterialTheme.colorScheme.onBackground.copy(0.05F))
                                         .border(
                                             width = (1.5).dp,
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.1F),
                                             shape = MaterialTheme.shapes.small
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
                                         style = MaterialTheme.typography.bodyMedium
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

                         SettingsPage.APPEARANCE -> {
                             Column {
                                 Text(
                                     text = stringResource(Res.string.settings_dialog_compact_tabs_title),
                                     style = MaterialTheme.typography.bodySmall,
                                     color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                 )
                                 Row(
                                     horizontalArrangement = Arrangement.spacedBy(15.dp),
                                     modifier = Modifier.padding(top = 15.dp)
                                 ) {
                                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                         Column(
                                             modifier = Modifier
                                                 .clip(MaterialTheme.shapes.small)
                                                 .background(MaterialTheme.colorScheme.onBackground.copy(0.03f))
                                                 .border(
                                                     width = if (viewModel.useCompactTabsPreference) 2.dp else 1.dp,
                                                     color = if (viewModel.useCompactTabsPreference) {
                                                         Color(0xFF767D87)
                                                     } else {
                                                         MaterialTheme.colorScheme.onBackground.copy(0.12f)
                                                     },
                                                     shape = MaterialTheme.shapes.small
                                                 )
                                                 .clickable {
                                                     viewModel.useCompactTabsPreference = true
                                                 }
                                                 .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                         ) {
                                             Image(
                                                 painter = painterResource(Res.drawable.preview_compact_tab),
                                                 contentDescription = null
                                             )
                                         }
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_compact_tab_label),
                                             style = MaterialTheme.typography.bodyMedium,
                                             modifier = Modifier.padding(top = 15.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                         )
                                     }
                                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                         Column(
                                             modifier = Modifier
                                                 .clip(MaterialTheme.shapes.small)
                                                 .background(MaterialTheme.colorScheme.onBackground.copy(0.03f))
                                                 .border(
                                                     width = if (!viewModel.useCompactTabsPreference) 2.dp else 1.dp,
                                                     color = if (!viewModel.useCompactTabsPreference) {
                                                         Color(0xFF767D87)
                                                     } else {
                                                         MaterialTheme.colorScheme.onBackground.copy(0.12f)
                                                     },
                                                     shape = MaterialTheme.shapes.small
                                                 )
                                                 .clickable {
                                                     viewModel.useCompactTabsPreference = false
                                                 }
                                                 .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                         ) {
                                             Image(
                                                 painter = painterResource(Res.drawable.preview_noncompact_tab),
                                                 contentDescription = null
                                             )
                                         }
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_normal_tab_label),
                                             style = MaterialTheme.typography.bodyMedium,
                                             modifier = Modifier.padding(top = 15.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.7F)
                                         )
                                     }
                                 }
                             }
                         }

                         SettingsPage.INFO -> {
                             Column {
                                 Row {
                                     Image(
                                         painter = painterResource(Res.drawable.skira_icon),
                                         contentDescription = null,
                                         modifier = Modifier
                                             .clip(MaterialTheme.shapes.medium)
                                             .size(55.dp)
                                     )
                                     Column(modifier = Modifier.padding(start = 15.dp)) {
                                         Text(
                                             text = stringResource(Res.string.app_name),
                                             style = MaterialTheme.typography.headlineLarge
                                         )
                                         Text(
                                             text = "v${AppBuildInfo.VERSION}",
                                             style = MaterialTheme.typography.bodyMedium,
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                             modifier = Modifier.padding(top = 2.dp)
                                         )
                                     }
                                 }
                                 Row(
                                     modifier = Modifier.fillMaxWidth()
                                         .padding(top = 40.dp)
                                         .clip(MaterialTheme.shapes.small)
                                         .background(Color(0XFFF3F4F5))
                                         .clickable(true) {
                                             uriHandler.openUri("https://abitua.org")
                                         }
                                         .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     Image(
                                         painter = painterResource(Res.drawable.abitua),
                                         contentDescription = null,
                                         modifier = Modifier
                                             .padding(10.dp)
                                             .size(40.dp)
                                             .clip(MaterialTheme.shapes.extraSmall)
                                     )
                                     Column(Modifier.padding(start = 5.dp)) {
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_info_abitua_title),
                                             style = MaterialTheme.typography.bodyMedium
                                         )
                                         Text(
                                             text = stringResource(Res.string.settings_dialog_info_abitua_verbose),
                                             style = MaterialTheme.typography.bodySmall,
                                             modifier = Modifier.padding(top = 2.dp),
                                             color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                         )
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
}

@Composable
private fun SettingsPageButton(
    text: String,
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) MaterialTheme.colorScheme.onBackground.copy(0.15F)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.7f))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
        )
    }
}

private enum class SettingsPage {
    STORAGE,
    APPEARANCE,
    INFO
}
