package com.example.domain.model

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

enum class ThemeSetting {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

data class VocabDefinitionItem(
    val partOfSpeech: String,
    val definition: String,
    val exampleSentence: String? = null
)

data class VocabularyResult(
    val word: String,
    val shortBlurb: String? = null,
    val longBlurb: String? = null,
    val primaryDefinition: String? = null,
    val primaryPartOfSpeech: String? = null,
    val definitions: List<VocabDefinitionItem> = emptyList(),
    val usageExamples: List<String> = emptyList(),
    val isOnline: Boolean = true
)

data class UrbanDefinitionItem(
    val defId: Long,
    val word: String,
    val definition: String,
    val example: String,
    val thumbsUp: Int,
    val thumbsDown: Int,
    val author: String,
    val permalink: String? = null,
    val writtenOn: String? = null
)

data class OfflineDefinition(
    val word: String,
    val phonetic: String? = null,
    val partOfSpeech: String,
    val definition: String,
    val blurb: String? = null,
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList()
)

data class UnifiedWordResult(
    val queryWord: String,
    val isOfflineFallback: Boolean,
    val vocabularyResult: VocabularyResult? = null,
    val urbanDefinitions: List<UrbanDefinitionItem> = emptyList(),
    val offlineDefinition: OfflineDefinition? = null,
    val isFavorite: Boolean = false
)

data class AiCandidateWord(
    val word: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val definition: String,
    val nuance: String,
    val exampleSentence: String? = null
)

data class ReverseAiSearchResult(
    val conceptQuery: String,
    val candidates: List<AiCandidateWord>,
    val rawExplanation: String? = null
)

data class AppUpdateInfo(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestVersion: String = "",
    val currentVersion: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String? = null,
    val apkSize: Long = 0L,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
)
