package com.skira.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skira.app.r.PlotWorker.requestMetadata
import com.skira.app.r.PlotWorker.runPlot
import com.skira.app.structures.*
import com.skira.app.utilities.PreferenceManager
import com.skira.app.utilities.isRunningOnMac
import com.skira.app.utilities.verifySelectedDataset
import kotlinx.coroutines.launch
import org.apache.batik.transcoder.SVGAbstractTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.jetbrains.skia.Image
import java.awt.Color
import java.awt.Desktop
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import javax.imageio.ImageIO

/**
 * Corresponding ViewModel for the HomeView composable (which encompasses all the plot generation features)
 */
class HomeViewModel : ViewModel() {

    private val umapAxesSvgBytes: ByteArray? by lazy { loadUmapAxesSvgBytes() }
    private val preferredTitleBaseFont: Font? by lazy { loadTitleFontFromResources() }

    /* Serves as the dialog navigation controller. Set to DialogType.NONE to hide dialog */
    var currentDialogToShow by mutableStateOf(DialogType.WELCOME)

    /* Serves as a tab navigation controller (+ other tab parameters) */
    var currentTabInView by mutableStateOf(0)
    /* Counter for issuing unique ids */
    private var nextTabId = 1L

    /* Represent each tab as a stable object with a unique id to avoid duplicate key issues in Lazy lists */
    data class TabEntry(
        val id: Long,
        val title: String = "New plot",
        val currentGene: String = "Select",
        val currentTimepoint: String = "Select",
        val selectedGene: String = "Select",
        val selectedTimepoint: String = "Select",
        val plotBitmap: ImageBitmap? = null,
        val dimPlotBitmap: ImageBitmap? = null,
        val currentExpressionPlotColor: String = PlotColor.Plasma,
        val currentDimPlotColor: Int = 0,
        val expressionPlotDpi: Int = 140,
        val cellTypePlotDpi: Int = 140,
        val cellTypeLabelFontSizePx: Int = 6,
        val showExpressionClusterLabels: Boolean = false,
        val showDimPlotClusterLabels: Boolean = true
        // isLoading ??
    )

    /* All current tabs to be displayed */
    var tabEntryList = mutableStateListOf(TabEntry(nextTabId++))

    /* Actively selected gene and timepoints (exactly what the UI is currently showing) */
    var currentGene by mutableStateOf("Select")
    var currentTimepoint by mutableStateOf("Select")

    /* Saving the value of the timepoint and gene that was selected at click of the "go" button */
    var selectedTimepoint by mutableStateOf(currentTimepoint)
    var selectedGene by mutableStateOf(currentGene)

    /* Lists of available genes and timepoints for the user to choose from */
    var metadataGeneList by mutableStateOf<List<String>>(emptyList())
    var metadataTimepointList by mutableStateOf<List<String>>(emptyList())

    /* Bitmap representations of the most recently generated version of each plot */
    var plotBitmap by mutableStateOf<ImageBitmap?>(null)
    var dimPlotBitmap by mutableStateOf<ImageBitmap?>(null)

    /* Status indicators for the plot worker */
    var isLoadingPlot by mutableStateOf(false)
    var isLoadingMeta by mutableStateOf(false)
    var loadError by mutableStateOf<String?>(null)
    var plotGenerationTaskProgress by mutableStateOf(0)
    var metadataLoadingProgress by mutableStateOf(0)

    /* Whether we are waiting for the system to open the image viewer app for each plot */
    var pendingOpenDimPlot by mutableStateOf(false)
    var pendingOpenFeaturePlot by mutableStateOf(false)

    /* Currently selected visual pixel per inch density to use when generating plots */
    var expressionPlotDpi by mutableStateOf(140)
    var cellTypePlotDpi by mutableStateOf(140)
    var cellTypeLabelFontSizePx by mutableStateOf(6)

    /* Colormaps to use when generating each plot. [PlotColor] gives the supported color schemes */
    var currentExpressionPlotColor by mutableStateOf(PlotColor.Plasma)
    /* 0 corresponds to color by cell type and 1 corresponds to color by timepoint (only in merge) */
    var currentDimPlotColor by mutableStateOf(0)

