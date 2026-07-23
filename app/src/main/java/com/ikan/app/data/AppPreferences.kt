package com.ikan.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ikan.app.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore by preferencesDataStore("settings")

class AppPreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val searchHistoryKey = stringPreferencesKey("search_history")

    val theme: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        runCatching { ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
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
}
