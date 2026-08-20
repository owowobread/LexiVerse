package com.example.data.repository

import com.example.data.local.dao.FavoriteWordDao
import com.example.data.local.dao.OfflineWordDao
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.OfflineWordEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.network.api.UrbanDictionaryApi
import com.example.data.network.scraper.VocabularyScraper
import com.example.domain.model.OfflineDefinition
import com.example.domain.model.Resource
import com.example.domain.model.UnifiedWordResult
import com.example.domain.model.UrbanDefinitionItem
import com.example.domain.model.VocabularyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DictionaryRepository(
    private val offlineWordDao: OfflineWordDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val vocabularyScraper: VocabularyScraper,
    private val urbanDictionaryApi: UrbanDictionaryApi
) {

    fun searchWord(query: String): Flow<Resource<UnifiedWordResult>> = flow {
        emit(Resource.Loading)
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            emit(Resource.Error("Search query cannot be empty"))
            return@flow
        }

        // Record to search history
        searchHistoryDao.addSearchQuery(cleanQuery, isReverseSearch = false)
        val isFavorite = favoriteWordDao.isFavorite(cleanQuery)

        // Attempt online multi-source search (Vocabulary.com + Urban Dictionary concurrently)
        var onlineVocabResult: VocabularyResult? = null
        var onlineUrbanItems: List<UrbanDefinitionItem> = emptyList()
        var onlineFetchSucceeded = false

        try {
            coroutineScope {
                val vocabDeferred = async(Dispatchers.IO) {
                    vocabularyScraper.scrapeWord(cleanQuery).getOrNull()
                }
                val urbanDeferred = async(Dispatchers.IO) {
                    try {
                        val response = urbanDictionaryApi.getDefinitions(cleanQuery)
                        if (response.isSuccessful) {
                            response.body()?.list?.map { dto ->
                                UrbanDefinitionItem(
                                    defId = dto.defid,
                                    word = dto.word,
                                    definition = dto.definition.replace("[", "").replace("]", ""),
                                    example = dto.example.replace("[", "").replace("]", ""),
                                    thumbsUp = dto.thumbs_up,
                                    thumbsDown = dto.thumbs_down,
                                    author = dto.author,
                                    permalink = dto.permalink,
                                    writtenOn = dto.written_on
                                )
                            } ?: emptyList()
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                onlineVocabResult = vocabDeferred.await()
                onlineUrbanItems = urbanDeferred.await()
            }

            if (onlineVocabResult != null || onlineUrbanItems.isNotEmpty()) {
                onlineFetchSucceeded = true
            }
        } catch (e: Exception) {
            onlineFetchSucceeded = false
        }

        // If online search succeeded for vocabulary, cache the word in local DB for offline access
        if (onlineVocabResult != null) {
            val vocab = onlineVocabResult!!
            val offlineEntity = OfflineWordEntity(
                word = vocab.word,
                phonetic = null,
                partOfSpeech = vocab.primaryPartOfSpeech ?: "word",
                definition = vocab.primaryDefinition ?: vocab.shortBlurb ?: "Definition available online.",
                blurb = vocab.shortBlurb ?: vocab.longBlurb,
                examples = vocab.usageExamples.joinToString("\n"),
                synonyms = ""
            )
            offlineWordDao.insertWord(offlineEntity)
        }

        // Query offline fallback from local Room database
        val localEntity = offlineWordDao.getWord(cleanQuery)
        val offlineDef = localEntity?.let { entity ->
            OfflineDefinition(
                word = entity.word,
                phonetic = entity.phonetic,
                partOfSpeech = entity.partOfSpeech,
                definition = entity.definition,
                blurb = entity.blurb,
                examples = entity.examples.split("\n").filter { it.isNotBlank() },
                synonyms = entity.synonyms.split(",").map { it.trim() }.filter { it.isNotBlank() }
            )
        }

        if (onlineFetchSucceeded) {
            emit(
                Resource.Success(
                    UnifiedWordResult(
                        queryWord = cleanQuery,
                        isOfflineFallback = false,
                        vocabularyResult = onlineVocabResult,
                        urbanDefinitions = onlineUrbanItems,
                        offlineDefinition = offlineDef,
                        isFavorite = isFavorite
                    )
                )
            )
        } else {
            // Offline fallback path
            if (offlineDef != null) {
                emit(
                    Resource.Success(
                        UnifiedWordResult(
                            queryWord = cleanQuery,
                            isOfflineFallback = true,
                            vocabularyResult = null,
                            urbanDefinitions = emptyList(),
                            offlineDefinition = offlineDef,
                            isFavorite = isFavorite
                        )
                    )
                )
            } else {
                emit(
                    Resource.Error(
                        "Could not find definitions for '$cleanQuery' online or in offline dictionary. Please check your internet connection."
                    )
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    fun getSuggestions(query: String): Flow<List<String>> {
        return offlineWordDao.getSuggestions(query.trim()).map { list ->
            list.map { it.word }
        }
    }

    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentHistory()

    suspend fun clearHistory() = searchHistoryDao.clearAll()

    suspend fun deleteHistory(id: Long) = searchHistoryDao.deleteById(id)

    fun getAllFavorites(): Flow<List<FavoriteWordEntity>> = favoriteWordDao.getAllFavorites()

    fun isFavoriteFlow(word: String): Flow<Boolean> = favoriteWordDao.isFavoriteFlow(word)

    suspend fun toggleFavorite(
        word: String,
        definition: String,
        blurb: String? = null,
        partOfSpeech: String? = null,
        source: String = "LexiVerse"
    ): Boolean {
        val exists = favoriteWordDao.isFavorite(word)
        if (exists) {
            favoriteWordDao.removeFavoriteByWord(word)
            return false
        } else {
            favoriteWordDao.addFavorite(
                FavoriteWordEntity(
                    word = word,
                    definition = definition,
                    blurb = blurb,
                    partOfSpeech = partOfSpeech,
                    source = source
                )
            )
            return true
        }
    }

    suspend fun removeFavoriteById(id: Long) = favoriteWordDao.removeFavoriteById(id)

    fun getRandomOfflineWord(): Flow<OfflineDefinition?> {
        return offlineWordDao.getRandomWord().map { entity ->
            entity?.let {
                OfflineDefinition(
                    word = it.word,
                    phonetic = it.phonetic,
                    partOfSpeech = it.partOfSpeech,
                    definition = it.definition,
                    blurb = it.blurb,
                    examples = it.examples.split("\n").filter { s -> s.isNotBlank() },
                    synonyms = it.synonyms.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }
                )
            }
        }
    }

    fun getOfflineWordCount(): Flow<Int> = offlineWordDao.getWordCount()
}