    /* Whether we are showing the download options for each plot */
    var showingExpressionDownloadMenu by mutableStateOf(false)
    var showingDimPlotDownloadMenu by mutableStateOf(false)

    /* Which plot/format should be preselected when opening export dialog */
    var exportDialogForFeaturePlot by mutableStateOf(true)
    var exportDialogPreferredFormat by mutableStateOf(DownloadFormat.PNG)

    /* Individual active states for each plot's download status */
    var expressionPlotDownloadState by mutableStateOf(PlotDownloadState.IDLE)
    var dimPlotDownloadState by mutableStateOf(PlotDownloadState.IDLE)

    /* Whether we should label cell types when generating each type of plot */
    var showExpressionClusterLabels by mutableStateOf(false)
    var showDimPlotClusterLabels by mutableStateOf(true)

    var currentSidebarPage by mutableStateOf(SidebarPage.DEFAULT)

    var sidebarMinimized by mutableStateOf(false)

    /* This tells us what to show the user in the status bar (e.g. if ready to generate the plot) */
    val viewState get() = computePlotViewState()

    /**
     * Determines the current view state of the plot area based on loading status
     * This lets us determine what to tell user about status of plot generation
     */
    fun computePlotViewState(): PlotViewState {
        if (isLoadingPlot) {
            return PlotViewState.Loading
        }
        loadError?.let {
            return PlotViewState.Error(it)
        }
        val geneSelected = currentGene != "Select"
        val timeSelected = currentTimepoint != "Select"
        return when {
            !geneSelected && !timeSelected -> PlotViewState.SelectGeneAndTime
            !geneSelected && timeSelected -> PlotViewState.SelectGene
            geneSelected && !timeSelected -> PlotViewState.SelectTime
            else -> PlotViewState.Ready
        }
    }

    fun addTabAndSwitch() {
        tabEntryList.add(TabEntry(nextTabId++))
        onSwitchTab(tabEntryList.size - 1)
    }

    private fun persistCurrentStateIntoTab(index: Int) {
        if (index !in tabEntryList.indices) return
        val prev = tabEntryList[index]
        tabEntryList[index] = prev.copy(
            currentGene = currentGene,
            currentTimepoint = currentTimepoint,
            selectedGene = selectedGene,
            selectedTimepoint = selectedTimepoint,
            plotBitmap = plotBitmap,
            dimPlotBitmap = dimPlotBitmap,
            currentExpressionPlotColor = currentExpressionPlotColor,
            currentDimPlotColor = currentDimPlotColor,
            expressionPlotDpi = expressionPlotDpi,
            cellTypePlotDpi = cellTypePlotDpi,
            cellTypeLabelFontSizePx = cellTypeLabelFontSizePx,
            showExpressionClusterLabels = showExpressionClusterLabels,
            showDimPlotClusterLabels = showDimPlotClusterLabels
        )
    }

    private fun applyTabState(entry: TabEntry) {
        currentGene = entry.currentGene
        currentTimepoint = entry.currentTimepoint
        selectedGene = entry.selectedGene
        selectedTimepoint = entry.selectedTimepoint
        plotBitmap = entry.plotBitmap
        dimPlotBitmap = entry.dimPlotBitmap
        currentExpressionPlotColor = entry.currentExpressionPlotColor
        currentDimPlotColor = entry.currentDimPlotColor
        expressionPlotDpi = entry.expressionPlotDpi
        cellTypePlotDpi = entry.cellTypePlotDpi
        cellTypeLabelFontSizePx = entry.cellTypeLabelFontSizePx
        showExpressionClusterLabels = entry.showExpressionClusterLabels
        showDimPlotClusterLabels = entry.showDimPlotClusterLabels
    }

    fun removeTabById(id: Long) {
        val removedIndex = tabEntryList.indexOfFirst { it.id == id }
        if (removedIndex < 0) return
        val wasSelected = currentTabInView == removedIndex
        tabEntryList.removeAt(removedIndex)
        currentTabInView = when {
            tabEntryList.isEmpty() -> 0
            wasSelected -> (removedIndex - 1).coerceAtLeast(0)
            currentTabInView > removedIndex -> currentTabInView - 1
            else -> currentTabInView
        }

        if (wasSelected && currentTabInView in tabEntryList.indices) {
            applyTabState(tabEntryList[currentTabInView])
        }
    }

