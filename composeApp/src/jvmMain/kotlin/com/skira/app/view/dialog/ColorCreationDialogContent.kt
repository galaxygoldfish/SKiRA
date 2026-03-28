package com.skira.app.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.components.MinimalIconButton
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.color_creation_dialog_add_step
import com.skira.app.composeapp.generated.resources.color_creation_dialog_color_strong
import com.skira.app.composeapp.generated.resources.color_creation_dialog_color_weak
import com.skira.app.composeapp.generated.resources.color_creation_dialog_save
import com.skira.app.composeapp.generated.resources.color_creation_dialog_title
import com.skira.app.composeapp.generated.resources.icon_add
import com.skira.app.composeapp.generated.resources.icon_trash
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.PreferenceManager
import com.skira.app.utilities.parseHexToColor
import com.skira.app.utilities.safeGradientColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import javax.swing.JColorChooser
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import com.skira.app.composeapp.generated.resources.icon_close

@Composable
fun ColorCreationDialogContent(
    schemeIndex: Int? = null,
    onClose: () -> Unit = {}
) {
    val storedAll = PreferenceManager.getColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES).toMutableList()
    val initialScheme = remember(schemeIndex) {
        schemeIndex?.takeIf { it >= 0 && it < storedAll.size }?.let { storedAll[it] } ?: emptyList()
    }
    val steps = remember(schemeIndex, initialScheme) {
        mutableStateListOf<String>().apply { addAll(initialScheme) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(0.4F)
            .animateContentSize()
            .padding(end = 10.dp, start = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.color_creation_dialog_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MinimalIconButton(
                    onClick = {
                        onClose()
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
                Text(
                    text = "esc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                )
            }
        }
        val previewColors = steps.map { parseHexToColor(it).takeIf { c -> c != Color.Unspecified } ?: Color.LightGray }
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 30.dp)
                .height(36.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                    shape = MaterialTheme.shapes.extraSmall
                )
                .background(Brush.horizontalGradient(safeGradientColors(previewColors)))
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.color_creation_dialog_color_weak),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
            )
            Text(
                text = stringResource(Res.string.color_creation_dialog_color_strong),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val screenHeightDp = with(androidx.compose.ui.platform.LocalDensity.current) {
            java.awt.Toolkit.getDefaultToolkit().screenSize.height.toDp()
        }
        Column(
            modifier = Modifier
                .heightIn(max = screenHeightDp * 0.8f)
                .verticalScroll(rememberScrollState())
        ) {
            steps.forEachIndexed { idx, colorHex ->
                val currentColor = parseHexToColor(colorHex).takeIf { it != Color.Unspecified } ?: Color.Gray
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(currentColor.copy(0.3F))
                            .clickable {
                                // custom color picker to be added in the future, for now using system one
                                val picked = pickColor(colorHex)
                                if (picked != null) {
                                    if (idx in steps.indices) steps[idx] = picked
                                }
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                                    .size(height = 30.dp, width = 50.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(currentColor)
                            )
                            // In future making this editable to type own hex codes
                            Text(
                                text = "#",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                            )
                            Text(
                                text = colorHex.substring(1),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 5.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(
                                modifier = Modifier.size(width = 1.dp, height = 30.dp)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(0.2F))
                            )
                            MinimalIconButton(
                                onClick = { if (idx in steps.indices) steps.removeAt(idx) },
                                icon = {
                                    Image(
                                        painter = painterResource(Res.drawable.icon_trash),
                                        contentDescription = null,
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                            MaterialTheme.colorScheme.onBackground.copy(
                                                0.4F
                                            )
                                        )
                                    )
                                },
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            MinimalIconButton(
                onClick = { steps.add("#9999FF") },
                icon = {
                    Row(
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_add),
                            contentDescription = null,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                MaterialTheme.colorScheme.onBackground.copy(0.6F)
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(Res.string.color_creation_dialog_add_step),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                            modifier = Modifier.padding(start = 5.dp, end = 10.dp)
                        )
                    }
                },
                smallSize = false
            )

            ActionTextButton(
                text = stringResource(Res.string.color_creation_dialog_save),
                onClick = {
                    val all = PreferenceManager.getColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES).toMutableList()
                    if (schemeIndex != null && schemeIndex >= 0 && schemeIndex < all.size) {
                        all[schemeIndex] = steps.toList()
                    } else {
                        all.add(steps.toList())
                    }
                    PreferenceManager.putColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES, all)
                    onClose()
                },
                color = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}

private fun pickColor(initialHex: String): String? {
    val initialCompose = parseHexToColor(initialHex)
    val r = (initialCompose.red * 255).toInt().coerceIn(0, 255)
    val g = (initialCompose.green * 255).toInt().coerceIn(0, 255)
    val b = (initialCompose.blue * 255).toInt().coerceIn(0, 255)
    val initial = java.awt.Color(r, g, b)
    val chosen = JColorChooser.showDialog(null, "Pick color", initial)
    return chosen?.let { String.format("#%02X%02X%02X", it.red, it.green, it.blue) }
}
