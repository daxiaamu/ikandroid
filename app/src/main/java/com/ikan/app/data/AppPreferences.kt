package com.ikan.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ikan.app.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore by preferencesDataStore("settings")

class AppPreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")
    private val customThemeColorKey = intPreferencesKey("custom_theme_color")
    private val searchHistoryKey = stringPreferencesKey("search_history")
    private val favoritesListLayoutKey = booleanPreferencesKey("favorites_list_layout")
    private val historyListLayoutKey = booleanPreferencesKey("history_list_layout")
    private val backgroundPlaybackKey = booleanPreferencesKey("background_playback")

    val theme: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        runCatching { ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[dynamicColorKey] ?: true
    }
    val customThemeColor: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[customThemeColorKey] ?: DEFAULT_THEME_COLOR
    }
    val favoritesListLayout: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[favoritesListLayoutKey] ?: false
    }
    val historyListLayout: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[historyListLayoutKey] ?: false
    }
    val backgroundPlayback: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[backgroundPlaybackKey] ?: false
    }

    val searchHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        runCatching {
            val array = JSONArray(preferences[searchHistoryKey] ?: "[]")
            List(array.length()) { array.getString(it) }
        }.getOrDefault(emptyList())
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[dynamicColorKey] = enabled }
    }

    suspend fun setCustomThemeColor(color: Int) {
        context.dataStore.edit { it[customThemeColorKey] = color }
    }

    suspend fun setFavoritesListLayout(enabled: Boolean) {
        context.dataStore.edit { it[favoritesListLayoutKey] = enabled }
    }

    suspend fun setHistoryListLayout(enabled: Boolean) {
        context.dataStore.edit { it[historyListLayoutKey] = enabled }
    }

    suspend fun setBackgroundPlayback(enabled: Boolean) {
        context.dataStore.edit { it[backgroundPlaybackKey] = enabled }
    }

    suspend fun addSearch(query: String) {
        val value = query.trim()
        if (value.isBlank()) return
        context.dataStore.edit { preferences ->
            val old = runCatching {
                val array = JSONArray(preferences[searchHistoryKey] ?: "[]")
                List(array.length()) { array.getString(it) }
            }.getOrDefault(emptyList())
            preferences[searchHistoryKey] = JSONArray(
                listOf(value) + old.filterNot { it == value }.take(9),
            ).toString()
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it.remove(searchHistoryKey) }
    }

    private companion object {
        const val DEFAULT_THEME_COLOR = 0xFF216DFF.toInt()
    }
}
