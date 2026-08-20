package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.FontFamilySetting
import com.example.domain.model.FontScaleSetting
import com.example.domain.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lexiverse_settings")

class LexiVersePreferences(private val context: Context) {
    companion object {
        private val KEY_OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        private val KEY_OPENROUTER_MODEL = stringPreferencesKey("openrouter_model")
        private val KEY_OPENROUTER_API_NAME = stringPreferencesKey("openrouter_api_name")
        private val KEY_OPENROUTER_BASE_URL = stringPreferencesKey("openrouter_base_url")
        private val KEY_GITHUB_OWNER = stringPreferencesKey("github_owner")
        private val KEY_GITHUB_REPO = stringPreferencesKey("github_repo")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        private val KEY_FONT_SCALE = stringPreferencesKey("font_scale")

        const val DEFAULT_MODEL = "google/gemma-4-26b-a4b-it:free"
        const val DEFAULT_API_NAME = "routersai"
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1" 
        const val DEFAULT_GITHUB_OWNER = "maddyisthegame"
        const val DEFAULT_GITHUB_REPO = "LexiVerse"
    }

    val openRouterApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPENROUTER_API_KEY] ?: ""
    }

    val openRouterModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPENROUTER_MODEL] ?: DEFAULT_MODEL
    }

    val openRouterApiName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPENROUTER_API_NAME] ?: DEFAULT_API_NAME
    }

    val openRouterBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPENROUTER_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val githubOwner: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GITHUB_OWNER] ?: DEFAULT_GITHUB_OWNER
    }

    val githubRepo: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GITHUB_REPO] ?: DEFAULT_GITHUB_REPO
    }

    val themeSetting: Flow<ThemeSetting> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME_MODE]) {
            "LIGHT" -> ThemeSetting.LIGHT
            "DARK" -> ThemeSetting.DARK
            "AMOLED" -> ThemeSetting.AMOLED
            else -> ThemeSetting.SYSTEM
        }
    }

    val fontFamilySetting: Flow<FontFamilySetting> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_FONT_FAMILY]) {
            "SERIF" -> FontFamilySetting.SERIF
            "MONOSPACE" -> FontFamilySetting.MONOSPACE
            "SANS_SERIF" -> FontFamilySetting.SANS_SERIF
            "CURSIVE" -> FontFamilySetting.CURSIVE
            else -> FontFamilySetting.DEFAULT
        }
    }

    val fontScaleSetting: Flow<FontScaleSetting> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_FONT_SCALE]) {
            "SMALL" -> FontScaleSetting.SMALL
            "LARGE" -> FontScaleSetting.LARGE
            "EXTRA_LARGE" -> FontScaleSetting.EXTRA_LARGE
            else -> FontScaleSetting.NORMAL
        }
    }

    val autoCheckUpdates: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CHECK_UPDATES] ?: true
    }

    suspend fun setOpenRouterApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPENROUTER_API_KEY] = apiKey.trim()
        }
    }

    suspend fun setOpenRouterModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPENROUTER_MODEL] = model.trim()
        }
    }

    suspend fun setOpenRouterApiName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPENROUTER_API_NAME] = name.trim()
        }
    }

    suspend fun setOpenRouterBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPENROUTER_BASE_URL] = url.trim()
        }
    }

    suspend fun setGitHubRepo(owner: String, repo: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GITHUB_OWNER] = owner.trim()
            prefs[KEY_GITHUB_REPO] = repo.trim()
        }
    }

    suspend fun setThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = theme.name
        }
    }
    
    suspend fun setFontFamilySetting(fontFamily: FontFamilySetting) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FONT_FAMILY] = fontFamily.name
        }
    }
    
    suspend fun setFontScaleSetting(fontScale: FontScaleSetting) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FONT_SCALE] = fontScale.name
        }
    }

    suspend fun setAutoCheckUpdates(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CHECK_UPDATES] = enabled
        }
    }
}
