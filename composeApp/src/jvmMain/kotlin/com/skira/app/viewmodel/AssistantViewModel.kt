package com.skira.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skira.app.assistant.AssistantUiState
import com.skira.app.assistant.MyGeneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssistantViewModel : ViewModel() {

    var uiState by mutableStateOf<AssistantUiState>(AssistantUiState.Idle)
        private set

    private var fetchJob: Job? = null

    /**
     * Fetches gene info from MyGene.info for [geneSymbol].
     * Any in-flight request for a previous gene is cancelled first.
     */
    fun fetchGeneInfo(geneSymbol: String) {
        fetchJob?.cancel()
        uiState = AssistantUiState.Loading
        fetchJob = viewModelScope.launch {
            uiState = MyGeneRepository.fetchGeneInfo(geneSymbol).fold(
                onSuccess = { AssistantUiState.Success(it) },
                onFailure = { AssistantUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    /** Resets the assistant back to an idle/empty state (e.g. on tab switch to an ungenerated tab). */
    fun reset() {
        fetchJob?.cancel()
        uiState = AssistantUiState.Idle
    }
}