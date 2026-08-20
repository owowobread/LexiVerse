package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SearchHistoryEntity
import com.example.domain.model.OfflineDefinition
import com.example.domain.model.Resource
import com.example.domain.model.UnifiedWordResult
import com.example.domain.usecase.ManageFavoritesUseCase
import com.example.domain.usecase.SearchWordUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DictionarySourceTab {
    VOCABULARY,
    URBAN,
    OFFLINE
}

data class DictionaryUiState(
    val searchQuery: String = "",
    val activeWordResult: UnifiedWordResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: DictionarySourceTab = DictionarySourceTab.VOCABULARY,
    val suggestions: List<String> = emptyList(),
    val wordOfTheDay: OfflineDefinition? = null,
    val isFavorite: Boolean = false
)

class DictionaryViewModel(
    private val searchWordUseCase: SearchWordUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<SearchHistoryEntity>> =
        searchWordUseCase.getRecentSearches().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null
    private var suggestionJob: Job? = null

    init {
        loadWordOfTheDay()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        suggestionJob?.cancel()
        if (query.length >= 2) {
            suggestionJob = viewModelScope.launch {
                delay(150)
                searchWordUseCase.getSuggestions(query).collect { list ->
                    _uiState.update { it.copy(suggestions = list) }
                }
            }
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun searchWord(query: String = _uiState.value.searchQuery) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return

        _uiState.update {
            it.copy(
                searchQuery = cleanQuery,
                isLoading = true,
                errorMessage = null,
                suggestions = emptyList()
            )
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchWordUseCase(cleanQuery).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val wordResult = result.data
                        val initialTab = when {
                            wordResult.vocabularyResult != null -> DictionarySourceTab.VOCABULARY
                            wordResult.urbanDefinitions.isNotEmpty() -> DictionarySourceTab.URBAN
                            else -> DictionarySourceTab.OFFLINE
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                activeWordResult = wordResult,
                                selectedTab = initialTab,
                                isFavorite = wordResult.isFavorite,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tab: DictionarySourceTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite() {
        val result = _uiState.value.activeWordResult ?: return
        viewModelScope.launch {
            val primaryDef = result.vocabularyResult?.primaryDefinition
                ?: result.offlineDefinition?.definition
                ?: result.urbanDefinitions.firstOrNull()?.definition
                ?: "Definition"

            val blurb = result.vocabularyResult?.shortBlurb
                ?: result.offlineDefinition?.blurb

            val pos = result.vocabularyResult?.primaryPartOfSpeech
                ?: result.offlineDefinition?.partOfSpeech
                ?: "word"

            val newFavoriteStatus = manageFavoritesUseCase.toggleFavorite(
                word = result.queryWord,
                definition = primaryDef,
                blurb = blurb,
                partOfSpeech = pos,
                source = if (result.isOfflineFallback) "Offline Dictionary" else "Vocabulary.com / Urban"
            )
            _uiState.update { it.copy(isFavorite = newFavoriteStatus) }
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                activeWordResult = null,
                errorMessage = null,
                suggestions = emptyList()
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchWordUseCase.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            searchWordUseCase.deleteHistory(id)
        }
    }

    fun loadWordOfTheDay() {
        viewModelScope.launch {
            searchWordUseCase.getRandomOfflineWord().collectLatest { word ->
                _uiState.update { it.copy(wordOfTheDay = word) }
            }
        }
    }

    class Factory(
        private val searchWordUseCase: SearchWordUseCase,
        private val manageFavoritesUseCase: ManageFavoritesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DictionaryViewModel(searchWordUseCase, manageFavoritesUseCase) as T
        }
    }
}
