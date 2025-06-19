package com.mobitechs.classapp.screens.chat


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.utils.formatTimestamp
import com.mobitechs.classapp.viewModel.chat.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    // Delete confirmation dialog
    if (uiState.showDeleteDialog && uiState.chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Delete Chat") },
            text = {
                Text("Are you sure you want to delete \"${uiState.chatToDelete!!.chatName}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.chatToDelete?.let { viewModel.deleteChat(it) }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("newChatScreen")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (uiState.chats.isNotEmpty()) {
                CommonSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    placeholder = "Search chats...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error!!)
                    }
                }
                else -> {
                    val filteredChats = viewModel.getFilteredChats()

                    if (filteredChats.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (uiState.searchQuery.isEmpty())
                                    "No chats yet"
                                else
                                    "No chats found for \"${uiState.searchQuery}\""
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredChats,
                                key = { it.chatId }
                            ) { chat ->
                                CommonChatListItem(
                                    item = ListItemData(
                                        id = chat.chatId,
                                        title = chat.chatName ?: "Chat",
                                        subtitle = chat.lastMessage,
                                        imageUrl = chat.chatImageUrl,
                                        trailing = chat.lastMessageTimestamp?.let {
                                            formatTimestamp(it)
                                        },
                                        unreadCount = 0 // TODO: Add unread count
                                    ),
                                    onClick = {
                                        navController.navigate("chat/${chat.chatId}")
                                    },
                                    onLongClick = {
                                        viewModel.showDeleteDialog(chat)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
