package com.skira.app.structures

/**
 * Holds metadata about an available app update fetched from GitHub Releases.
 *
 * @param currentVersion The version string currently running (e.g. "1.3.0")
 * @param latestVersion The version string of the available release (e.g. "1.4.0")
 * @param releaseNotes The markdown body of the GitHub release
 * @param downloadUrl Direct download URL for the platform-appropriate asset
 * @param assetName File name of the asset (e.g. "SKiRA-1.4.0.dmg")
 * @param assetSize Asset size in bytes reported by GitHub (0 if unknown)
 */
data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val assetName: String,
    val assetSize: Long
)

