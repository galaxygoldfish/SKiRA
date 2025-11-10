package com.skira.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.PreferenceManager
import java.nio.file.Paths

/**
 * ViewModel corresponding to the SettingsDialogContent composable
 */
class SettingsDialogViewModel : ViewModel() {

    /* The currently selected path to which plots will be downloaded to
       Note that this value will not always represent the value saved in Preferences as it is user-selected */
    var selectedDownloadFolder by mutableStateOf(value = "")

    /**
     * Load the previously selected download directory from Preferences
     * If the user hasn't explicitly set a download folder before, by default we create ~/Downloads/SKiRA/
     */
    fun loadDownloadDirectory() {
        selectedDownloadFolder = PreferenceManager.getString(
            key = PreferenceKey.PLOT_DOWNLOAD_PATH,
            default = Paths.get(System.getProperty("user.home"), "Downloads", "SKiRA").toString()
        )!!
    }

    /**
     * Write the currently selected download directory to Preferences under the PLOT_DOWNLOAD_PATH key
     */
    fun saveDownloadDirectory() {
        PreferenceManager.putString(key = PreferenceKey.PLOT_DOWNLOAD_PATH, value = selectedDownloadFolder)
    }

}