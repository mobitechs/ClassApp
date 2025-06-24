package com.mobitechs.classapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.MutableStateFlow

// Create a global theme state holder
object ThemeState {
    val currentTheme = MutableStateFlow(ThemeType.CLASSIC_BLUE)
    val isDarkMode = MutableStateFlow(false)
}

// Light Color Schemes for each theme
private val ClassicBlueLightColors = lightColorScheme(
    primary = ClassicBluePrimary,
    onPrimary = Color.White,
    primaryContainer = ClassicBluePrimary.copy(alpha = 0.12f),
    onPrimaryContainer = ClassicBluePrimary,
    secondary = ClassicBlueSecondary,
    onSecondary = Color.Black,
    background = ClassicBlueBackground,
    onBackground = Color.Black,
    surface = ClassicBlueSurface,
    onSurface = Color.Black,
    error = ErrorLight
)

private val ClassicBlueDarkColors = darkColorScheme(
    primary = ClassicBluePrimaryDark,
    onPrimary = Color.White,
    secondary = ClassicBlueSecondary,
    onSecondary = Color.Black,
    background = ClassicBlueDarkBackground,
    surface = ClassicBlueDarkSurface,
    error = ErrorDark
)

private val OceanBreezeLightColors = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = OceanTertiary.copy(alpha = 0.3f),
    onPrimaryContainer = OceanPrimary,
    secondary = OceanSecondary,
    onSecondary = Color.Black,
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = OceanSurface,
    error = ErrorLight
)

private val OceanBreezeDarkColors = darkColorScheme(
    primary = OceanSecondary,
    onPrimary = Color.Black,
    primaryContainer = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = OceanDarkBackground,
    surface = OceanDarkSurface,
    error = ErrorDark
)

private val SunsetGlowLightColors = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = SunsetTertiary.copy(alpha = 0.3f),
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    error = ErrorLight
)

private val SunsetGlowDarkColors = darkColorScheme(
    primary = SunsetPrimary,
    primaryContainer = SunsetPrimaryDark,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary,
    background = SunsetDarkBackground,
    surface = SunsetDarkSurface,
    error = ErrorDark
)

private val ForestGreenLightColors = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestTertiary.copy(alpha = 0.3f),
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = ForestSurface,
    error = ErrorLight
)

private val ForestGreenDarkColors = darkColorScheme(
    primary = ForestSecondary,
    primaryContainer = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestDarkBackground,
    surface = ForestDarkSurface,
    error = ErrorDark
)

private val RoyalPurpleLightColors = lightColorScheme(
    primary = RoyalPrimary,
    onPrimary = Color.White,
    primaryContainer = RoyalTertiary.copy(alpha = 0.3f),
    secondary = RoyalSecondary,
    tertiary = RoyalTertiary,
    background = RoyalBackground,
    surface = RoyalSurface,
    error = ErrorLight
)

private val RoyalPurpleDarkColors = darkColorScheme(
    primary = RoyalTertiary,
    primaryContainer = RoyalPrimary,
    secondary = RoyalSecondary,
    tertiary = RoyalTertiary,
    background = RoyalDarkBackground,
    surface = RoyalDarkSurface,
    error = ErrorDark
)

private val MidnightBlackColors = darkColorScheme(
    primary = MidnightSecondary,
    onPrimary = Color.Black,
    primaryContainer = MidnightTertiary,
    secondary = MidnightSecondary,
    tertiary = MidnightTertiary,
    background = MidnightBackground,
    surface = MidnightSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorDark
)

@Composable
fun ClassConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    selectedTheme: ThemeType = ThemeType.CLASSIC_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        selectedTheme == ThemeType.MIDNIGHT_BLACK -> MidnightBlackColors
        darkTheme -> when (selectedTheme) {
            ThemeType.CLASSIC_BLUE -> ClassicBlueDarkColors
            ThemeType.OCEAN_BREEZE -> OceanBreezeDarkColors
            ThemeType.SUNSET_GLOW -> SunsetGlowDarkColors
            ThemeType.FOREST_GREEN -> ForestGreenDarkColors
            ThemeType.ROYAL_PURPLE -> RoyalPurpleDarkColors
            else -> ClassicBlueDarkColors
        }

        else -> when (selectedTheme) {
            ThemeType.CLASSIC_BLUE -> ClassicBlueLightColors
            ThemeType.OCEAN_BREEZE -> OceanBreezeLightColors
            ThemeType.SUNSET_GLOW -> SunsetGlowLightColors
            ThemeType.FOREST_GREEN -> ForestGreenLightColors
            ThemeType.ROYAL_PURPLE -> RoyalPurpleLightColors
            ThemeType.MIDNIGHT_BLACK -> MidnightBlackColors
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme && selectedTheme != ThemeType.MIDNIGHT_BLACK
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ClassConnectThemeWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }

    // Collect theme state
    val selectedTheme by themeManager.selectedTheme.collectAsState(
        initial = ThemeType.CLASSIC_BLUE
    )
    val isDarkMode by themeManager.isDarkMode.collectAsState(
        initial = isSystemInDarkTheme()
    )

    // Update global theme state
    ThemeState.currentTheme.value = selectedTheme
    ThemeState.isDarkMode.value = isDarkMode

    ClassConnectTheme(
        darkTheme = isDarkMode,
        selectedTheme = selectedTheme,
        content = content
    )
}