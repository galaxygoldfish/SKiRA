package com.skira.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image

@Composable
fun ActionTextButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    contentDescription: String? = null,
    filled: Boolean = true,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) color else MaterialTheme.colorScheme.background,
            contentColor = if (filled) MaterialTheme.colorScheme.background else color
        ),
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp, start = 15.dp, end = 15.dp),
        border = if (filled) null else BorderStroke(width = 1.5.dp, color = color),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) MaterialTheme.colorScheme.background else color
        )
        icon?.let {
            Image(
                painter = it,
                contentDescription = contentDescription,
                modifier = Modifier.padding(start = 10.dp)
                    .size(10.dp),
                colorFilter = ColorFilter.tint(if (filled) MaterialTheme.colorScheme.background else color)
            )
        }
    }
}

@Composable
fun LargeTextButton(text: String, onClick: () -> Unit) {}
