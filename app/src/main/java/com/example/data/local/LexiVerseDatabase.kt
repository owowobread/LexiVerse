package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FavoriteWordDao
import com.example.data.local.dao.OfflineWordDao
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.OfflineWordEntity
import com.example.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OfflineWordEntity::class,
        SearchHistoryEntity::class,
        FavoriteWordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LexiVerseDatabase : RoomDatabase() {

    abstract fun offlineWordDao(): OfflineWordDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteWordDao(): FavoriteWordDao

    companion object {
        @Volatile
        private var INSTANCE: LexiVerseDatabase? = null

        fun getInstance(context: Context): LexiVerseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LexiVerseDatabase::class.java,
                    "lexiverse_database_v2"
                )
                    .createFromAsset("lexiverse.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
