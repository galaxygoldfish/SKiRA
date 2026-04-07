package com.skira.app.structures

/**
 * Represents the state of the HomeView in terms of the
 * plot generation/loading/selection process for the current tab
 */
sealed interface PlotViewState {
    data object Loading : PlotViewState
    data object SelectGeneAndTime : PlotViewState
    data object SelectGene : PlotViewState
    data object SelectTime : PlotViewState
    data object Ready : PlotViewState
    data class Error(val message: String) : PlotViewState
}