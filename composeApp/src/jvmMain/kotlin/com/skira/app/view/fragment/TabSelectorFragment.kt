package com.skira.app.view.fragment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.icon_add
import org.jetbrains.compose.resources.painterResource

@Composable
fun TabSelectorFragment() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 10.dp, end = 10.dp, start = 10.dp)
    ) {
        // TEMPORARY SHOULD ALLOW ADDING TABS
        Button(
            onClick = {},
            border = BorderStroke((1.5.dp), MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            elevation = buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            modifier = Modifier.weight(1f, fill = true)
                .height(37.dp)
                .padding(end = 10.dp)
        ) {
            // TEMPORARY MAKE INPUT FIELD
            Text(
                text = "New plot",
                color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = {},
            border = BorderStroke((1.5.dp), MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            elevation = buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            modifier = Modifier.size(37.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_add),
                contentDescription = null
            )
        }
    }
}