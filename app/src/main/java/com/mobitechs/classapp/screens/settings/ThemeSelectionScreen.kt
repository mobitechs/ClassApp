package com.mobitechs.classapp.screens.settings


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    viewModel: ThemeSelectionViewModel,
    navController: NavController
) {
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showPremiumDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Dark Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Switch between light and dark appearance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    viewModel.setDarkMode(it)
                                }
                            }
                        )
                    }
                }
            }

            // Theme Options
            items(ThemeType.values().toList()) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = selectedTheme == theme,
                    isPremiumUser = isPremiumUser,
                    isDarkMode = isDarkMode,
                    onThemeSelected = {
                        if (theme.isPremium && !isPremiumUser) {
                            showPremiumDialog = true
                        } else {
                            coroutineScope.launch {
                                viewModel.selectTheme(theme)
                            }
                        }
                    }
                )
            }
        }
    }

    // Premium Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            icon = {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Premium Themes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Unlock premium themes and enjoy a personalized learning experience with exclusive color schemes designed to enhance your focus and productivity.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Navigate to premium purchase screen
                        showPremiumDialog = false
                    }
                ) {
                    Text("Upgrade to Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}

@Composable
fun ThemeCard(
    theme: ThemeType,
    isSelected: Boolean,
    isPremiumUser: Boolean,
    isDarkMode: Boolean,
    onThemeSelected: () -> Unit
) {
    val colors = getThemePreviewColors(theme, isDarkMode)
    val isLocked = theme.isPremium && !isPremiumUser

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked || !theme.isPremium) { onThemeSelected() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color Preview Circles
                Box {
                    colors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = (index * 20).dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width((colors.size * 20).dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            theme.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (theme.isPremium) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "PRO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            } else if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun getThemePreviewColors(theme: ThemeType, isDarkMode: Boolean): List<Color> {
    return when (theme) {
        ThemeType.CLASSIC_BLUE -> if (isDarkMode)
            listOf(ClassicBluePrimaryDark, ClassicBlueSecondary, ClassicBlueDarkBackground)
        else
            listOf(ClassicBluePrimary, ClassicBlueSecondary, ClassicBlueBackground)

        ThemeType.OCEAN_BREEZE -> if (isDarkMode)
            listOf(OceanSecondary, OceanTertiary, OceanDarkBackground)
        else
            listOf(OceanPrimary, OceanSecondary, OceanTertiary)

        ThemeType.SUNSET_GLOW -> if (isDarkMode)
            listOf(SunsetPrimary, SunsetSecondary, SunsetDarkBackground)
        else
            listOf(SunsetPrimary, SunsetSecondary, SunsetTertiary)

        ThemeType.FOREST_GREEN -> if (isDarkMode)
            listOf(ForestSecondary, ForestTertiary, ForestDarkBackground)
        else
            listOf(ForestPrimary, ForestSecondary, ForestTertiary)

        ThemeType.ROYAL_PURPLE -> if (isDarkMode)
            listOf(RoyalTertiary, RoyalSecondary, RoyalDarkBackground)
        else
            listOf(RoyalPrimary, RoyalSecondary, RoyalTertiary)

        ThemeType.MIDNIGHT_BLACK ->
            listOf(Color.Black, MidnightSecondary, MidnightTertiary)
    }
}