    fun moveTab(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in tabEntryList.indices || toIndex !in tabEntryList.indices || fromIndex == toIndex) return
        val moved = tabEntryList.removeAt(fromIndex)
        tabEntryList.add(toIndex, moved)

        currentTabInView = when {
            currentTabInView == fromIndex -> toIndex
            fromIndex < toIndex && currentTabInView in (fromIndex + 1)..toIndex -> currentTabInView - 1
            fromIndex > toIndex && currentTabInView in toIndex until fromIndex -> currentTabInView + 1
            else -> currentTabInView
        }
    }

    fun onSwitchTab(index: Int) {
        if (index < 0 || index >= tabEntryList.size) return
        if (index == currentTabInView) return
        val prevIndex = currentTabInView
        persistCurrentStateIntoTab(prevIndex)
        val entry = tabEntryList[index]
        currentTabInView = index
        applyTabState(entry)
     }

    /**
     * Used to verify that the dataset folder set in preferences still exists and contains the necessary files
     *
     * @return true if the dataset exists and is valid, false otherwise
     */
    fun verifyThatDatasetExists(): Boolean {
        val base = PreferenceManager.getString(PreferenceKey.R_DATASET_FOLDER) ?: return false
        val datasetDir = File(base)
        if (!datasetDir.isDirectory) return false
        return verifySelectedDataset(datasetDir.absolutePath)
    }

    /**
     * Determines whether the user is ready to interact with the main features of the app and
     * shows onboarding process if not. The user is ready to interact only when they have the
     * necessary dataset files still present, and they have completed onboarding
     */
    fun determineOnboardingStatus() {
        val onboardingComplete = PreferenceManager.getBoolean(PreferenceKey.ONBOARDING_COMPLETE)
        currentDialogToShow = if (!onboardingComplete || !verifyThatDatasetExists()) {
            DialogType.WELCOME
        } else {
            DialogType.NONE
        }
    }

    /**
     * Computes whether metadata should be loaded from R based on whether the onboarding
     * process is complete, dataset exists, no plot is currently shown, and no metadata
     * has been loaded yet
     *
     * @return true if it is an appropriate time to load metadata, false otherwise
     */
    fun computeShouldLoadMeta(): Boolean {
        return currentDialogToShow == DialogType.NONE &&
                PreferenceManager.getBoolean(PreferenceKey.ONBOARDING_COMPLETE, false) &&
                verifyThatDatasetExists() &&
                dimPlotBitmap == null && plotBitmap == null &&
                metadataTimepointList.isEmpty() && metadataGeneList.isEmpty()
    }

    /**
     * Warms up the R plot worker and loads metadata (genes and timepoints) from the metadata.R script
     * The data is available in [metadataGeneList] and [metadataTimepointList] after successful execution
     * Sets [isLoadingMeta] to true while loading, and sets [loadError] if an error occurs
     */
    fun warmupAndLoadMeta() {
        if (isLoadingMeta || !computeShouldLoadMeta()) return
        isLoadingMeta = true
        metadataLoadingProgress = 0
        loadError = null
        viewModelScope.launch {
            try {
                val meta = requestMetadata(
                    onProgress = { prog ->
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            metadataLoadingProgress = prog
                        }
                    }
                )

                meta.onSuccess {
                    metadataGeneList = listOf("Select") + it.genes
                    metadataTimepointList = listOf("Select") + it.timepoints
                    metadataLoadingProgress = 100
                }.onFailure {
                    loadError = it.message ?: "Failed to load metadata"
                    if (it.message == "R/Rscript not found on PATH or RSCRIPT env") {
                        currentDialogToShow = DialogType.R_INSTALLATION
                    }
                }
            } finally {
                isLoadingMeta = false
            }
        }
    }

    /**
     * Initiates a job to generate the gene expression and cell type plots by querying the R plot worker
     * Upon success, the resulting bitmaps are stored in [plotBitmap] and [dimPlotBitmap]
     * Updates [isLoadingPlot], [plotGenerationTaskProgress], and [loadError] to reflect current status of the job
     *
     * @param gene The gene name to plot
     * @param timepoint The timepoint to plot (hpf)
     * @param expressionDpiParam The integer DPI for the expression plot
     * @param cellTypeDpiParam The integer DPI for the cell type plot
     * @param expressionColorParam The color scheme to use for the expression plot (use [PlotColor] constants)
     * @param dimColorByParam The integer index of the metadata variable to color the cell type plot by
     * @param cellTypeLabelFontSizePxParam Label font size for cell-type labels in the dim plot (in px)
     * @param showDimLabelsParam Whether to show cluster labels on the cell type plot
     * @param showExpressionLabelsParam Whether to show cluster labels on the expression plot
     */
    suspend fun startPlotJob(
        gene: String,
        timepoint: String,
        expressionDpiParam: Int,
        cellTypeDpiParam: Int,
        expressionColorParam: String,
        dimColorByParam: Int,
        cellTypeLabelFontSizePxParam: Int,
        showDimLabelsParam: Boolean,
        showExpressionLabelsParam: Boolean
    ) {
        plotGenerationTaskProgress = 0
        plotBitmap = null
        dimPlotBitmap = null
        loadError = null
        isLoadingPlot = true
        selectedTimepoint = currentTimepoint
        selectedGene = currentGene
        try {
            val result = runPlot(
                gene,
                timepoint,
                expressionDpiParam,
                cellTypeDpiParam,
                expressionColorParam,
                dimColorByParam,
                cellTypeLabelFontSizePxParam,
                showDimLabelsParam,
                showExpressionLabelsParam
            ) { pct -> plotGenerationTaskProgress = pct }
            result.fold(
                onSuccess = { out ->
                    try {
                        val bytes = out.featureBytes
                            ?: run {
                                loadError = "Plot image is missing. The plot worker returned no feature plot."
                                return@fold
                            }
                        if (bytes.isEmpty()) {
                            loadError = "Plot image is empty. The plot worker returned no data."
                            return@fold
                        }
                        val img = Image.makeFromEncoded(bytes)
                        plotBitmap = img.toComposeImageBitmap()
                        out.dimBytes?.let { dimBytes ->
                            if (dimBytes.isNotEmpty()) {
                                runCatching { Image.makeFromEncoded(dimBytes).toComposeImageBitmap() }
                                    .onSuccess { dimPlotBitmap = it }
                            }
                        }
                    } catch (t: Throwable) {
                        loadError = "Failed to decode plot image: ${t.message ?: "Unknown decode error"}"
                    }
                },
                onFailure = { t ->
                    loadError = t.message ?: "Unknown error"
                }
            )
        } finally {
            tabEntryList[currentTabInView] = tabEntryList[currentTabInView].copy(
                title = "$currentGene @ $currentTimepoint"
            )
            isLoadingPlot = false
        }
    }

    /**
     * Opens a given bitmap in the system default image viewer application
     * Note that this function currently causes a slight hiccup in UI smoothness when run
     *
     * @param plotBitmap The bitmap to be opened
     */
    fun openBitmapInSystemImageApp(plotBitmap: ImageBitmap?) {
        if (plotBitmap == null) return
        val tempFile = File.createTempFile("plot", ".png")
        val buffered = plotBitmap.toAwtImage()
        val imageToOpen = runCatching { overlayUmapAxes(buffered) }.getOrElse { buffered }
        ImageIO.write(imageToOpen, "png", tempFile)
        tempFile.deleteOnExit()
        val openedInPreview = if (isRunningOnMac()) {
            runCatching {
                ProcessBuilder("open", "-a", "Preview", tempFile.absolutePath)
                    .start()
                    .waitFor() == 0
            }.getOrDefault(false)
        } else {
            false
        }
        if (!openedInPreview && Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            val opened = runCatching {
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(tempFile)
                    true
                } else {
                    false
                }
            }.getOrDefault(false)

            if (!opened && desktop.isSupported(Desktop.Action.BROWSE)) {
                runCatching { desktop.browse(tempFile.toURI()) }
            }
        }
    }

    fun openExportPlotDialog(isFeaturePlot: Boolean) {
        val useExtendedEdit = PreferenceManager.getBoolean(
            key = PreferenceKey.PREFERENCE_USE_EXTENDED_EDIT_EXPORT,
            default = true
        )

        if (useExtendedEdit) {
            exportDialogForFeaturePlot = isFeaturePlot
            exportDialogPreferredFormat = DownloadFormat.PNG
            currentDialogToShow = DialogType.EXPORT_PLOT
            return
        }

        val image = if (isFeaturePlot) plotBitmap else dimPlotBitmap
        val baseName = if (isFeaturePlot) {
            "${selectedGene}-${selectedTimepoint}"
        } else {
            "cell-types-${selectedTimepoint}"
        }

        // Disabled extended edit means exporting the rendered plot as-is.
        exportBitmapToDownloadDirectory(
            image = image,
            ext = DownloadFormat.PNG,
            baseName = baseName,
            isFeaturePlot = isFeaturePlot,
            includeAxesOverlay = false
        )
    }

    /**
     * Exports a given bitmap to the selected PLOT_DOWNLOAD_PATH in user preferences
     *
     * @param image The bitmap to be exported
     * @param ext The file extension/format to export as (use the PlotDownloadFormat constants)
     * @param baseName The base name (without extension) for the exported file
     * @param isFeaturePlot Whether the plot being exported is a feature plot (true) or dim plot (false)
     *
     * @return true if export was successful, false otherwise
     */
    fun exportBitmapToDownloadDirectory(
        image: ImageBitmap?,
        ext: String,
        baseName: String,
        isFeaturePlot: Boolean,
        includeAxesOverlay: Boolean = true
    ): Boolean {
        val openFolder = PreferenceManager.getBoolean(
            PreferenceKey.PREFERENCE_SHOW_DOWNLOAD_FOLDER,
            true
        )
        val extToUsePre = when (ext) {
            DownloadFormat.PNG -> PlotDownloadState.DOWNLOADING_PNG
            DownloadFormat.JPG -> PlotDownloadState.DOWNLOADING_JPG
            DownloadFormat.PDF -> PlotDownloadState.DOWNLOADING_PDF
            else -> PlotDownloadState.IDLE
        }
        if (isFeaturePlot) {
            expressionPlotDownloadState = extToUsePre
        } else {
            dimPlotDownloadState = extToUsePre
        }
        if (image == null) {
            if (isFeaturePlot) {
                expressionPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
            } else {
                dimPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
            }
            return false
        }

        val extLower = ext.lowercase().removePrefix(".")
        val downloadsDir: Path = Paths.get(
            PreferenceManager.getString(
                key = PreferenceKey.PLOT_DOWNLOAD_PATH,
                default = Paths.get(System.getProperty("user.home"), "Downloads", "SKiRA").toString()
            )!!
        )

        try {
            Files.createDirectories(downloadsDir)
            val target = downloadsDir.resolve("$baseName.$extLower")
            val awtImage = image.toAwtImage()

            val buf = toBufferedImage(awtImage)
            val exportImage = if (includeAxesOverlay) overlayUmapAxes(buf) else buf

            when (extLower) {
                "png" -> {
                    ImageIO.write(exportImage, "png", target.toFile())
                }

                "svg" -> {
                    val pngBytes = ByteArrayOutputStream().use { out ->
                        ImageIO.write(exportImage, "png", out)
                        out.toByteArray()
                    }
                    val encoded = Base64.getEncoder().encodeToString(pngBytes)
                    val svg = """
                        <svg xmlns="http://www.w3.org/2000/svg" width="${exportImage.width}" height="${exportImage.height}" viewBox="0 0 ${exportImage.width} ${exportImage.height}">
                          <image href="data:image/png;base64,$encoded" width="${exportImage.width}" height="${exportImage.height}"/>
                        </svg>
                    """.trimIndent()
                    target.toFile().writeText(svg)
                }

                "jpg", "jpeg" -> {
                    val rgbImage: BufferedImage = if (exportImage.colorModel.hasAlpha()) {
                        val converted = BufferedImage(exportImage.width, exportImage.height, BufferedImage.TYPE_INT_RGB)
                        val g: Graphics2D = converted.createGraphics()
                        g.paint = Color.WHITE
                        g.fillRect(0, 0, converted.width, converted.height)
                        g.drawImage(exportImage, 0, 0, null)
                        g.dispose()
                        converted
                    } else {
                        BufferedImage(exportImage.width, exportImage.height, BufferedImage.TYPE_INT_RGB).also {
                            val g = it.createGraphics()
                            g.drawImage(exportImage, 0, 0, null)
                            g.dispose()
                        }
                    }
                    ImageIO.write(rgbImage, "jpg", target.toFile())
                }

                "pdf" -> {
                    PDDocument().use { doc ->
                        val pdImage = LosslessFactory.createFromImage(doc, exportImage)
                        val pageRect = PDRectangle(exportImage.width.toFloat(), exportImage.height.toFloat())
                        val page = PDPage(pageRect)
                        doc.addPage(page)
                        PDPageContentStream(doc, page).use { cs ->
                            cs.drawImage(pdImage, 0f, 0f, pageRect.width, pageRect.height)
                        }
                        doc.save(target.toFile())
                    }
                }

                else -> {
                    // try writing using ImageIO with given format; fallback to PNG
                    val written = runCatching { ImageIO.write(exportImage, extLower, target.toFile()) }.getOrNull() ?: false
                    if (!written) {
                        ImageIO.write(exportImage, "png", target.toFile())
                    }
                }
            }

            // update download state to success after writing file(s)
            if (isFeaturePlot) {
                expressionPlotDownloadState = PlotDownloadState.DOWNLOAD_SUCCESS
            } else {
                dimPlotDownloadState = PlotDownloadState.DOWNLOAD_SUCCESS
            }

            if (openFolder && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(downloadsDir.toFile())
                } catch (_: IOException) { /* exported successfully; ignore folder-open failure */ }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            if (isFeaturePlot) {
                expressionPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
            } else {
                dimPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
            }
            return false
        }
    }

    fun buildClientExportPreview(
        image: ImageBitmap?,
        showAxes: Boolean,
        showTitle: Boolean,
        transparentBackground: Boolean,
        backgroundHex: String,
        titleText: String,
        invertOverlayColors: Boolean,
        isFeaturePlot: Boolean
    ): ImageBitmap? {
        if (image == null) return null

        val source = toBufferedImage(image.toAwtImage())
        val targetBackground = parseAwtColor(backgroundHex) ?: Color.WHITE
        var rendered = applyClientBackground(
            source = source,
            transparentBackground = transparentBackground,
            backgroundColor = targetBackground
        )

        val overlayColor = if (invertOverlayColors) {
            Color(255, 255, 255, 220)
        } else {
            Color(20, 20, 20, 220)
        }

        if (invertOverlayColors && isFeaturePlot) {
            rendered = recolorExpressionLegendText(rendered, overlayColor)
        }

        if (showAxes) {
            rendered = overlayUmapAxes(rendered, overlayColor)
        }
        if (showTitle && titleText.isNotBlank()) {
            rendered = overlayPlotTitle(rendered, titleText, overlayColor)
        }
        return rendered.toComposeImageBitmap()
    }

    private fun overlayUmapAxes(base: BufferedImage, overlayColor: Color = Color(20, 20, 20, 220)): BufferedImage {
        val composed = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
        val g = composed.createGraphics()
        g.drawImage(base, 0, 0, null)

        val minDimension = minOf(base.width, base.height).toFloat().coerceAtLeast(1f)
        val iconSize = (minDimension * 0.105f).toInt().coerceAtLeast(42)
        val margin = (minDimension * 0.03f).toInt().coerceAtLeast(10)

        val overlay = rasterizeUmapAxesSvg(iconSize, iconSize)
        if (overlay != null) {
            val x = margin
            val y = (base.height - margin - iconSize).coerceAtLeast(0)
            val tintedOverlay = tintMonochromeOverlay(overlay, overlayColor)
            g.drawImage(tintedOverlay, x, y, iconSize, iconSize, null)
        }

        g.dispose()
        return composed
    }

    private fun applyClientBackground(
        source: BufferedImage,
        transparentBackground: Boolean,
        backgroundColor: Color
    ): BufferedImage {
        val output = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
        val g = output.createGraphics()
        g.drawImage(source, 0, 0, null)
        g.dispose()

        val maxX = (source.width - 1).coerceAtLeast(0)
        val maxY = (source.height - 1).coerceAtLeast(0)
        val insetX = (source.width * 0.02f).toInt().coerceIn(0, maxX)
        val insetY = (source.height * 0.02f).toInt().coerceIn(0, maxY)
        val corners = listOf(
            output.getRGB(insetX, insetY),
            output.getRGB(maxX - insetX, insetY),
            output.getRGB(insetX, maxY - insetY),
            output.getRGB(maxX - insetX, maxY - insetY)
        )

        val tolerance = 34
        val replacementRgb = backgroundColor.rgb and 0x00FFFFFF
        for (y in 0 until output.height) {
            for (x in 0 until output.width) {
                val argb = output.getRGB(x, y)
                val alpha = (argb ushr 24) and 0xFF
                if (alpha == 0) continue

                val matchesBackground = corners.any { corner ->
                    colorDistance(argb, corner) <= tolerance
                }
                if (!matchesBackground) continue

                if (transparentBackground) {
                    output.setRGB(x, y, 0x00000000)
                } else {
                    output.setRGB(x, y, (0xFF shl 24) or replacementRgb)
                }
            }
        }

        return output
    }

    private fun recolorExpressionLegendText(source: BufferedImage, textColor: Color): BufferedImage {
        val output = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
        val g = output.createGraphics()
        g.drawImage(source, 0, 0, null)
        g.dispose()

        val maxX = (source.width - 1).coerceAtLeast(0)
        val maxY = (source.height - 1).coerceAtLeast(0)
        val insetX = (source.width * 0.02f).toInt().coerceIn(0, maxX)
        val insetY = (source.height * 0.02f).toInt().coerceIn(0, maxY)
        val backgroundSamples = listOf(
            output.getRGB(insetX, insetY),
            output.getRGB(maxX - insetX, insetY),
            output.getRGB(insetX, maxY - insetY),
            output.getRGB(maxX - insetX, maxY - insetY)
        )

        val xStart = (source.width * 0.72f).toInt().coerceIn(0, source.width)
        val xEnd = (source.width * 0.985f).toInt().coerceIn(0, source.width)
        val yStart = (source.height * 0.10f).toInt().coerceIn(0, source.height)
        val yEnd = (source.height * 0.92f).toInt().coerceIn(0, source.height)
        val rgb = textColor.rgb and 0x00FFFFFF

        for (y in yStart until yEnd) {
            for (x in xStart until xEnd) {
                val argb = output.getRGB(x, y)
                val alpha = (argb ushr 24) and 0xFF
                if (alpha < 32) continue

                val r = (argb ushr 16) and 0xFF
                val gCh = (argb ushr 8) and 0xFF
                val b = argb and 0xFF
                val max = maxOf(r, gCh, b)
                val min = minOf(r, gCh, b)
                val avg = (r + gCh + b) / 3
                val looksLikeBackground = backgroundSamples.any { bg -> colorDistance(argb, bg) <= 18 }

                // Capture dark, near-neutral legend tick labels without affecting the color bar gradient.
                val looksLikeText = !looksLikeBackground && (max - min) < 26 && avg < 125
                if (looksLikeText) {
                    output.setRGB(x, y, (alpha shl 24) or rgb)
                }
            }
        }

        return output
    }

    private fun colorDistance(c1: Int, c2: Int): Int {
        val r1 = (c1 ushr 16) and 0xFF
        val g1 = (c1 ushr 8) and 0xFF
        val b1 = c1 and 0xFF
        val r2 = (c2 ushr 16) and 0xFF
        val g2 = (c2 ushr 8) and 0xFF
        val b2 = c2 and 0xFF
        val dr = r1 - r2
        val dg = g1 - g2
        val db = b1 - b2
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble()).toInt()
    }

    private fun overlayPlotTitle(base: BufferedImage, title: String, textColor: Color = Color(20, 20, 20, 220)): BufferedImage {
        val composed = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
        val g = composed.createGraphics()
        g.drawImage(base, 0, 0, null)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val minDimension = minOf(base.width, base.height).toFloat().coerceAtLeast(1f)
        val fontSize = (minDimension * 0.035f).toInt().coerceIn(14, 44)
        g.font = preferredTitleBaseFont?.deriveFont(Font.PLAIN, fontSize.toFloat())
            ?: Font("IBM Plex Sans Condensed", Font.PLAIN, fontSize)
        g.color = textColor

        val fm = g.fontMetrics
        val x = ((base.width - fm.stringWidth(title)) / 2).coerceAtLeast(10)
        val y = (fontSize + (base.height * 0.02f).toInt()).coerceAtMost(base.height - 8)
        g.drawString(title, x, y)

        g.dispose()
        return composed
    }

    private fun loadTitleFontFromResources(): Font? {
        val candidates = listOf(
            "composeResources/com.skira.app.composeapp.generated.resources/font/plex_condensed_regular.ttf",
            "font/plex_condensed_regular.ttf"
        )

        val classLoaders = listOfNotNull(
            Thread.currentThread().contextClassLoader,
            this::class.java.classLoader
        )

        for (path in candidates) {
            for (classLoader in classLoaders) {
                classLoader.getResourceAsStream(path.removePrefix("/"))?.use {
                    return runCatching { Font.createFont(Font.TRUETYPE_FONT, it) }.getOrNull()
                }
            }
            this::class.java.getResourceAsStream(if (path.startsWith("/")) path else "/$path")?.use {
                return runCatching { Font.createFont(Font.TRUETYPE_FONT, it) }.getOrNull()
            }
        }
        return null
    }

    private fun tintMonochromeOverlay(overlay: BufferedImage, color: Color): BufferedImage {
        val tinted = BufferedImage(overlay.width, overlay.height, BufferedImage.TYPE_INT_ARGB)
        val rgb = color.rgb and 0x00FFFFFF
        for (y in 0 until overlay.height) {
            for (x in 0 until overlay.width) {
                val argb = overlay.getRGB(x, y)
                val alpha = (argb ushr 24) and 0xFF
                if (alpha == 0) continue
                tinted.setRGB(x, y, (alpha shl 24) or rgb)
            }
        }
        return tinted
    }

    private fun parseAwtColor(hex: String): Color? {
        val normalized = hex.trim().removePrefix("#")
        if (normalized.length != 6) return null
        return runCatching {
            val rgb = normalized.toInt(16)
            Color((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
        }.getOrNull()
    }

    private fun toBufferedImage(img: java.awt.Image): BufferedImage {
        if (img is BufferedImage) return img
        val b = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val g = b.createGraphics()
        g.drawImage(img, 0, 0, null)
        g.dispose()
        return b
    }

    private fun rasterizeUmapAxesSvg(width: Int, height: Int): BufferedImage? {
        val bytes = umapAxesSvgBytes ?: return null

        return runCatching {
            val transcoder = object : ImageTranscoder() {
                var image: BufferedImage? = null

                override fun createImage(w: Int, h: Int): BufferedImage {
                    return BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
                }

                override fun writeImage(img: BufferedImage, out: TranscoderOutput?) {
                    image = img
                }
            }

            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width.toFloat())
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height.toFloat())

            ByteArrayInputStream(bytes).use { inputStream ->
                transcoder.transcode(TranscoderInput(inputStream), null)
            }

            transcoder.image
        }.getOrNull()
    }

    private fun loadUmapAxesSvgBytes(): ByteArray? {
        val candidates = listOf(
            "composeResources/com.skira.app.composeapp.generated.resources/drawable/umap_axes.svg",
            "/composeResources/com.skira.app.composeapp.generated.resources/drawable/umap_axes.svg",
            "drawable/umap_axes.svg",
            "/drawable/umap_axes.svg",
            "umap_axes.svg",
            "/umap_axes.svg"
        )

        val classLoaders = listOfNotNull(
            Thread.currentThread().contextClassLoader,
            this::class.java.classLoader
        )

        for (path in candidates) {
            for (classLoader in classLoaders) {
                classLoader.getResourceAsStream(path.removePrefix("/"))?.use { return it.readBytes() }
            }
            this::class.java.getResourceAsStream(path)?.use { return it.readBytes() }
        }

        return null
    }
}