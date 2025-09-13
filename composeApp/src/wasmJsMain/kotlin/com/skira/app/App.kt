package com.skira.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
// ... existing code ...
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.skira.app.components.ActionTextButton
import com.skira.app.components.Checkbox
import com.skira.app.components.DropdownSelector
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.jetbrains.compose.resources.painterResource
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.toUByteArray
import org.w3c.fetch.Response
import org.jetbrains.skia.Image as SkiaImage
import skira.composeapp.generated.resources.Res
import skira.composeapp.generated.resources.icon_arrow_end
import skira.composeapp.generated.resources.skira_logo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import skira.composeapp.generated.resources.icon_arrow_down
import kotlin.js.Promise

@Serializable
private data class AssayMeta(
    val genes: List<String> = emptyList(),
    val timepoints: List<String> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    SKiRATheme {
        var showWelcomeDialog by remember {
            mutableStateOf(!(window.localStorage.getItem("showWelcomeDialog")?.toBoolean() ?: false))
        }
        val uriHandler = LocalUriHandler.current

        // Simple inputs for demo; replace with real UI state as needed
        var gene by remember { mutableStateOf("lcp1") }
        var timepoint by remember { mutableStateOf("52hpf") }

        var genes by remember { mutableStateOf<List<String>>(emptyList()) }
        var timepoints by remember { mutableStateOf<List<String>>(emptyList()) }

        var plotBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var isLoadingPlot by remember { mutableStateOf(false) }
        var isLoadingMeta by remember { mutableStateOf(false) }
        var loadError by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(gene, timepoint) {
            isLoadingPlot = true
            loadError = null
            plotBitmap = null
            try {
                val resp = window.fetch("http://127.0.0.1:8081/plot?gene=$gene&timepoint=$timepoint").await<Response>()
                if (!resp.ok) {
                    loadError = "Request failed: ${resp.status} ${resp.statusText}"
                } else {
                    val buffer = resp.arrayBuffer().await<ArrayBuffer>()
                    val bytes = Uint8Array(buffer)
                    val byteArray = bytes.toUByteArray().toByteArray()
                    val skiaImage = SkiaImage.makeFromEncoded(byteArray)
                    plotBitmap = skiaImage.toComposeImageBitmap()
                }
            } catch (t: Throwable) {
                loadError = t.message ?: "Unknown error"
            } finally {
                isLoadingPlot = false
            }
        }

        LaunchedEffect(true) {
            isLoadingMeta = true
            loadError = null
            try {
                val resp = window.fetch("http://127.0.0.1:8081/getAssayMeta").await<Response>()
                if (!resp.ok) {
                    loadError = "Request failed: ${resp.status} ${resp.statusText}"
                } else {

                    val clone = resp.clone()

                    // Type the promise explicitly, then await
                    val textPromise: Promise<JsString> = clone.text()
                    val text = textPromise.await<JsString>().toString()

                    // Tolerant parsing: coerce any array items to strings to avoid illegal cast
                    val json = Json { ignoreUnknownKeys = true }
                    val root = json.parseToJsonElement(text)
                    val obj = root.jsonObject

                    genes = obj["genes"]?.jsonArray
                        ?.map { it.jsonPrimitive.contentOrNull ?: it.toString() }
                        ?: emptyList()

                    timepoints = obj["timepoints"]?.jsonArray
                        ?.map { it.jsonPrimitive.contentOrNull ?: it.toString() }
                        ?: emptyList()
                }
            } catch (t: Throwable) {
                loadError = t.message ?: "Unknown error"
            } finally {
                isLoadingMeta = false
            }

        }

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
                            modifier = Modifier.padding(start = 30.dp)
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
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row {
                        Column(modifier = Modifier.padding(40.dp)) {
                            if (!isLoadingMeta && timepoints.isNotEmpty()) {
                                Row {
                                    Text(
                                        text = "Timepoint",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Text(
                                        text = "(hours post-fertilization)",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.5F),
                                        modifier = Modifier.padding(start = 5.dp)
                                    )
                                }
                                Box {
                                    DropdownSelector(
                                        selectedItem = timepoint,
                                        onSelectionChange = { timepoint = it },
                                        availableItems = timepoints
                                    )
                                }
                                Text(
                                    text = "Gene",
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(top = 40.dp)
                                )
                                Box {
                                    DropdownSelector(
                                        selectedItem = gene,
                                        onSelectionChange = { gene = it },
                                        availableItems = genes,
                                        searchable = true
                                    )
                                }
                            }
                        }
                        Column {
                            when {
                                isLoadingPlot -> Text("Loading plot...", style = MaterialTheme.typography.labelLarge)
                                loadError != null -> Text(
                                    text = "Error: $loadError",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFB00020)
                                )

                                plotBitmap != null -> Image(
                                    bitmap = plotBitmap!!,
                                    contentDescription = "Plot for $gene at $timepoint",
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )

                                else -> Text("No image")
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showWelcomeDialog,
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                var dontShowAgain by remember { mutableStateOf(false) }
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
                        Column(modifier = Modifier.padding(top = 30.dp, end = 40.dp, bottom = 30.dp)) {
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
                            Row(
                                modifier = Modifier.padding(top = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checkedState = dontShowAgain,
                                    onCheckChange = {
                                        dontShowAgain = it
                                        window.localStorage.setItem("showWelcomeDialog", dontShowAgain.toString())
                                    }
                                )
                                Text(
                                    text = "Don't show this again",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(0.33F)
                                    .padding(top = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ActionTextButton(
                                    text = "Start",
                                    icon = painterResource(Res.drawable.icon_arrow_end),
                                    contentDescription = null,
                                    onClick = {
                                        showWelcomeDialog = false
                                    },
                                    color = MaterialTheme.colorScheme.onBackground,
                                    filled = true
                                )
                                ActionTextButton(
                                    text = "About the Abitua Lab",
                                    onClick = { uriHandler.openUri("https://abitua.org") },
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