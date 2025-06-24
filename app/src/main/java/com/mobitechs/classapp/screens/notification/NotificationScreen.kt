package com.mobitechs.classapp.screens.notification


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.NotificationItem
import com.mobitechs.classapp.screens.common.NotificationCard
import com.mobitechs.classapp.screens.home.ErrorView
import com.mobitechs.classapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
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
                    onRetry = { viewModel.loadNotifications() }
                )
            }
            // Empty state
            else if (uiState.notifications.isEmpty()) {
                EmptyNotificationsView()
            }
            // Content state - list of notifications
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.notifications) { notification ->
//                        NotificationCard(
//                            notification = notification,
//                            onNotificationClick = { selectedNotification ->
//                                handleNotificationClick(navController, selectedNotification)
//                            },
//                            formatDate = viewModel::formatDate
//                        )

                        NotificationCard(
                            notification = notification,
                            onNotificationClick = { selectedNotification ->
                                handleNotificationClick(navController, selectedNotification)
                            },
                            formatDate = viewModel::formatDate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Notifications Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You don't have any notifications at the moment. Check back later!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun handleNotificationClick(
    navController: NavController,
    notification: NotificationItem
) {
    // Handle notification click based on type
    if (notification.is_course == "Yes" && notification.course_id != null) {
        // Navigate to course details
        navController.navigate("course_details/${notification.course_id}")
    } else {
        // Navigate to notification details
        navController.navigate("notification_details/${notification.id}")
    }
}