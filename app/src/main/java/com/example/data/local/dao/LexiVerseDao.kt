package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.OfflineWordEntity
import com.example.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineWordDao {
    @Query("SELECT * FROM offline_words WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getWord(word: String): OfflineWordEntity?

    @Query("SELECT * FROM offline_words WHERE LOWER(word) LIKE LOWER(:prefix || '%') LIMIT 10")
    fun getSuggestions(prefix: String): Flow<List<OfflineWordEntity>>

    @Query("SELECT * FROM offline_words ORDER BY RANDOM() LIMIT 1")
    fun getRandomWord(): Flow<OfflineWordEntity?>

    @Query("SELECT COUNT(*) FROM offline_words")
    fun getWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM offline_words")
    suspend fun getWordCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(entity: OfflineWordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<OfflineWordEntity>)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 30): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: SearchHistoryEntity)

    @Transaction
    suspend fun addSearchQuery(query: String, isReverseSearch: Boolean = false) {
        deleteByQuery(query)
        insertHistory(SearchHistoryEntity(query = query, timestamp = System.currentTimeMillis(), isReverseSearch = isReverseSearch))
    }

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface FavoriteWordDao {
    @Query("SELECT * FROM favorite_words ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteWordEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_words WHERE LOWER(word) = LOWER(:word))")
    fun isFavoriteFlow(word: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_words WHERE LOWER(word) = LOWER(:word))")
    suspend fun isFavorite(word: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteWordEntity)

    @Query("DELETE FROM favorite_words WHERE LOWER(word) = LOWER(:word)")
    suspend fun removeFavoriteByWord(word: String)

    @Query("DELETE FROM favorite_words WHERE id = :id")
    suspend fun removeFavoriteById(id: Long)
}
