package com.skira.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import skira.composeapp.generated.resources.Res
import skira.composeapp.generated.resources.compose_multiplatform
import skira.composeapp.generated.resources.icon_arrow_end
import skira.composeapp.generated.resources.skira_logo

@Composable
fun App() {
    SKiRATheme {
        var showWelcomeDialog by remember { mutableStateOf(true) }
        Box(modifier = Modifier.fillMaxSize()) {
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
            AnimatedVisibility(
                visible = showWelcomeDialog,
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2F)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.skira_logo),
                            contentDescription = null,
                            modifier = Modifier.padding(30.dp)
                                .size(60.dp)
                        )
                        Column(modifier = Modifier.padding(30.dp)) {
                            Text(
                                text = "Welcome to SKiRA",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "single-cell killifish RNA atlas",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            Text(
                                text = "SKiRA is a visualization & plotting tool to explore scRNA-seq data collected from N. furzeri embryos at different timepoints post-fertilization. Data collection and development by the Abitua Lab at the University of Washington Department of Genome Sciences.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.33F)
                            )
                            // don't show this again checkbox
                            Row(
                                modifier = Modifier.fillMaxWidth(0.33F)
                                    .padding(top = 30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ActionTextButton(
                                    text = "Start",
                                    icon = painterResource(Res.drawable.icon_arrow_end),
                                    contentDescription = null,
                                    onClick = { showWelcomeDialog = false },
                                    color = MaterialTheme.colorScheme.onBackground,
                                    filled = true
                                )
                                ActionTextButton(
                                    text = "About the Abitua Lab",
                                    onClick = { showWelcomeDialog = false },
                                    color = MaterialTheme.colorScheme.outline,
                                    filled = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}