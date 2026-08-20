import re

with open("app/src/main/java/com/example/ui/screens/DictionarySearchScreen.kt", "r") as f:
    content = f.read()

# Add missing imports for pager
imports_to_add = """import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
"""
if "import androidx.compose.foundation.pager.HorizontalPager" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.items\n", "import androidx.compose.foundation.lazy.items\n" + imports_to_add)


# Replace ActiveWordResultView with a pager version
old_active_view = """@Composable
fun ActiveWordResultView(
    result: UnifiedWordResult,
    selectedTab: DictionarySourceTab,
    isFavorite: Boolean,
    onSelectTab: (DictionarySourceTab) -> Unit,
    onToggleFavorite: () -> Unit,
    ttsManager: com.example.ui.components.TtsManager
) {
    val primaryDefinition = result.vocabularyResult?.primaryDefinition
        ?: result.offlineDefinition?.definition
        ?: result.urbanDefinitions.firstOrNull()?.definition
        ?: "No definition"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Header card with Word Title, Phonetic, Status Badge, and Action Icons
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnlineStatusBadge(isOffline = result.isOfflineFallback)
                        
                        WordActionRow(
                            word = result.queryWord,
                            definition = primaryDefinition,
                            isFavorite = isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            ttsManager = ttsManager
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = result.queryWord,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val phonetic = result.offlineDefinition?.phonetic
                    if (!phonetic.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = phonetic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            // Source Selector Tabs styled as Natural Tones pill chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    NaturalPillTab(
                        title = "Vocabulary",
                        isSelected = selectedTab == DictionarySourceTab.VOCABULARY,
                        onClick = { onSelectTab(DictionarySourceTab.VOCABULARY) }
                    )
                }
                item {
                    val countStr = if (result.urbanDefinitions.isNotEmpty()) " (${result.urbanDefinitions.size})" else ""
                    NaturalPillTab(
                        title = "Urban$countStr",
                        isSelected = selectedTab == DictionarySourceTab.URBAN,
                        onClick = { onSelectTab(DictionarySourceTab.URBAN) }
                    )
                }
                item {
                    NaturalPillTab(
                        title = "Offline",
                        isSelected = selectedTab == DictionarySourceTab.OFFLINE,
                        onClick = { onSelectTab(DictionarySourceTab.OFFLINE) }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            DictionarySourceTab.VOCABULARY -> {
                if (result.vocabularyResult != null) {
                    item {
                        VocabularyTabContent(vocab = result.vocabularyResult)
                    }
                } else {
                    item {
                        EmptySourceNotice(
                            sourceName = "Vocabulary.com",
                            message = "No online Vocabulary.com entry was found for '${result.queryWord}'. Displaying local dictionary below."
                        )
                    }
                    if (result.offlineDefinition != null) {
                        item {
                            OfflineTabContent(offline = result.offlineDefinition)
                        }
                    }
                }
            }
            DictionarySourceTab.URBAN -> {
                if (result.urbanDefinitions.isNotEmpty()) {
                    items(result.urbanDefinitions) { urbanItem ->
                        UrbanItemCard(item = urbanItem)
                    }
                } else {
                    item {
                        EmptySourceNotice(
                            sourceName = "Urban Dictionary",
                            message = "No slang or urban definitions found for '${result.queryWord}'."
                        )
                    }
                }
            }
            DictionarySourceTab.OFFLINE -> {
                if (result.offlineDefinition != null) {
                    item {
                        OfflineTabContent(offline = result.offlineDefinition)
                    }
                } else {
                    item {
                        EmptySourceNotice(
                            sourceName = "Offline Dictionary",
                            message = "'${result.queryWord}' is not yet in the offline database cache."
                        )
                    }
                }
            }
        }
    }
}"""

