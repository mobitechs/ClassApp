package com.mobitechs.classapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.screens.common.ImagePickerDialog
import com.mobitechs.classapp.screens.common.PrimaryButton
import com.mobitechs.classapp.ui.theme.AppTheme
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.rememberImagePickerWithOptions
import com.mobitechs.classapp.viewModel.profile.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val gson = Gson()
    val sharedPrefsManager = SharedPrefsManager(context, gson)

    val uiState by viewModel.uiState.collectAsState()

    // State for showing image picker dialog
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePicker = rememberImagePickerWithOptions(
        onImageSelected = { imagePath ->
            viewModel.updateProfileImage(imagePath)
        },
        onError = { error ->
            viewModel.showToast(error)
        }
    )

    ToastObserver(viewModel)

    // Navigate back if profile was updated successfully
    LaunchedEffect(uiState.isProfileUpdated) {
        if (uiState.isProfileUpdated) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = AppTheme.topAppBarColors
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture Section

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showImagePickerDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Camera icon overlay - Fixed positioning
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp) // Adjust position to prevent cropping
                            .size(32.dp) // Slightly smaller size
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp) // Smaller icon
                        )
                    }
                }
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primaryContainer)
//                        .clickable { showImagePickerDialog = true },
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (uiState.profileImageUrl.isNotEmpty()) {
//                        AsyncImage(
//                            model = uiState.profileImageUrl,
//                            contentDescription = "Profile Picture",
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )
//                    } else {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = null,
//                            modifier = Modifier.size(60.dp),
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//
//                    // Camera icon overlay
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.primary),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.CameraAlt,
//                            contentDescription = "Change Photo",
//                            tint = MaterialTheme.colorScheme.onPrimary,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to change photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Personal Information Section
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Form Fields
                ProfileTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    error = uiState.nameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    error = uiState.emailError,
                    enabled = false, // Email usually can't be changed
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::updatePhone,
                    label = "Phone Number",
                    leadingIcon = Icons.Default.Phone,
                    error = uiState.phoneError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selection
                GenderSelection(
                    selectedGender = uiState.gender,
                    onGenderSelected = viewModel::updateGender,
                    error = uiState.genderError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Blood Group Dropdown
                BloodGroupDropdown(
                    selectedBloodGroup = uiState.bloodGroup,
                    onBloodGroupSelected = viewModel::updateBloodGroup,
                    error = uiState.bloodGroupError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Address Section
                Text(
                    text = "Address Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                ProfileTextField(
                    value = uiState.address,
                    onValueChange = viewModel::updateAddress,
                    label = "Address",
                    leadingIcon = Icons.Default.Home,
                    error = uiState.addressError,
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = uiState.city,
                    onValueChange = viewModel::updateCity,
                    label = "City",
                    leadingIcon = Icons.Default.LocationCity,
                    error = uiState.cityError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = uiState.pincode,
                    onValueChange = viewModel::updatePincode,
                    label = "Pincode",
                    leadingIcon = Icons.Default.Pin,
                    error = uiState.pincodeError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Document Section
                Text(
                    text = "Document Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                ProfileTextField(
                    value = uiState.adharNo,
                    onValueChange = viewModel::updateAdharNo,
                    label = "Aadhar Number",
                    leadingIcon = Icons.Default.CreditCard,
                    error = uiState.adharNoError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = uiState.panNo,
                    onValueChange = viewModel::updatePanNo,
                    label = "PAN Number",
                    leadingIcon = Icons.Default.Badge,
                    error = uiState.panNoError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                PrimaryButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Image Picker Dialog
        if (showImagePickerDialog) {
            ImagePickerDialog(
                onDismiss = { showImagePickerDialog = false },
                onGalleryClick = { imagePicker.openGallery() },
                onCameraClick = { imagePicker.openCamera() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    error: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(),
        keyboardOptions = keyboardOptions,
        isError = error.isNotEmpty(),
        supportingText = if (error.isNotEmpty()) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

@Composable
fun GenderSelection(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    error: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderChip(
                text = "Male",
                isSelected = selectedGender == "Male",
                onClick = { onGenderSelected("Male") },
                modifier = Modifier.weight(1f)
            )

            GenderChip(
                text = "Female",
                isSelected = selectedGender == "Female",
                onClick = { onGenderSelected("Female") },
                modifier = Modifier.weight(1f)
            )

            GenderChip(
                text = "Other",
                isSelected = selectedGender == "Other",
                onClick = { onGenderSelected("Other") },
                modifier = Modifier.weight(1f)
            )
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun GenderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodGroupDropdown(
    selectedBloodGroup: String,
    onBloodGroupSelected: (String) -> Unit,
    error: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBloodGroup,
                onValueChange = { },
                readOnly = true,
                label = { Text("Blood Group") },
                leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(),
                isError = error.isNotEmpty()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bloodGroups.forEach { bloodGroup ->
                    DropdownMenuItem(
                        text = { Text(bloodGroup) },
                        onClick = {
                            onBloodGroupSelected(bloodGroup)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}