package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_words",
    indices = [Index(value = ["word"], unique = true)]
)
data class OfflineWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val phonetic: String? = null,
    val partOfSpeech: String,
    val definition: String,
    val blurb: String? = null,
    val examples: String = "", // Comma/newline separated
    val synonyms: String = "", // Comma separated
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "search_history",
    indices = [Index(value = ["query"])]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReverseSearch: Boolean = false
)

@Entity(
    tableName = "favorite_words",
    indices = [Index(value = ["word"], unique = true)]
)
data class FavoriteWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val definition: String,
    val blurb: String? = null,
    val partOfSpeech: String? = null,
    val source: String = "LexiVerse",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
