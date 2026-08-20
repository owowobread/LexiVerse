package com.example.data.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UrbanResponseDto(
    @Json(name = "list") val list: List<UrbanItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UrbanItemDto(
    @Json(name = "defid") val defid: Long = 0,
    @Json(name = "word") val word: String = "",
    @Json(name = "definition") val definition: String = "",
    @Json(name = "example") val example: String = "",
    @Json(name = "thumbs_up") val thumbs_up: Int = 0,
    @Json(name = "thumbs_down") val thumbs_down: Int = 0,
    @Json(name = "author") val author: String = "",
    @Json(name = "permalink") val permalink: String? = null,
    @Json(name = "written_on") val written_on: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterRequestDto(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenRouterMessageDto>,
    @Json(name = "temperature") val temperature: Double = 0.5,
    @Json(name = "max_tokens") val max_tokens: Int = 1000
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponseDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "choices") val choices: List<OpenRouterChoiceDto> = emptyList(),
    @Json(name = "error") val error: OpenRouterErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoiceDto(
    @Json(name = "message") val message: OpenRouterMessageDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterErrorDto(
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: Any? = null
)

@JsonClass(generateAdapter = true)
data class GitHubReleaseDto(
    @Json(name = "tag_name") val tag_name: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "html_url") val html_url: String? = null,
    @Json(name = "published_at") val published_at: String? = null,
    @Json(name = "assets") val assets: List<GitHubAssetDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubAssetDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "browser_download_url") val browser_download_url: String = "",
    @Json(name = "size") val size: Long = 0L,
    @Json(name = "content_type") val content_type: String? = null
)
