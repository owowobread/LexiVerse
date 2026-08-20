package com.example.data.network.scraper

import com.example.domain.model.VocabDefinitionItem
import com.example.domain.model.VocabularyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

class VocabularyScraper {

    suspend fun scrapeWord(word: String): Result<VocabularyResult> = withContext(Dispatchers.IO) {
        try {
            val encodedWord = URLEncoder.encode(word.trim().lowercase(), "UTF-8")
            val url = "https://www.vocabulary.com/dictionary/$encodedWord"

            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0")
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(10000)
                .get()

            val cleanWord = doc.selectFirst("h1.dynamic-text")?.text()?.trim()
                ?: doc.selectFirst("div.header h1")?.text()?.trim()
                ?: word

            // Short & long blurb (Vocabulary.com's famous conversational explanations)
            val shortBlurb = doc.selectFirst("p.short")?.text()?.trim()
                ?: doc.selectFirst(".short-blurb")?.text()?.trim()
            
            val longBlurb = doc.selectFirst("p.long")?.text()?.trim()
                ?: doc.selectFirst(".long-blurb")?.text()?.trim()

            // Definitions list
            val definitions = mutableListOf<VocabDefinitionItem>()
            
            val defElements = doc.select("div.definition, div.definitions ol li, div.word-definitions ol li")
            for (el in defElements) {
                val pos = el.selectFirst("a.pos, span.pos, div.pos")?.text()?.trim() ?: "definition"
                val defText = el.selectFirst("div.definition, div.def, span.definition")?.text()?.trim()
                    ?: el.ownText().trim()
                
                val exampleText = el.selectFirst("div.example, div.defContent div.example, .sentence")?.text()?.trim()

                if (defText.isNotBlank()) {
                    definitions.add(
                        VocabDefinitionItem(
                            partOfSpeech = pos,
                            definition = defText,
                            exampleSentence = exampleText.takeIf { !it.isNullOrBlank() }
                        )
                    )
                }
            }

            // Fallback definition parsing if list was empty
            if (definitions.isEmpty()) {
                val primaryDef = doc.selectFirst("h3.definition, div.definition")?.text()?.trim()
                val primaryPos = doc.selectFirst("span.pos, a.pos")?.text()?.trim() ?: "noun"
                if (!primaryDef.isNullOrBlank()) {
                    definitions.add(
                        VocabDefinitionItem(
                            partOfSpeech = primaryPos,
                            definition = primaryDef
                        )
                    )
                }
            }

            // Usage examples
            val usageExamples = mutableListOf<String>()
            val exampleElements = doc.select("div.sentence, div.example, div.instances li, div.sentence-list div.sentence")
            for (ex in exampleElements) {
                val text = ex.text().trim()
                if (text.isNotBlank() && text.length > 10 && !usageExamples.contains(text)) {
                    usageExamples.add(text)
                    if (usageExamples.size >= 8) break
                }
            }

            if (definitions.isEmpty() && shortBlurb.isNullOrBlank() && longBlurb.isNullOrBlank()) {
                Result.failure(Exception("No definitions found for '$word' on Vocabulary.com"))
            } else {
                Result.success(
                    VocabularyResult(
                        word = cleanWord,
                        shortBlurb = shortBlurb,
                        longBlurb = longBlurb,
                        primaryDefinition = definitions.firstOrNull()?.definition,
                        primaryPartOfSpeech = definitions.firstOrNull()?.partOfSpeech,
                        definitions = definitions,
                        usageExamples = usageExamples,
                        isOnline = true
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
