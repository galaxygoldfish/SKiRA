package com.skira.app.structures

/**
 * Used to denote the different states of plot downloading and their formats
 */
object PlotDownloadState {
    const val IDLE = -1
    const val DOWNLOADING_PNG = 0
    const val DOWNLOADING_JPG = 1
    const val DOWNLOADING_PDF = 2
    const val DOWNLOAD_SUCCESS = 3
    const val DOWNLOAD_FAILURE = 4
}