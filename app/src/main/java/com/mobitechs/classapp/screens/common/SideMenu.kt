package com.mobitechs.classapp.screens.common


import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobitechs.classapp.R
import com.mobitechs.classapp.Screen

import com.mobitechs.classapp.data.model.response.Student

@Composable
fun SideMenu(
    onMenuItemClick: (String) -> Unit,
    onClose: () -> Unit,
    user: Student? = null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)

            .verticalScroll(rememberScrollState())
    ) {
        // Header with close button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Menu"
                )
            }
        }

        // Student profile section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // Student image
            if (user?.photo != null) {
                AsyncImage(
                    model = user.photo,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_avatar),
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student name
            Text(
                text = user?.name ?: "Guest Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Student email or phone
            Text(
                text = user?.email ?: user?.phone ?: "Sign in to access your account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Divider()


        // Menu items
        ProfileMenuItem(
            title = "Profile",
            icon = Icons.Default.Person,
            onClick = { onMenuItemClick(Screen.ProfileScreen.route) }
        )

        ProfileMenuItem(
            title = "Offline Downloads",
            icon = Icons.Default.CloudDownload,
            onClick = { onMenuItemClick(Screen.MyDownloadScreen.route) }
        )

        ProfileMenuItem(
            title = "Free Content",
            icon = Icons.Default.FreeBreakfast,
            onClick = { onMenuItemClick(Screen.FreeContentScreen.route) }
        )

        ProfileMenuItem(
            title = "Payment History",
            icon = Icons.Default.Payment,
            onClick = { onMenuItemClick(Screen.PaymentHistoryScreen.route) }
        )

        ProfileMenuItem(
            title = "Add Feedback",
            icon = Icons.Default.Feedback,
            onClick = { onMenuItemClick(Screen.FeedbackScreen.route) }
        )

        ProfileMenuItem(
            title = "Rate Us",
            icon = Icons.Default.RateReview,
            onClick = { onMenuItemClick("rate") }
        )

        ProfileMenuItem(
            title = "Privacy Policy",
            icon = Icons.Default.Security,
            onClick = { onMenuItemClick(Screen.PrivacyPolicyScreen.route) }
        )

        ProfileMenuItem(
            title = "Terms and Conditions",
            icon = Icons.Default.Policy,
            onClick = { onMenuItemClick(Screen.TermConditionScreen.route) },
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        ProfileMenuItem(
            title = "Logout",
            icon = Icons.Default.Logout,
            onClick = { onMenuItemClick(Screen.LoginScreen.route) },
            showDivider = false,
            iconTint = MaterialTheme.colorScheme.error
        )
    }
}