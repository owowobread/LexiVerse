package com.example.data.repository

import com.example.data.network.api.OpenRouterApi
import com.example.data.network.dto.OpenRouterMessageDto
import com.example.data.network.dto.OpenRouterRequestDto
import com.example.data.preferences.LexiVersePreferences
import com.example.domain.model.AiCandidateWord
import com.example.domain.model.Resource
import com.example.domain.model.ReverseAiSearchResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject

class AiRepository(
    private val openRouterApi: OpenRouterApi,
    private val preferences: LexiVersePreferences
) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun reverseConceptSearch(conceptPrompt: String): Flow<Resource<ReverseAiSearchResult>> = flow {
        emit(Resource.Loading)
        val cleanPrompt = conceptPrompt.trim()
        if (cleanPrompt.isBlank()) {
            emit(Resource.Error("Concept description cannot be empty."))
            return@flow
        }

        val apiKey = preferences.openRouterApiKey.first()
        if (apiKey.isBlank()) {
            emit(
                Resource.Error(
                    "OpenRouter API key is missing. Please go to Settings and enter your OpenRouter API Key to use Reverse AI Search."
                )
            )
            return@flow
        }

        val model = preferences.openRouterModel.first().ifBlank {
            LexiVersePreferences.DEFAULT_MODEL
        }

        val systemPrompt = """
            You are a master lexicographer and vocabulary assistant. 
            The user will provide a concept, feeling, obscure nuance, or descriptive scenario.
            Your task is to identify the top 3 to 5 most precise, elegant, or evocative vocabulary words that match this description.
            
            You MUST respond ONLY with a raw JSON array of objects (no introductory markdown, no backticks, just valid JSON) matching this exact schema:
            [
              {
                "word": "String (Word name)",
                "phonetic": "String (IPA pronunciation)",
                "partOfSpeech": "String (noun/verb/adjective/etc.)",
                "definition": "String (Crisp, accurate one-sentence definition)",
                "nuance": "String (Explain why this word precisely captures the user's specific context or feeling)",
                "exampleSentence": "String (Natural example sentence in context)"
              }
            ]
        """.trimIndent()

        try {
            val request = OpenRouterRequestDto(
                model = model,
                messages = listOf(
                    OpenRouterMessageDto(role = "system", content = systemPrompt),
                    OpenRouterMessageDto(role = "user", content = "Find the best vocabulary words for this concept: $cleanPrompt")
                ),
                temperature = 0.4,
                max_tokens = 1200
            )

            val response = openRouterApi.getChatCompletions(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                emit(
                    Resource.Error(
                        "OpenRouter Error (${response.code()}): ${errorBody ?: response.message()}. Please verify your API key and model in Settings."
                    )
                )
                return@flow
            }

            val body = response.body()
            if (body?.error != null) {
                emit(Resource.Error("API returned error: ${body.error.message ?: "Unknown error"}"))
                return@flow
            }

            val rawContent = body?.choices?.firstOrNull()?.message?.content
            if (rawContent.isNullOrBlank()) {
                emit(Resource.Error("Received empty response from AI model."))
                return@flow
            }

            val candidates = parseAiResponse(rawContent)
            if (candidates.isEmpty()) {
                emit(
                    Resource.Error(
                        "Could not parse vocabulary words from AI output. Please try rephrasing your description."
                    )
                )
            } else {
                emit(
                    Resource.Success(
                        ReverseAiSearchResult(
                            conceptQuery = cleanPrompt,
                            candidates = candidates,
                            rawExplanation = rawContent
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error contacting OpenRouter: ${e.localizedMessage ?: "Failed to connect"}"))
        }
    }.flowOn(Dispatchers.IO)

    fun testConnection(apiKey: String, model: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        if (apiKey.isBlank()) {
            emit(Resource.Error("API Key is empty."))
            return@flow
        }
        val targetModel = model.ifBlank { LexiVersePreferences.DEFAULT_MODEL }

        try {
            val request = OpenRouterRequestDto(
                model = targetModel,
                messages = listOf(
                    OpenRouterMessageDto(role = "user", content = "Reply with 'LexiVerse Ready' if you can read this.")
                ),
                max_tokens = 30
            )
            val response = openRouterApi.getChatCompletions(
                authorization = "Bearer $apiKey",
                request = request
            )
            if (response.isSuccessful) {
                val reply = response.body()?.choices?.firstOrNull()?.message?.content ?: "Connected!"
                emit(Resource.Success("Successfully connected to OpenRouter ($targetModel): ${reply.trim()}"))
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                emit(Resource.Error("Connection test failed (HTTP ${response.code()}): $err"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Connection test failed: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseAiResponse(content: String): List<AiCandidateWord> {
        val candidates = mutableListOf<AiCandidateWord>()
        try {
            // Clean markdown codeblocks if model wrapped in ```json ... ```
            var cleanJson = content.trim()
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substringAfter("\n").substringBeforeLast("```").trim()
            }
            // If json is wrapped or has text before array
            val arrayStart = cleanJson.indexOf('[')
            val arrayEnd = cleanJson.lastIndexOf(']')
            if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
                cleanJson = cleanJson.substring(arrayStart, arrayEnd + 1)
            }

            val jsonArray = JSONArray(cleanJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val word = obj.optString("word")
                if (word.isNotBlank()) {
                    candidates.add(
                        AiCandidateWord(
                            word = word,
                            phonetic = obj.optString("phonetic").takeIf { it.isNotBlank() },
                            partOfSpeech = obj.optString("partOfSpeech").takeIf { it.isNotBlank() },
                            definition = obj.optString("definition", "Definition"),
                            nuance = obj.optString("nuance", "Matches your concept"),
                            exampleSentence = obj.optString("exampleSentence").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback line-by-line parsing if json parsing failed
        }
        return candidates
    }
}
