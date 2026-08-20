package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FavoriteWordEntity
import com.example.domain.usecase.ManageFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : ViewModel() {

    private val _searchFilter = MutableStateFlow("")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    val filteredFavorites: StateFlow<List<FavoriteWordEntity>> =
        combine(manageFavoritesUseCase.getFavorites(), _searchFilter) { list, filter ->
            if (filter.isBlank()) {
                list
            } else {
                list.filter {
                    it.word.contains(filter, ignoreCase = true) ||
                            it.definition.contains(filter, ignoreCase = true) ||
                            (it.blurb?.contains(filter, ignoreCase = true) == true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onFilterChanged(query: String) {
        _searchFilter.value = query
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch {
            manageFavoritesUseCase.removeFavoriteById(id)
        }
    }

    class Factory(
        private val manageFavoritesUseCase: ManageFavoritesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(manageFavoritesUseCase) as T
        }
    }
}
