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
    version = 1,
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
                    "lexiverse_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate the database in background
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.offlineWordDao().insertAll(OfflineDictionaryData.initialWords)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
