package com.example.di

import android.content.Context
import com.example.data.local.LexiVerseDatabase
import com.example.data.network.NetworkClientProvider
import com.example.data.network.scraper.VocabularyScraper
import com.example.data.preferences.LexiVersePreferences
import com.example.data.repository.AiRepository
import com.example.data.repository.DictionaryRepository
import com.example.data.repository.UpdateRepository
import com.example.data.updater.ApkDownloaderAndInstaller
import com.example.domain.usecase.CheckAppUpdateUseCase
import com.example.domain.usecase.ManageFavoritesUseCase
import com.example.domain.usecase.ReverseAiSearchUseCase
import com.example.domain.usecase.SearchWordUseCase

class AppContainer(private val context: Context) {

    val database by lazy {
        LexiVerseDatabase.getInstance(context)
    }

    val preferences by lazy {
        LexiVersePreferences(context)
    }

    val vocabularyScraper by lazy {
        VocabularyScraper()
    }

    val apkDownloaderAndInstaller by lazy {
        ApkDownloaderAndInstaller(context)
    }

    val dictionaryRepository by lazy {
        DictionaryRepository(
            offlineWordDao = database.offlineWordDao(),
            searchHistoryDao = database.searchHistoryDao(),
            favoriteWordDao = database.favoriteWordDao(),
            vocabularyScraper = vocabularyScraper,
            urbanDictionaryApi = NetworkClientProvider.urbanDictionaryApi
        )
    }

    val aiRepository by lazy {
        AiRepository(
            openRouterApi = NetworkClientProvider.openRouterApi,
            preferences = preferences
        )
    }

    val updateRepository by lazy {
        UpdateRepository(
            gitHubApi = NetworkClientProvider.gitHubApi,
            apkDownloaderAndInstaller = apkDownloaderAndInstaller
        )
    }

    val searchWordUseCase by lazy {
        SearchWordUseCase(dictionaryRepository)
    }

    val reverseAiSearchUseCase by lazy {
        ReverseAiSearchUseCase(aiRepository)
    }

    val checkAppUpdateUseCase by lazy {
        CheckAppUpdateUseCase(updateRepository)
    }

    val manageFavoritesUseCase by lazy {
        ManageFavoritesUseCase(dictionaryRepository)
    }
}
