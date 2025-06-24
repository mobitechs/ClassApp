package com.mobitechs.classapp.data.repository

import android.content.Context
import com.mobitechs.classapp.ui.theme.ThemeManager
import com.mobitechs.classapp.ui.theme.ThemeType
import kotlinx.coroutines.flow.Flow

class ThemeRepository(private val context: Context) {
    private val themeManager = ThemeManager(context)

    fun getSelectedTheme(): Flow<ThemeType> = themeManager.selectedTheme

    fun getIsDarkMode(): Flow<Boolean> = themeManager.isDarkMode

    suspend fun saveTheme(theme: ThemeType) = themeManager.saveTheme(theme)

    suspend fun saveDarkMode(isDark: Boolean) = themeManager.saveDarkMode(isDark)

    suspend fun hasAccessToPremiumThemes(): Boolean = themeManager.hasAccessToPremiumThemes()
}