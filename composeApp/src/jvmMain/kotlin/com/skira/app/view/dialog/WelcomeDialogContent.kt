package com.skira.app.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.composeapp.generated.resources.*
import com.skira.app.structures.DialogType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skia.FontFeature

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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = stringResource(Res.string.welcome_dialog_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                modifier = Modifier.padding(top = 5.dp)
            )
            val message = stringResource(Res.string.welcome_dialog_message)
            val italicText = "(Nothobranchius furzeri)"
            Text(
                text = buildAnnotatedString {
                    val parts = message.split(italicText)
                    append(parts.getOrNull(0) ?: "")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                    if (parts.size > 1) {
                        append(parts.subList(1, parts.size).joinToString(italicText))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 17.dp).fillMaxWidth(0.33F)
            )

            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.33F)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_scrna),
                    contentDescription = null,
                    modifier = Modifier.padding(17.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = stringResource(Res.string.welcome_dialog_interactive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F)
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(0.33F)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_dna),
                    contentDescription = null,
                    modifier = Modifier.padding(17.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = stringResource(Res.string.welcome_dialog_visualize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F)
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(0.33F)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_magic_wand),
                    contentDescription = null,
                    modifier = Modifier.padding(17.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = stringResource(Res.string.welcome_dialog_customize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7F)
                )
            }

           val pre = stringResource(Res.string.welcome_dialog_data_collection)
           val mid = stringResource(Res.string.welcome_dialog_at_the)
           val abituaLab = stringResource(Res.string.welcome_dialog_abitua_lab)
           val deptGenome = stringResource(Res.string.welcome_dialog_genome)

           val abituaUrl = "https://abitua.org"
           val deptGenomeUrl = "https://www.gs.washington.edu/"

           val annotated = buildAnnotatedString {
               append(pre)
               pushStringAnnotation(tag = "URL", annotation = abituaUrl)
               withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                   append(abituaLab)
               }
               pop()
               append(mid)
               pushStringAnnotation(tag = "URL", annotation = deptGenomeUrl)
               withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                   append(deptGenome)
               }
               pop()
           }

          ClickableText(
               text = annotated,
               style = MaterialTheme.typography.labelMedium.copy(
                   color = MaterialTheme.colorScheme.onBackground.copy(0.8F)
               ),
               modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.33F),
               onClick = { offset ->
                   annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                       .firstOrNull()?.let { uriHandler.openUri(it.item) }
               }
           )
            ActionTextButton(
                text = stringResource(Res.string.welcome_dialog_start_button),
                icon = painterResource(Res.drawable.icon_arrow_end),
                contentDescription = null,
                onClick = { onNavigationRequest(DialogType.DOWNLOAD_ONBOARD) },
                color = MaterialTheme.colorScheme.onBackground,
                filled = true,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}