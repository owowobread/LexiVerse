package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.LexiVersePreferences
import com.example.domain.model.Resource
import com.example.domain.model.ReverseAiSearchResult
import com.example.domain.usecase.ReverseAiSearchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReverseAiUiState(
    val conceptQuery: String = "",
    val isLoading: Boolean = false,
    val searchResult: ReverseAiSearchResult? = null,
    val errorMessage: String? = null,
    val isApiKeyMissing: Boolean = false
)

class ReverseAiViewModel(
    private val reverseAiSearchUseCase: ReverseAiSearchUseCase,
    private val preferences: LexiVersePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReverseAiUiState())
    val uiState: StateFlow<ReverseAiUiState> = _uiState.asStateFlow()

    val currentModel: StateFlow<String> = preferences.openRouterModel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LexiVersePreferences.DEFAULT_MODEL
    )

    val currentApiKey: StateFlow<String> = preferences.openRouterApiKey.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private var aiSearchJob: Job? = null

    fun onConceptQueryChanged(text: String) {
        _uiState.update { it.copy(conceptQuery = text) }
    }

    fun searchConcept(prompt: String = _uiState.value.conceptQuery) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) return

        _uiState.update {
            it.copy(
                conceptQuery = cleanPrompt,
                isLoading = true,
                errorMessage = null,
                isApiKeyMissing = false
            )
        }

        aiSearchJob?.cancel()
        aiSearchJob = viewModelScope.launch {
            reverseAiSearchUseCase(cleanPrompt).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                searchResult = result.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        val isKeyMissing = result.message.contains("API key is missing", ignoreCase = true)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                                isApiKeyMissing = isKeyMissing
                            )
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        _uiState.update {
            it.copy(
                conceptQuery = "",
                searchResult = null,
                errorMessage = null
            )
        }
    }

    class Factory(
        private val reverseAiSearchUseCase: ReverseAiSearchUseCase,
        private val preferences: LexiVersePreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReverseAiViewModel(reverseAiSearchUseCase, preferences) as T
        }
    }
}
