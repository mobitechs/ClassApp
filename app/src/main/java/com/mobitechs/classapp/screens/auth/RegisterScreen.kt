package com.mobitechs.classapp.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobitechs.classapp.R
import com.mobitechs.classapp.screens.common.AppTextField
import com.mobitechs.classapp.screens.common.DropDown
import com.mobitechs.classapp.screens.common.PasswordTextField
import com.mobitechs.classapp.screens.common.PrimaryButton


@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle one-time events
    LaunchedEffect(uiState.onNavigateToHome) {
        if (uiState.onNavigateToHome) {
            onNavigateToHome()
            viewModel.resetNavigation()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Class Connect Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                // Header
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Join our learning community today",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Registration card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Name input
                        AppTextField(
                            value = uiState.name ?: "",
                            onValueChange = { viewModel.updateName(it) },
                            label = "Full Name",
                            leadingIcon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            error = uiState.nameError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email input
                        AppTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = "Email Address",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            error = uiState.emailError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone input
                        AppTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updatePhone(it) },
                            label = "Phone Number",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                            error = uiState.phoneError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gender selection
                        val genderOptions =
                            remember { listOf("male", "female", "Other", "Prefer not to say") }
                        DropDown(
                            options = genderOptions,
                            selectedOption = uiState.gender ?: "Select Gender",
                            onOptionSelected = { viewModel.updateGender(it) },
                            label = "Gender",
                            error = uiState.genderError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // City input
                        AppTextField(
                            value = uiState.city ?: "",
                            onValueChange = { viewModel.updateCity(it) },
                            label = "City",
                            leadingIcon = Icons.Default.LocationCity,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            error = uiState.cityError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pincode input
                        AppTextField(
                            value = uiState.pincode ?: "",
                            onValueChange = { viewModel.updatePinCode(it) },
                            label = "Pincode",
                            leadingIcon = Icons.Default.Home,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            error = uiState.pincodeError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password input
                        PasswordTextField(
                            value = uiState.password ?: "",
                            onValueChange = { viewModel.updatePassword(it) },
                            label = "Password",
                            error = uiState.passwordError,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password input
                        PasswordTextField(
                            value = uiState.password_confirmation ?: "",
                            onValueChange = { viewModel.updateConfirmPassword(it) },
                            label = "Confirm Password",
                            error = uiState.password_confirmationError,
                            imeAction = ImeAction.Done
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Register button
                        PrimaryButton(
                            text = "Register",
                            onClick = { viewModel.register() },
                            enabled = !uiState.isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login option
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Error message
            if (uiState.error.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}