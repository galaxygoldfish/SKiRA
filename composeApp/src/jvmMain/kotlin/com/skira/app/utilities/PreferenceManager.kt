package com.skira.app.utilities

import java.util.prefs.Preferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.skira.app.structures.PreferenceKey

/**
 * A wrapper class for managing application preferences using Java Preferences API.
 *
 * To ensure consistent Preference access, we use the [com.skira.app.structures.PreferenceKey] object to
 * store all keys used in preferences and reference this when we read/write values
 */
object PreferenceManager {
    val prefs: Preferences = Preferences.userRoot().node("com.skira.app")

    /**
     * Store a given string in the current Preferences
     *
     * @param key The key under which the string is stored
     * @param value The string value to store
     */
    fun putString(key: String, value: String) = prefs.put(key, value)

    /**
     * Retrieve a string from the current Preferences or the default value if it doesn't exist
     *
     * @param key The key under which the string is stored
     * @param default The default value to return if the key doesn't exist
     *
     * @return The retrieved string or the default value
     */
    fun getString(key: String, default: String? = null): String? = prefs.get(key, default)

    /**
     * Store a given boolean in the current Preferences
     *
     * @param key The key under which the boolean is stored
     * @param value The boolean value to store
     */
    fun putBoolean(key: String, value: Boolean) = prefs.putBoolean(key, value)

    /**
     * Retrieve a boolean from the current Preferences or the default value if it doesn't exist
     *
     * @param key The key under which the boolean is stored
     * @param default The default value to return if the key doesn't exist
     *
     * @return The retrieved boolean or the default value
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)

    private val _colorSchemesState: MutableState<List<List<String>>> =
        mutableStateOf(getColorSchemes(PreferenceKey.CUSTOM_COLOR_SCHEMES))
    val colorSchemesState: State<List<List<String>>> get() = _colorSchemesState

    // Custom color schemes serialization helpers
    // Schemes are stored as: "#RRGGBB,#RRGGBB;#RRGGBB,#RRGGBB,#RRGGBB"
    fun putColorSchemes(key: String, schemes: List<List<String>>) {
        val serialized = schemes.joinToString(";") { scheme ->
            scheme.joinToString(",") { color -> color.trim() }
        }
        putString(key, serialized)
        _colorSchemesState.value = schemes
    }

    fun getColorSchemes(key: String): List<List<String>> {
        val raw = getString(key, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.splitToSequence(';')
            .map { scheme ->
                scheme.splitToSequence(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toList()
            }
            .filter { it.isNotEmpty() }
            .toList()
    }

    /**
     * Removes a color scheme at the specified index
     *
     * @param key The key under which the color schemes are stored
     * @param index The index of the color scheme to remove
     *
     * @return True if the color scheme was removed, false if the index was invalid
     */
    fun removeColorScheme(key: String, index: Int): Boolean {
        val schemes = getColorSchemes(key).toMutableList()
        if (index < 0 || index >= schemes.size) return false
        schemes.removeAt(index)
        putColorSchemes(key, schemes)
        return true
    }

    /**
     * Clears all persisted preference values for this app.
     */
    fun clearAll() {
        prefs.clear()
        _colorSchemesState.value = emptyList()
    }
}