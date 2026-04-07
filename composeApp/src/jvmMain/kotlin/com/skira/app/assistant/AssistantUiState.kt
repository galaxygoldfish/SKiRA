package com.skira.app.assistant

/**
 * Represents the user-facing state of the AssistantFragment
 */
sealed interface AssistantUiState {
    data object Idle : AssistantUiState
    data object Loading : AssistantUiState
    data class Success(val data: MyGeneInfoData) : AssistantUiState
    data class Error(val message: String) : AssistantUiState
}

