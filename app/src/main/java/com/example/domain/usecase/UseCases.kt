package com.example.domain.usecase

import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.repository.AiRepository
import com.example.data.repository.DictionaryRepository
import com.example.data.repository.UpdateRepository
import com.example.domain.model.AppUpdateInfo
import com.example.domain.model.OfflineDefinition
import com.example.domain.model.Resource
import com.example.domain.model.ReverseAiSearchResult
import com.example.domain.model.UnifiedWordResult
import kotlinx.coroutines.flow.Flow

class SearchWordUseCase(private val repository: DictionaryRepository) {
    operator fun invoke(query: String): Flow<Resource<UnifiedWordResult>> =
        repository.searchWord(query)

    fun getSuggestions(query: String): Flow<List<String>> =
        repository.getSuggestions(query)

    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        repository.getRecentSearches()

    suspend fun clearHistory() =
        repository.clearHistory()

    suspend fun deleteHistory(id: Long) =
        repository.deleteHistory(id)

    fun getRandomOfflineWord(): Flow<OfflineDefinition?> =
        repository.getRandomOfflineWord()

    fun getOfflineCount(): Flow<Int> =
        repository.getOfflineWordCount()
}

class ReverseAiSearchUseCase(private val aiRepository: AiRepository) {
    operator fun invoke(conceptDescription: String): Flow<Resource<ReverseAiSearchResult>> =
        aiRepository.reverseConceptSearch(conceptDescription)

    fun testConnection(apiKey: String, model: String): Flow<Resource<String>> =
        aiRepository.testConnection(apiKey, model)
}

class CheckAppUpdateUseCase(private val updateRepository: UpdateRepository) {
    operator fun invoke(owner: String, repo: String): Flow<Resource<AppUpdateInfo>> =
        updateRepository.checkForUpdate(owner, repo)

    fun downloadApk(url: String) =
        updateRepository.downloadApk(url)

    fun hasInstallPermission() =
        updateRepository.hasInstallPermission()

    fun requestInstallPermissionIntent() =
        updateRepository.requestInstallPermissionIntent()

    fun installApk(file: java.io.File) =
        updateRepository.installApk(file)
}

class ManageFavoritesUseCase(private val repository: DictionaryRepository) {
    fun getFavorites(): Flow<List<FavoriteWordEntity>> =
        repository.getAllFavorites()

    suspend fun toggleFavorite(
        word: String,
        definition: String,
        blurb: String? = null,
        partOfSpeech: String? = null,
        source: String = "LexiVerse"
    ): Boolean = repository.toggleFavorite(word, definition, blurb, partOfSpeech, source)

    suspend fun removeFavoriteById(id: Long) =
        repository.removeFavoriteById(id)
}
