package com.skira.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skira.app.r.PlotWorker
import com.skira.app.r.PlotWorker.runPlot
import com.skira.app.r.PlotWorker.warmUp
import com.skira.app.structures.*
import com.skira.app.utilities.PreferenceManager
import com.skira.app.utilities.verifySelectedDataset
import kotlinx.coroutines.launch
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.jetbrains.skia.Image
import java.awt.Color
import java.awt.Desktop
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * Corresponding ViewModel for the HomeView composable (which encompasses all the plot generation features)
 */
class HomeViewModel : ViewModel() {

    /* Serves as the dialog navigation controller. Set to DialogType.NONE to hide dialog */
    var currentDialogToShow by mutableStateOf(DialogType.WELCOME)

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

    /* Colormaps to use when generating each plot. [PlotColor] gives the supported color schemes */
    var currentExpressionPlotColor by mutableStateOf(PlotColor.Plasma)
    /* 0 corresponds to color by cell type and 1 corresponds to color by timepoint (only in merge) */
    var currentDimPlotColor by mutableStateOf(0)

    /* Whether we are showing the download options for each plot */
    var showingExpressionDownloadMenu by mutableStateOf(false)
    var showingDimPlotDownloadMenu by mutableStateOf(false)

    /* Individual active states for each plot's download status */
    var expressionPlotDownloadState by mutableStateOf(PlotDownloadState.IDLE)
    var dimPlotDownloadState by mutableStateOf(PlotDownloadState.IDLE)

    /* Whether we should label cell types when generating each type of plot */
    var showExpressionClusterLabels by mutableStateOf(false)
    var showDimPlotClusterLabels by mutableStateOf(false)

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
        if (isLoadingMeta) return
        isLoadingMeta = true
        loadError = null
        viewModelScope.launch {
            val warmupJob = launch {
                val w = warmUp()
                w.onFailure { println("[SKiRA] PlotWorker warmup failed: ${it.message}") }
            }
            val meta = PlotWorker.requestMetadata(onProgress = { prog ->
                metadataLoadingProgress = prog
            })
            meta.fold(
                onSuccess = {
                    metadataGeneList = listOf("Select") + it.genes
                    metadataTimepointList = listOf("Select") + it.timepoints
                },
                onFailure = { t ->
                    loadError = t.message ?: "Failed to load metadata"
                }
            )
            warmupJob.join()
            isLoadingMeta = false
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
                showDimLabelsParam,
                showExpressionLabelsParam
            ) { pct -> plotGenerationTaskProgress = pct }
            result.fold(
                onSuccess = { out ->
                    try {
                        val bytes = out.featureBytes
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
                        println("[SKiRA] startPlotJob: decode failure -> ${t::class.simpleName}: ${t.message}")
                    }
                },
                onFailure = { t ->
                    loadError = t.message ?: "Unknown error"
                    println("[SKiRA] startPlotJob: worker error -> ${t::class.simpleName}: ${t.message}")
                }
            )
        } finally {
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
        ImageIO.write(plotBitmap.toAwtImage(), "png", tempFile)
        tempFile.deleteOnExit()
        Desktop.getDesktop().browse(tempFile.toURI())
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
        isFeaturePlot: Boolean
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

            fun toBufferedImage(img: java.awt.Image): BufferedImage {
                if (img is BufferedImage) return img
                val b = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
                val g = b.createGraphics()
                g.drawImage(img, 0, 0, null)
                g.dispose()
                return b
            }

            val buf = toBufferedImage(awtImage)

            when (extLower) {
                "png" -> {
                    ImageIO.write(buf, "png", target.toFile())
                }

                "jpg", "jpeg" -> {
                    val rgbImage: BufferedImage = if (buf.colorModel.hasAlpha()) {
                        val converted = BufferedImage(buf.width, buf.height, BufferedImage.TYPE_INT_RGB)
                        val g: Graphics2D = converted.createGraphics()
                        g.paint = Color.WHITE
                        g.fillRect(0, 0, converted.width, converted.height)
                        g.drawImage(buf, 0, 0, null)
                        g.dispose()
                        converted
                    } else {
                        BufferedImage(buf.width, buf.height, BufferedImage.TYPE_INT_RGB).also {
                            val g = it.createGraphics()
                            g.drawImage(buf, 0, 0, null)
                            g.dispose()
                        }
                    }
                    ImageIO.write(rgbImage, "jpg", target.toFile())
                }

                "pdf" -> {
                    PDDocument().use { doc ->
                        val pdImage = LosslessFactory.createFromImage(doc, buf)
                        val pageRect = PDRectangle(buf.width.toFloat(), buf.height.toFloat())
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
                    val written = runCatching { ImageIO.write(buf, extLower, target.toFile()) }.getOrNull() ?: false
                    if (!written) {
                        ImageIO.write(buf, "png", target.toFile())
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
                } catch (_: IOException) {
                    if (isFeaturePlot) {
                        expressionPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
                    } else {
                        dimPlotDownloadState = PlotDownloadState.DOWNLOAD_FAILURE
                    }
                }
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
}