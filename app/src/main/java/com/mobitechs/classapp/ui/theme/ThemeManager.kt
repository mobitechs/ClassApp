package com.mobitechs.classapp.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    private val sharedPrefsManager = SharedPrefsManager(context, Gson())

    companion object {
        private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme")
        private val IS_DARK_MODE_KEY = stringPreferencesKey("is_dark_mode")
    }

    val selectedTheme: Flow<ThemeType> = context.themeDataStore.data.map { preferences ->
        val themeName = preferences[SELECTED_THEME_KEY] ?: ThemeType.CLASSIC_BLUE.name
        try {
            ThemeType.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeType.CLASSIC_BLUE
        }
    }

    val isDarkMode: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY]?.toBoolean() ?: false
    }

    suspend fun saveTheme(theme: ThemeType) {
        context.themeDataStore.edit { preferences ->
            preferences[SELECTED_THEME_KEY] = theme.name
        }
        // Also update the global state immediately
        ThemeState.currentTheme.value = theme
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark.toString()
        }
        // Also update the global state immediately
        ThemeState.isDarkMode.value = isDark
    }

    suspend fun hasAccessToPremiumThemes(): Boolean {
       // return sharedPrefsManager.isPremiumValid()
        return true
    }

    // Get current theme synchronously (for initialization)
    fun getCurrentThemeSync(): ThemeType {
        return runBlocking {
            selectedTheme.map { it }.first()
        }
    }
}