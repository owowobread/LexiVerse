package com.example.data.repository

import com.example.BuildConfig
import com.example.data.network.api.GitHubApi
import com.example.data.updater.ApkDownloaderAndInstaller
import com.example.data.updater.DownloadState
import com.example.domain.model.AppUpdateInfo
import com.example.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class UpdateRepository(
    private val gitHubApi: GitHubApi,
    private val apkDownloaderAndInstaller: ApkDownloaderAndInstaller
) {

    fun checkForUpdate(owner: String, repo: String): Flow<Resource<AppUpdateInfo>> = flow {
        emit(Resource.Loading)
        val cleanOwner = owner.trim()
        val cleanRepo = repo.trim()

        if (cleanOwner.isBlank() || cleanRepo.isBlank()) {
            emit(Resource.Error("GitHub repository owner and repo name must not be empty."))
            return@flow
        }

        try {
            val response = gitHubApi.getLatestRelease(cleanOwner, cleanRepo)
            if (!response.isSuccessful) {
                if (response.code() == 404) {
                    emit(
                        Resource.Error(
                            "No releases found for GitHub repo $cleanOwner/$cleanRepo. Please verify repository owner and name in Settings."
                        )
                    )
                } else {
                    emit(
                        Resource.Error(
                            "GitHub API error (${response.code()}): ${response.message()}"
                        )
                    )
                }
                return@flow
            }

            val release = response.body()
            if (release == null) {
                emit(Resource.Error("Empty release response from GitHub"))
                return@flow
            }

            val remoteTag = release.tag_name.trim()
            val currentVersion = BuildConfig.VERSION_NAME // e.g. "1.0.0"

            val isNewer = isVersionNewer(remoteTag, currentVersion)
            val apkAsset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true) ||
                        it.content_type == "application/vnd.android.package-archive"
            }

            val updateInfo = AppUpdateInfo(
                isChecking = false,
                updateAvailable = isNewer && apkAsset != null,
                latestVersion = remoteTag,
                currentVersion = currentVersion,
                releaseNotes = release.body ?: "No release notes provided.",
                downloadUrl = apkAsset?.browser_download_url,
                apkSize = apkAsset?.size ?: 0L
            )

            emit(Resource.Success(updateInfo))
        } catch (e: Exception) {
            emit(Resource.Error("Error checking for updates: ${e.localizedMessage ?: "Network error"}"))
        }
    }.flowOn(Dispatchers.IO)

    fun downloadApk(url: String): Flow<DownloadState> =
        apkDownloaderAndInstaller.downloadApk(url)

    fun hasInstallPermission(): Boolean =
        apkDownloaderAndInstaller.hasInstallPermission()

    fun requestInstallPermissionIntent() =
        apkDownloaderAndInstaller.requestInstallPermissionIntent()

    fun installApk(file: File): Result<Unit> =
        apkDownloaderAndInstaller.installApk(file)

    private fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
        try {
            val cleanRemote = remoteVersion.removePrefix("v").removePrefix("V").trim()
            val cleanLocal = localVersion.removePrefix("v").removePrefix("V").trim()

            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
            val localParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
            return false
        } catch (e: Exception) {
            return remoteVersion != localVersion
        }
    }
}
