package com.mobitechs.classapp.viewModel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatUser
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import android.util.Log
import com.mobitechs.classapp.screens.chat.ChatTestDataHelper
import kotlinx.coroutines.flow.first

data class NewChatUiState(
    val users: List<ChatUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreatingChat: Boolean = false,
    val createdChatId: String? = null,
    val navigateToChatId: String? = null
)

class NewChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatUserRepository: ChatUserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    private val currentUserId: String by lazy {
        authRepository.getCurrentUserId() ?: "5"
    }

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // For now, create dummy users if none exist
                createDummyUsersIfNeeded()

                // Get all users except current user
                chatUserRepository.getAllUsers().collect { allUsers ->
                    val filteredUsers = allUsers.filter { it.userId != currentUserId }
                    _uiState.value = _uiState.value.copy(
                        users = filteredUsers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("NewChatViewModel", "Error loading users", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load users",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun createDummyUsersIfNeeded() {
        try {
            val existingUsers = chatUserRepository.getAllUsers().first()
            if (existingUsers.isEmpty() || existingUsers.size < 5) {
                // Add more dummy users for testing

                val dummyUsers = ChatTestDataHelper.getDummyUsers()


                dummyUsers.forEach { user ->
                    try {
                        chatUserRepository.saveUser(user)
                    } catch (e: Exception) {
                        Log.e("NewChatViewModel", "Error saving user ${user.userId}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NewChatViewModel", "Error creating dummy users", e)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredUsers(): List<ChatUser> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.users
        } else {
            _uiState.value.users.filter { user ->
                user.displayName.lowercase().contains(query) ||
                        user.username.lowercase().contains(query)
            }
        }
    }

    fun startChatWithUser(user: ChatUser) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingChat = true)

            try {
                // Check if private chat already exists with this user
                val existingChat = chatRepository.findPrivateChatBetweenUsers(
                    currentUserId,
                    user.userId
                )

                if (existingChat != null) {
                    // Chat already exists, navigate to it
                    Log.d("NewChatViewModel", "Found existing chat: ${existingChat.chatId}")
                    _uiState.value = _uiState.value.copy(
                        navigateToChatId = existingChat.chatId,
                        isCreatingChat = false
                    )
                } else {
                    // Create new private chat
                    Log.d("NewChatViewModel", "Creating new chat with user: ${user.displayName}")
                    val chatId = "chat_${UUID.randomUUID()}"
                    val newChat = Chat(
                        chatId = chatId,
                        chatName = user.displayName,
                        chatType = "PRIVATE",
                        chatImageUrl = user.profilePictureUrl,
                        createdAt = System.currentTimeMillis()
                    )

                    chatRepository.createChat(
                        chat = newChat,
                        participantIds = listOf(currentUserId, user.userId)
                    )

                    _uiState.value = _uiState.value.copy(
                        navigateToChatId = chatId,
                        isCreatingChat = false
                    )
                }
            } catch (e: Exception) {
                Log.e("NewChatViewModel", "Error creating/finding chat", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start chat",
                    isCreatingChat = false
                )
            }
        }
    }

    fun clearNavigationState() {
        _uiState.value = _uiState.value.copy(navigateToChatId = null)
    }

    private suspend fun findExistingPrivateChat(otherUserId: String): Chat? {
        // This is a simplified check - in a real app, you'd query the database
        // to find if a private chat exists between current user and the other user
        return null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}