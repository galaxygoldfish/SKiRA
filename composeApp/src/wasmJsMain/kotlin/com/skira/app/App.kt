package com.skira.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import skira.composeapp.generated.resources.Res
import skira.composeapp.generated.resources.compose_multiplatform
import skira.composeapp.generated.resources.skira_logo

@Composable
fun App() {
    SKiRATheme {
        var showWelcomeDialog by remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(75.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.skira_logo),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 20.dp)
                            .size(45.dp)
                    )
                    Text(
                        text = "SKiRA",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(start = 20.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        ) {

        }
    }
}