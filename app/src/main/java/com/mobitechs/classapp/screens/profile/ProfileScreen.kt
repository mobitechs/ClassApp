package com.mobitechs.classapp.screens.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.classapp.Screen
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.screens.common.ProfileMenuItem
import com.mobitechs.classapp.screens.home.ErrorView
import com.mobitechs.classapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = AppTheme.topAppBarColors,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading state
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            // Error state
            else if (uiState.error.isNotEmpty()) {
                ErrorView(
                    message = uiState.error,
                    onRetry = { /* Reload profile */ }
                )
            }
            // Profile content
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile header
                    ProfileHeader(
                        user = uiState.user,
                        onEditProfileClick = { /* Show edit profile dialog */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile menu
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            ProfileMenuSection(
                                title = "My Learning",
                                items = listOf(
                                    ProfileMenuItemData(
                                        title = "My Purchased Courses",
                                        icon = Icons.Default.School,
                                        route = "purchased_courses"
                                    ),
                                    ProfileMenuItemData(
                                        title = "My Payment History",
                                        icon = Icons.Default.Payment,
                                        route = Screen.PaymentHistoryScreen.route
                                    ),
                                    ProfileMenuItemData(
                                        title = "My Favorite Courses",
                                        icon = Icons.Default.Favorite,
                                        route = Screen.MyFavouriteScreen.route
                                    ),
                                    ProfileMenuItemData(
                                        title = "My Downloaded Courses",
                                        icon = Icons.Default.Download,
                                        route = Screen.MyDownloadsScreen.route
                                    ),
                                    ProfileMenuItemData(
                                        title = "My Wishlist",
                                        icon = Icons.Default.Bookmark,
                                        route = Screen.MyWishlistScreen.route

                                    ),
                                    ProfileMenuItemData(
                                        title = "Free Content",
                                        icon = Icons.Default.Star,
                                        route = Screen.FreeContentScreen.route,
                                        showDivider = false
                                    )
                                ),
                                onItemClick = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Additional options
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            ProfileMenuSection(
                                title = "App Settings",
                                items = listOf(
                                    ProfileMenuItemData(
                                        title = "Themes",
                                        icon = Icons.Default.AppShortcut,
                                        route = Screen.ThemeSelectionScreen.route,
                                        showDivider = false
                                    ),
                                    ProfileMenuItemData(
                                        title = "Add Feedback",
                                        icon = Icons.Default.Feedback,
                                        route = "feedbackScreen"
                                    ),
                                    ProfileMenuItemData(
                                        title = "Rate Us",
                                        icon = Icons.Default.ThumbUp,
                                        route = "rate"
                                    ),
                                    ProfileMenuItemData(
                                        title = "Privacy Policy",
                                        icon = Icons.Default.Security,
                                        route = "privacyPolicyScreen"
                                    ),
                                    ProfileMenuItemData(
                                        title = "Terms and Conditions",
                                        icon = Icons.Default.Description,
                                        route = "termConditionScreen",
                                        showDivider = false
                                    )
                                ),
                                onItemClick = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout button
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Logout")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Logout confirmation dialog
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to logout?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.logout()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Logout")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: Student?,
    onEditProfileClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (user?.photo != null) {
                    AsyncImage(
                        model = user.photo,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = user?.name?.first()?.toString() ?: "G",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = user?.name ?: "Guest Student",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email or phone
            Text(
                text = user?.email ?: user?.phone ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit profile button
            OutlinedButton(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun ProfileMenuSection(
    title: String,
    items: List<ProfileMenuItemData>,
    onItemClick: (String) -> Unit
) {
    Column {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Menu items
        items.forEach { item ->
            ProfileMenuItem(
                title = item.title,
                icon = item.icon,
                onClick = { onItemClick(item.route) },
                showDivider = item.showDivider
            )
        }
    }
}

data class ProfileMenuItemData(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val showDivider: Boolean = true
)