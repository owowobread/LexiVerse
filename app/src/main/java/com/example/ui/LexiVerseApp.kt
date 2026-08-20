package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MenuBook
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
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
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
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

    val themeSetting by appContainer.preferences.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM)

    // Optional silent update check on launch
    LaunchedEffect(Unit) {
        settingsViewModel.checkForUpdate(showDialogIfAvailable = false)
    }

    LexiVerseTheme(themeSetting = themeSetting) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
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
        }
    }
}
