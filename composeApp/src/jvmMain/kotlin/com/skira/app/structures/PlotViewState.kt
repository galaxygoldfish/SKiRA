package com.skira.app.structures

sealed interface PlotViewState {
    data object Loading : PlotViewState
    data object SelectGeneAndTime : PlotViewState
    data object SelectGene : PlotViewState
    data object SelectTime : PlotViewState
    data object Ready : PlotViewState
    data class Error(val message: String) : PlotViewState
}