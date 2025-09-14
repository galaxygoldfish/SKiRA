package com.skira.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
// ... existing code ...
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skira.app.components.ActionTextButton
import com.skira.app.components.Checkbox
import com.skira.app.components.DropdownSelector
import com.skira.app.components.ShimmerPlaceholder
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.fetch.RequestInit


@Serializable
private data class AssayMeta(
    val genes: List<String> = emptyList(),
    val timepoints: List<String> = emptyList()
)

sealed interface PlotViewState {
    data object Loading : PlotViewState
    data object SelectGeneAndTime : PlotViewState
    data object SelectGene : PlotViewState
    data object SelectTime : PlotViewState
    data object Success : PlotViewState
    data object Ready : PlotViewState
    data class Error(val message: String) : PlotViewState
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    SKiRATheme {
        var showWelcomeDialog by remember {
            mutableStateOf(!(window.localStorage.getItem("showWelcomeDialog")?.toBoolean() ?: false))
        }
        val uriHandler = LocalUriHandler.current
        val scope = rememberCoroutineScope()

        // Simple inputs for demo; replace with real UI state as needed
        var gene by remember { mutableStateOf("Select") }
        var timepoint by remember { mutableStateOf("Select") }

        var genes by remember { mutableStateOf<List<String>>(emptyList()) }
        var timepoints by remember { mutableStateOf<List<String>>(emptyList()) }

        var plotBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var isLoadingPlot by remember { mutableStateOf(false) }
        var isLoadingMeta by remember { mutableStateOf(false) }
        var loadError by remember { mutableStateOf<String?>(null) }

        var jobId by remember { mutableStateOf<String?>(null) }
        var progress by remember { mutableStateOf(0) }

        val viewState = remember(isLoadingPlot, gene, timepoint, plotBitmap, loadError) {
            computePlotViewState(isLoadingPlot, gene, timepoint, plotBitmap, loadError)
        }

        suspend fun startPlotJob() {
            if (gene == "Select" || timepoint == "Select") return
            progress = 0
            jobId = null
            plotBitmap = null
            loadError = null
            isLoadingPlot = true
            try {
                val startResp = window.fetch(
                    "http://127.0.0.1:8081/startPlot?gene=$gene&timepoint=$timepoint"
                ).await<Response>()
                if (!startResp.ok) {
                    loadError = "Failed to start job: ${startResp.status} ${startResp.statusText}"
                    return
                }
                val startText = startResp.text().await<JsString>().toString()
                val id = Regex("\"jobId\"\\s*:\\s*\"([^\"]+)\"").find(startText)?.groupValues?.get(1)
                if (id == null) {
                    loadError = "No jobId returned"
                    return
                }
                jobId = id

                while (true) {
                    delay(400)
                    val progResp = window.fetch("http://127.0.0.1:8081/progress?id=$id").await<Response>()
                    if (!progResp.ok) {
                        loadError = "Progress error: ${progResp.status}"
                        break
                    }
                    val progText = progResp.text().await<JsString>().toString()
                    val pct = Regex("\"progress\"\\s*:\\s*(\\d+)").find(progText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val done = Regex("\"done\"\\s*:\\s*(true|false)").find(progText)?.groupValues?.get(1)?.toBoolean() ?: false
                    val hasImage = Regex("\"hasImage\"\\s*:\\s*(true|false)").find(progText)?.groupValues?.get(1)?.toBoolean() ?: false
                    progress = pct
                    if (done) {
                        if (hasImage) {
                            val imgResp = window.fetch("http://127.0.0.1:8081/image?id=$id").await<Response>()
                            if (!imgResp.ok) {
                                loadError = "Image error: ${imgResp.status}"
                            } else {
                                val buffer = imgResp.arrayBuffer().await<ArrayBuffer>()
                                val bytes = Uint8Array(buffer)
                                val byteArray = bytes.toUByteArray().toByteArray()
                                val skiaImage = SkiaImage.makeFromEncoded(byteArray)
                                plotBitmap = skiaImage.toComposeImageBitmap()
                            }
                        } else if (loadError == null) {
                            loadError = "Job finished without image"
                        }
                        break
                    }
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

                    val parsedGenes = obj["genes"]?.jsonArray
                        ?.map { it.jsonPrimitive.contentOrNull ?: it.toString() }
                        ?: emptyList()

                    val parsedTimepoints = obj["timepoints"]?.jsonArray
                        ?.map { it.jsonPrimitive.contentOrNull ?: it.toString() }
                        ?: emptyList()

                    genes = listOf("Select") + parsedGenes
                    timepoints = listOf("Select") + parsedTimepoints
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Row(
                            modifier = Modifier.padding(end = 20.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color.White)
                        ) {
                            Text(
                                text = "BETA",
                                style = MaterialTheme.typography.headlineLarge + TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(40.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(end = 40.dp).weight(1F, fill = true)) {
                            AnimatedContent(
                                targetState = !isLoadingMeta
                                        && timepoints.isNotEmpty()
                                        && genes.isNotEmpty()
                            ) { doneLoading ->
                                Column {
                                    if (doneLoading) {
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
                                        val selectionsComplete = gene != "Select" && timepoint != "Select"
                                        val canClick = selectionsComplete && !isLoadingPlot
                                        AnimatedContent(targetState = canClick) {
                                            Button(
                                                onClick = { scope.launch { startPlotJob() } },
                                                shape = MaterialTheme.shapes.extraSmall,
                                                enabled = it,
                                                colors = if (it) {
                                                    ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary.copy(0.6F),
                                                        contentColor = MaterialTheme.colorScheme.onBackground
                                                    )
                                                } else {
                                                    ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                        contentColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.5f
                                                        )
                                                    )
                                                },
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp,
                                                    focusedElevation = 0.dp,
                                                    hoveredElevation = 0.dp
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp),
                                                modifier = Modifier.padding(top = 40.dp)
                                                    .width(300.dp)
                                            ) {
                                                Text(
                                                    text = "Go",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(start = 5.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        ShimmerPlaceholder(height = 20.dp, width = 200.dp)
                                        ShimmerPlaceholder(
                                            height = 40.dp,
                                            width = 300.dp,
                                            modifier = Modifier.padding(top = 15.dp)
                                        )
                                        ShimmerPlaceholder(
                                            height = 20.dp,
                                            width = 200.dp,
                                            modifier = Modifier.padding(top = 40.dp)
                                        )
                                        ShimmerPlaceholder(
                                            height = 40.dp,
                                            width = 300.dp,
                                            modifier = Modifier.padding(top = 15.dp)
                                        )
                                        ShimmerPlaceholder(
                                            height = 40.dp,
                                            width = 300.dp,
                                            modifier = Modifier.padding(top = 40.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 40.dp).weight(2F, fill = true),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedContent(
                                targetState = Pair(viewState, isLoadingMeta),
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) { target ->
                                if (target.second) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Loading metadata",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                        )
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(0.5F)
                                                .height(17.dp),
                                            trackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                                            color = Color(0XFFC7CED7)
                                        )
                                    }
                                } else {
                                    when (target.first) {
                                        PlotViewState.Loading -> {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Generating plot",
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val target = (progress.coerceIn(0, 100)) / 100f
                                                    val animated by animateFloatAsState(
                                                        targetValue = target,
                                                        animationSpec = tween(durationMillis = 450),
                                                        label = "progressAnim"
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { animated },
                                                        modifier = Modifier.fillMaxWidth(0.5F)
                                                            .height(17.dp),
                                                        trackColor = MaterialTheme.colorScheme.onBackground.copy(0.05F),
                                                        color = Color(0XFFC7CED7),
                                                        drawStopIndicator = {}
                                                    )
                                                    Text(
                                                        text = "$progress%",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                                        modifier = Modifier.padding(start = 20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        PlotViewState.SelectGene -> {
                                            Text(
                                                text = "Choose a gene to view plot",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                            )
                                        }

                                        PlotViewState.SelectTime -> {
                                            Text(
                                                text = "Choose a timepoint to view plot",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                            )
                                        }

                                        PlotViewState.SelectGeneAndTime -> {
                                            Text(
                                                text = "Choose a gene and timepoint to view plot",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                            )
                                        }

                                        PlotViewState.Ready -> {
                                            Text(
                                                text = "Ready",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F)
                                            )
                                        }

                                        is PlotViewState.Error -> {
                                            Text(
                                                text = "Unexpected error: $loadError",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.error.copy(0.7F)
                                            )
                                        }

                                        else -> { }
                                    }
                                }
                            }
                            AnimatedContent(targetState = isLoadingPlot) {
                                if (it) {
                                    ShimmerPlaceholder(height = 0.9F, width = 1.0F)
                                } else {
                                    when {
                                        loadError != null -> {
                                            Spacer(
                                                modifier = Modifier.fillMaxHeight(0.9F)
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(MaterialTheme.colorScheme.error.copy(0.2F))
                                            )
                                        }
                                        plotBitmap != null -> Image(
                                            bitmap = plotBitmap!!,
                                            contentDescription = "Plot for $gene at $timepoint",
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        else -> {
                                            Spacer(
                                                modifier = Modifier.fillMaxHeight(0.9F)
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(start = 40.dp)
                                .width(300.dp)
                                .weight(1F, fill = true)
                        ) {}
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
                                        window.localStorage.setItem(
                                            "showWelcomeDialog",
                                            dontShowAgain.toString()
                                        )
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

fun computePlotViewState(
    isLoading: Boolean,
    gene: String,
    timepoint: String,
    plot: ImageBitmap?,
    error: String?
): PlotViewState {
    if (isLoading) return PlotViewState.Loading
    error?.let { return PlotViewState.Error(it) }
    val geneSelected = gene != "Select"
    val timeSelected = timepoint != "Select"
    return when {
        !geneSelected && !timeSelected -> PlotViewState.SelectGeneAndTime
        !geneSelected && timeSelected -> PlotViewState.SelectGene
        geneSelected && !timeSelected -> PlotViewState.SelectTime
        plot != null -> PlotViewState.Success
        geneSelected && timeSelected -> PlotViewState.Ready
        else -> PlotViewState.SelectGeneAndTime
    }
}
