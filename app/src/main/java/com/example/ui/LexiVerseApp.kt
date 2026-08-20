package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.ui.components.UpdateDialog
import java.io.File
import com.example.data.updater.DownloadState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
import com.example.domain.model.FontFamilySetting
import com.example.domain.model.FontScaleSetting
import com.example.domain.model.ThemeSetting
import com.example.ui.screens.BookmarksHistoryScreen
import com.example.ui.screens.DictionarySearchScreen
import com.example.ui.screens.ReverseAiSearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LexiVerseTheme
import com.example.ui.viewmodel.DictionaryViewModel
import com.example.ui.viewmodel.FavoritesViewModel
import com.example.ui.viewmodel.ReverseAiViewModel
import com.example.ui.viewmodel.SettingsViewModel

enum class AppNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DICTIONARY(
        title = "Dictionary",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_item_dictionary"
    ),
    REVERSE_AI(
        title = "Reverse AI",
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "nav_item_reverse_ai"
    ),
    SAVED(
        title = "Saved",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        testTag = "nav_item_saved"
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_item_settings"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexiVerseApp(
    appContainer: AppContainer
) {
    var selectedItem by remember { mutableStateOf(AppNavigationItem.DICTIONARY) }

    val dictionaryViewModel: DictionaryViewModel = viewModel(
        factory = DictionaryViewModel.Factory(
            appContainer.searchWordUseCase,
            appContainer.manageFavoritesUseCase
        )
    )
    val reverseAiViewModel: ReverseAiViewModel = viewModel(
        factory = ReverseAiViewModel.Factory(
            appContainer.reverseAiSearchUseCase,
            appContainer.preferences
        )
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.Factory(
            appContainer.manageFavoritesUseCase
        )
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            appContainer.preferences,
            appContainer.searchWordUseCase,
            appContainer.reverseAiSearchUseCase,
            appContainer.checkAppUpdateUseCase
        )
    )

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val themeSetting by appContainer.preferences.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM)
    val fontFamilySetting by appContainer.preferences.fontFamilySetting.collectAsState(initial = FontFamilySetting.DEFAULT)
    val fontScaleSetting by appContainer.preferences.fontScaleSetting.collectAsState(initial = FontScaleSetting.NORMAL)

    val composeFontFamily = when(fontFamilySetting) {
        FontFamilySetting.DEFAULT -> FontFamily.Default
        FontFamilySetting.SERIF -> FontFamily.Serif
        FontFamilySetting.MONOSPACE -> FontFamily.Monospace
        FontFamilySetting.SANS_SERIF -> FontFamily.SansSerif
        FontFamilySetting.CURSIVE -> FontFamily.Cursive
    }
    
    val fontScale = fontScaleSetting.scale

    // Nested scroll for bottom bar
    var bottomBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -15f) {
                    bottomBarVisible = false
                } else if (available.y > 15f) {
                    bottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Optional silent update check on launch
    LaunchedEffect(settingsUiState.autoCheckUpdates) {
        if (settingsUiState.autoCheckUpdates) {
            settingsViewModel.checkForUpdate(showDialogIfAvailable = true)
        }
    }

    LexiVerseTheme(
        themeSetting = themeSetting,
        fontFamily = composeFontFamily,
        fontScale = fontScale
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.nestedScroll(nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "LexiVerse",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                bottomBar = {
                    AnimatedVisibility(
                        visible = bottomBarVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + expandVertically(),
                        exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically()
                    ) {
                        NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        AppNavigationItem.values().forEach { item ->
                            val isSelected = selectedItem == item
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedItem = item },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                    }
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = selectedItem,
                    modifier = Modifier.padding(innerPadding),
                    label = "screen_transition"
                ) { currentScreen ->
                    when (currentScreen) {
                        AppNavigationItem.DICTIONARY -> {
                            DictionarySearchScreen(
                                viewModel = dictionaryViewModel,
                                onNavigateToAiSearch = { query ->
                                    reverseAiViewModel.onConceptQueryChanged(query)
                                    selectedItem = AppNavigationItem.REVERSE_AI
                                }
                            )
                        }
                        AppNavigationItem.REVERSE_AI -> {
                            ReverseAiSearchScreen(
                                viewModel = reverseAiViewModel,
                                onLookupInDictionary = { word ->
                                    dictionaryViewModel.searchWord(word)
                                    selectedItem = AppNavigationItem.DICTIONARY
                                },
                                onNavigateToSettings = {
                                    selectedItem = AppNavigationItem.SETTINGS
                                }
                            )
                        }
                        AppNavigationItem.SAVED -> {
                            BookmarksHistoryScreen(
                                favoritesViewModel = favoritesViewModel,
                                dictionaryViewModel = dictionaryViewModel,
                                onSelectWord = { word ->
                                    dictionaryViewModel.searchWord(word)
                                    selectedItem = AppNavigationItem.DICTIONARY
                                }
                            )
                        }
                        AppNavigationItem.SETTINGS -> {
                            SettingsScreen(
                                viewModel = settingsViewModel
                            )
                        }
                    }
                }
            }

            if (settingsUiState.showUpdateDialog && settingsUiState.updateInfo != null) {
                UpdateDialog(
                    updateInfo = settingsUiState.updateInfo!!,
                    downloadState = settingsUiState.downloadState,
                    onStartDownload = { url -> settingsViewModel.startDownload(url) },
                    onInstallApk = { file -> settingsViewModel.installApk(file) },
                    onDismiss = { settingsViewModel.dismissUpdateDialog() }
                )
            }
        }
    }
}
