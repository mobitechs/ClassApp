package com.mobitechs.classapp.ui.theme


enum class ThemeType(
    val displayName: String,
    val description: String,
    val isPremium: Boolean = false
) {
    CLASSIC_BLUE(
        displayName = "Classic Blue",
        description = "Professional blue theme for focused learning"
    ),
    OCEAN_BREEZE(
        displayName = "Ocean Breeze",
        description = "Calming teal and aqua colors",
        isPremium = true
    ),
    SUNSET_GLOW(
        displayName = "Sunset Glow",
        description = "Warm orange and pink gradients",
        isPremium = true
    ),
    FOREST_GREEN(
        displayName = "Forest Green",
        description = "Natural green tones for reduced eye strain",
        isPremium = true
    ),
    ROYAL_PURPLE(
        displayName = "Royal Purple",
        description = "Elegant purple and gold accents",
        isPremium = true
    ),
    MIDNIGHT_BLACK(
        displayName = "Midnight Black",
        description = "Pure AMOLED black theme",
        isPremium = true
    )
}