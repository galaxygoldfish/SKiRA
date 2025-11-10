package com.skira.app.utilities

import java.util.prefs.Preferences

/**
 * A wrapper class for managing application preferences using Java Preferences API.
 *
 * To ensure consistent Preference access, we use the [com.skira.app.structures.PreferenceKey] object to
 * store all keys used in preferences and reference this when we read/write values
 */
object PreferenceManager {
    private val prefs: Preferences = Preferences.userRoot().node("com.skira.app")

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
}