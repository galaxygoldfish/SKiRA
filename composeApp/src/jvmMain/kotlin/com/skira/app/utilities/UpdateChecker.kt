package com.skira.app.utilities

import com.skira.app.structures.UpdateInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


private const val GITHUB_OWNER = "galaxygoldfish"
private const val GITHUB_REPO  = "SKiRA"

private val json = Json { ignoreUnknownKeys = true }

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

@Serializable
private data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long = 0L
)

@Serializable
private data class GitHubRelease(
    val tag_name: String,
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

/**
 * Queries the GitHub Releases API and returns an [UpdateInfo] when a newer version
 * than [AppVersion.CURRENT] is available, or `null` when the app is already up-to-date.
 *
 * Network errors and HTTP failures are captured and returned as a failing [Result].
 * Call this function from a background thread / coroutine with [kotlinx.coroutines.Dispatchers.IO].
 */
fun checkForUpdate(): Result<UpdateInfo?> = runCatching {
    val url = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    val request = Request.Builder()
        .url(url)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .build()

    val responseBody = httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@runCatching null
        response.body?.string() ?: return@runCatching null
    }

    val release = json.decodeFromString<GitHubRelease>(responseBody)
    val latestVersion = release.tag_name.trimStart('v').trim()

    if (!isNewerVersion(current = AppVersion.CURRENT, latest = latestVersion)) {
        return@runCatching null
    }

    val asset = pickPlatformAsset(release.assets) ?: return@runCatching null

    UpdateInfo(
        currentVersion = AppVersion.CURRENT,
        latestVersion = latestVersion,
        releaseNotes = release.body,
        downloadUrl = asset.browser_download_url,
        assetName = asset.name,
        assetSize = asset.size
    )
}

/** Picks the most appropriate release asset for the current OS. */
private fun pickPlatformAsset(assets: List<GitHubAsset>): GitHubAsset? {
    val os = System.getProperty("os.name")?.lowercase() ?: ""
    return when {
        os.contains("mac") ->
            assets.firstOrNull { it.name.endsWith(".dmg", ignoreCase = true) }
        os.contains("win") ->
            assets.firstOrNull { it.name.endsWith(".msi", ignoreCase = true) }
                ?: assets.firstOrNull { it.name.endsWith(".exe", ignoreCase = true) }
        else -> assets.firstOrNull()
    }
}

/**
 * Returns `true` when [latest] represents a higher semantic version than [current].
 * Both strings should be in the form `MAJOR.MINOR.PATCH` (leading `v` is ignored).
 */
private fun isNewerVersion(current: String, latest: String): Boolean {
    fun parse(v: String): Triple<Int, Int, Int> {
        val parts = v.trim().split(".").map { it.toIntOrNull() ?: 0 }
        return Triple(
            parts.getOrElse(0) { 0 },
            parts.getOrElse(1) { 0 },
            parts.getOrElse(2) { 0 }
        )
    }
    val (cMaj, cMin, cPat) = parse(current)
    val (lMaj, lMin, lPat) = parse(latest)
    return lMaj > cMaj
        || (lMaj == cMaj && lMin > cMin)
        || (lMaj == cMaj && lMin == cMin && lPat > cPat)
}

