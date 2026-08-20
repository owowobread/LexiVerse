package com.example.data.network.api

import com.example.data.network.dto.GitHubReleaseDto
import com.example.data.network.dto.OpenRouterRequestDto
import com.example.data.network.dto.OpenRouterResponseDto
import com.example.data.network.dto.UrbanResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface UrbanDictionaryApi {
    @GET("v0/define")
    suspend fun getDefinitions(
        @Query("term") term: String
    ): Response<UrbanResponseDto>
}

interface OpenRouterApi {
    @POST
    suspend fun getChatCompletions(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/maddyisthegame/LexiVerse",
        @Header("X-Title") title: String = "LexiVerse Android",
        @Body request: OpenRouterRequestDto
    ): Response<OpenRouterResponseDto>
}

interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authorization: String? = null
    ): Response<GitHubReleaseDto>
}
