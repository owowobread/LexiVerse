package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.LexiVersePreferences
import com.example.data.updater.DownloadState
import com.example.domain.model.AppUpdateInfo
import com.example.domain.model.FontFamilySetting
import com.example.domain.model.FontScaleSetting
import com.example.domain.model.Resource
import com.example.domain.model.ThemeSetting
import com.example.domain.usecase.CheckAppUpdateUseCase
import com.example.domain.usecase.ReverseAiSearchUseCase
import com.example.domain.usecase.SearchWordUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val apiKey: String = "",
    val model: String = LexiVersePreferences.DEFAULT_MODEL,
    val githubOwner: String = LexiVersePreferences.DEFAULT_GITHUB_OWNER,
    val githubRepo: String = LexiVersePreferences.DEFAULT_GITHUB_REPO,
    val themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    val fontFamilySetting: FontFamilySetting = FontFamilySetting.DEFAULT,
    val fontScaleSetting: FontScaleSetting = FontScaleSetting.NORMAL,
    val autoCheckUpdates: Boolean = true,
    val offlineWordCount: Int = 0,
    val isTestingApi: Boolean = false,
    val testStatus: String? = null,
    val isCheckingUpdate: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val updateErrorMessage: String? = null,
    val downloadState: DownloadState = DownloadState.Idle,
    val showUpdateDialog: Boolean = false
)

class SettingsViewModel(
    private val preferences: LexiVersePreferences,
    private val searchWordUseCase: SearchWordUseCase,
    private val reverseAiSearchUseCase: ReverseAiSearchUseCase,
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var updateCheckJob: Job? = null
    private var downloadJob: Job? = null

    init {
        viewModelScope.launch {
            preferences.openRouterApiKey.collectLatest { key ->
                _uiState.update { it.copy(apiKey = key) }
            }
        }
        viewModelScope.launch {
            preferences.openRouterModel.collectLatest { model ->
                _uiState.update { it.copy(model = model) }
            }
        }
        viewModelScope.launch {
            preferences.githubOwner.collectLatest { owner ->
                _uiState.update { it.copy(githubOwner = owner) }
            }
        }
        viewModelScope.launch {
            preferences.githubRepo.collectLatest { repo ->
                _uiState.update { it.copy(githubRepo = repo) }
            }
        }
        viewModelScope.launch {
            preferences.themeSetting.collectLatest { theme ->
                _uiState.update { it.copy(themeSetting = theme) }
            }
        }
        viewModelScope.launch {
            preferences.fontFamilySetting.collectLatest { font ->
                _uiState.update { it.copy(fontFamilySetting = font) }
            }
        }
        viewModelScope.launch {
            preferences.fontScaleSetting.collectLatest { scale ->
                _uiState.update { it.copy(fontScaleSetting = scale) }
            }
        }
        viewModelScope.launch {
            preferences.autoCheckUpdates.collectLatest { enabled ->
                _uiState.update { it.copy(autoCheckUpdates = enabled) }
            }
        }
        viewModelScope.launch {
            searchWordUseCase.getOfflineCount().collectLatest { count ->
                _uiState.update { it.copy(offlineWordCount = count) }
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            preferences.setOpenRouterApiKey(key)
        }
    }

    fun saveModel(model: String) {
        viewModelScope.launch {
            preferences.setOpenRouterModel(model)
        }
    }

    fun saveGitHubRepo(owner: String, repo: String) {
        viewModelScope.launch {
            preferences.setGitHubRepo(owner, repo)
        }
    }

    fun saveTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            preferences.setThemeSetting(theme)
        }
    }
    
    fun saveFontFamily(fontFamily: FontFamilySetting) {
        viewModelScope.launch {
            preferences.setFontFamilySetting(fontFamily)
        }
    }
    
    fun saveFontScale(fontScale: FontScaleSetting) {
        viewModelScope.launch {
            preferences.setFontScaleSetting(fontScale)
        }
    }

    fun saveAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoCheckUpdates(enabled)
        }
    }

    fun testApiConnection() {
        val key = _uiState.value.apiKey
        val model = _uiState.value.model

        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, testStatus = "Testing connection to OpenRouter...") }
            reverseAiSearchUseCase.testConnection(key, model).collectLatest { res ->
                when (res) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isTestingApi = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isTestingApi = false, testStatus = res.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isTestingApi = false, testStatus = "Error: ${res.message}") }
                    }
                }
            }
        }
    }

    fun checkForUpdate(showDialogIfAvailable: Boolean = true) {
        val owner = _uiState.value.githubOwner
        val repo = _uiState.value.githubRepo

        updateCheckJob?.cancel()
        updateCheckJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingUpdate = true,
                    updateErrorMessage = null
                )
            }

            checkAppUpdateUseCase(owner, repo).collectLatest { res ->
                when (res) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isCheckingUpdate = true) }
                    }
                    is Resource.Success -> {
                        val info = res.data
                        _uiState.update {
                            it.copy(
                                isCheckingUpdate = false,
                                updateInfo = info,
                                showUpdateDialog = showDialogIfAvailable && (info.updateAvailable || info.latestVersion.isNotBlank()),
                                updateErrorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isCheckingUpdate = false,
                                updateErrorMessage = res.message,
                                showUpdateDialog = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun startDownload(downloadUrl: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            checkAppUpdateUseCase.downloadApk(downloadUrl).collectLatest { state ->
                _uiState.update { it.copy(downloadState = state) }
            }
        }
    }

    fun installApk(file: File): Result<Unit> {
        return checkAppUpdateUseCase.installApk(file)
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchWordUseCase.clearHistory()
        }
    }

    class Factory(
        private val preferences: LexiVersePreferences,
        private val searchWordUseCase: SearchWordUseCase,
        private val reverseAiSearchUseCase: ReverseAiSearchUseCase,
        private val checkAppUpdateUseCase: CheckAppUpdateUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferences, searchWordUseCase, reverseAiSearchUseCase, checkAppUpdateUseCase) as T
        }
    }
}
