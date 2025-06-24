package com.mobitechs.classapp.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.repository.ThemeRepository
import com.mobitechs.classapp.ui.theme.ThemeState
import com.mobitechs.classapp.ui.theme.ThemeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeSelectionViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _selectedTheme = MutableStateFlow(ThemeType.CLASSIC_BLUE)
    val selectedTheme: StateFlow<ThemeType> = _selectedTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    init {
        // Observe theme changes
        viewModelScope.launch {
            themeRepository.getSelectedTheme().collect { theme ->
                _selectedTheme.value = theme
                ThemeState.currentTheme.value = theme // Update global state
            }
        }

        // Observe dark mode changes
        viewModelScope.launch {
            themeRepository.getIsDarkMode().collect { isDark ->
                _isDarkMode.value = isDark
                ThemeState.isDarkMode.value = isDark // Update global state
            }
        }

        // Check premium status
        viewModelScope.launch {
            _isPremiumUser.value = themeRepository.hasAccessToPremiumThemes()
        }
    }

    fun selectTheme(theme: ThemeType) {
        viewModelScope.launch {
            themeRepository.saveTheme(theme)
            _selectedTheme.value = theme // Update local state immediately
            ThemeState.currentTheme.value = theme // Update global state immediately
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themeRepository.saveDarkMode(isDark)
            _isDarkMode.value = isDark // Update local state immediately
            ThemeState.isDarkMode.value = isDark // Update global state immediately
        }
    }
}