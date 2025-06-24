package com.mobitechs.classapp.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.mobitechs.classapp.screens.common.PhotoOptionsDialog
import com.mobitechs.classapp.viewModel.chat.ChatViewModel
import kotlinx.coroutines.launch
import androidx.activity.result.PickVisualMediaRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    navController: NavController,
    chatId: String
) {
    // Initialize the chat
    LaunchedEffect(chatId) {
        viewModel.initializeChat(chatId)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            uiState.tempPhotoUri?.let { uri ->
                viewModel.sendPhotoMessage(uri)
            }
        }
    }


    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createTempPhotoUri()
            cameraLauncher.launch(uri)
        }
    }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendPhotoMessage(it)
        }
    }

    val legacyGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendPhotoMessage(it)
        }
    }


    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    // Photo options dialog
    if (uiState.showPhotoOptions) {
        PhotoOptionsDialog(
            onDismiss = { viewModel.hidePhotoOptions() },
            onTakePhoto = {
                viewModel.hidePhotoOptions()
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val uri = viewModel.createTempPhotoUri()
                        cameraLauncher.launch(uri)
                    }

                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            onSelectFromGallery = {
                viewModel.hidePhotoOptions()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use photo picker for Android 13+
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    // Use legacy gallery for older versions
                    legacyGalleryLauncher.launch("image/*")
                }
            }

        )
    }

    // Clear chat confirmation dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDialog() },
            title = { Text("Clear Chat") },
            text = { Text("Are you sure you want to clear all messages? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearChat() }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear chat confirmation dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDialog() },
            title = { Text("Clear Chat") },
            text = { Text("Are you sure you want to clear all messages? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearChat() }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.chat?.chatName ?: "Chat")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear chat") },
                            onClick = {
                                showMenu = false
                                viewModel.showClearDialog()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isInitialized && uiState.error == null) {
                ChatInput(
                    onSendMessage = viewModel::sendMessage,
                    onAttachClick = { viewModel.showPhotoOptions() },
                    enabled = !uiState.isSending
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when {
                !uiState.isInitialized || uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (uiState.messages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No messages yet. Start a conversation!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(
                                items = uiState.messages,
                                key = { it.messageId }
                            ) { message ->
                                MessageItem(
                                    message = message,
                                    isCurrentUser = message.senderId == viewModel.currentUserId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