new_active_view = """@Composable
fun ActiveWordResultView(
    result: UnifiedWordResult,
    selectedTab: DictionarySourceTab,
    isFavorite: Boolean,
    onSelectTab: (DictionarySourceTab) -> Unit,
    onToggleFavorite: () -> Unit,
    ttsManager: com.example.ui.components.TtsManager
) {
    val primaryDefinition = result.vocabularyResult?.primaryDefinition
        ?: result.offlineDefinition?.definition
        ?: result.urbanDefinitions.firstOrNull()?.definition
        ?: "No definition"

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    // Sync external tab selection with pager
    LaunchedEffect(selectedTab) {
        val targetPage = when(selectedTab) {
            DictionarySourceTab.VOCABULARY -> 0
            DictionarySourceTab.URBAN -> 1
            DictionarySourceTab.OFFLINE -> 2
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
    
    // Sync pager swipe with external tab selection
    LaunchedEffect(pagerState.currentPage) {
        val newTab = when(pagerState.currentPage) {
            0 -> DictionarySourceTab.VOCABULARY
            1 -> DictionarySourceTab.URBAN
            else -> DictionarySourceTab.OFFLINE
        }
        if (selectedTab != newTab) {
            onSelectTab(newTab)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header card with Word Title, Phonetic, Status Badge, and Action Icons
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OnlineStatusBadge(isOffline = result.isOfflineFallback)
                    
                    WordActionRow(
                        word = result.queryWord,
                        definition = primaryDefinition,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        ttsManager = ttsManager
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.queryWord,
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val phonetic = result.offlineDefinition?.phonetic
                if (!phonetic.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = phonetic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Source Selector Tabs styled as Natural Tones pill chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                NaturalPillTab(
                    title = "Vocabulary",
                    isSelected = selectedTab == DictionarySourceTab.VOCABULARY,
                    onClick = { 
                        onSelectTab(DictionarySourceTab.VOCABULARY)
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
            item {
                val countStr = if (result.urbanDefinitions.isNotEmpty()) " (${result.urbanDefinitions.size})" else ""
                NaturalPillTab(
                    title = "Urban$countStr",
                    isSelected = selectedTab == DictionarySourceTab.URBAN,
                    onClick = { 
                        onSelectTab(DictionarySourceTab.URBAN)
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
            }
            item {
                NaturalPillTab(
                    title = "Offline",
                    isSelected = selectedTab == DictionarySourceTab.OFFLINE,
                    onClick = { 
                        onSelectTab(DictionarySourceTab.OFFLINE)
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
            }
        }

        // Tab Content via HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    0 -> {
                        if (result.vocabularyResult != null) {
                            item {
                                VocabularyTabContent(vocab = result.vocabularyResult)
                            }
                        } else {
                            item {
                                EmptySourceNotice(
                                    sourceName = "Vocabulary.com",
                                    message = "No online Vocabulary.com entry was found for '${result.queryWord}'. Displaying local dictionary below."
                                )
                            }
                            if (result.offlineDefinition != null) {
                                item {
                                    OfflineTabContent(offline = result.offlineDefinition)
                                }
                            }
                        }
                    }
                    1 -> {
                        if (result.urbanDefinitions.isNotEmpty()) {
                            items(result.urbanDefinitions) { urbanItem ->
                                UrbanItemCard(item = urbanItem)
                            }
                        } else {
                            item {
                                EmptySourceNotice(
                                    sourceName = "Urban Dictionary",
                                    message = "No slang or urban definitions found for '${result.queryWord}'."
                                )
                            }
                        }
                    }
                    2 -> {
                        if (result.offlineDefinition != null) {
                            item {
                                OfflineTabContent(offline = result.offlineDefinition)
                            }
                        } else {
                            item {
                                EmptySourceNotice(
                                    sourceName = "Offline Dictionary",
                                    message = "'${result.queryWord}' is not yet in the offline database cache."
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}"""

content = content.replace(old_active_view, new_active_view)

with open("app/src/main/java/com/example/ui/screens/DictionarySearchScreen.kt", "w") as f:
    f.write(content)
