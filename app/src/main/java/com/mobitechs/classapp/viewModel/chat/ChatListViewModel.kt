package com.mobitechs.classapp.viewModel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
import com.mobitechs.classapp.screens.chat.ChatTestDataHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val chatToDelete: Chat? = null,
    val searchQuery: String = "",
)

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val chatUserRepository: ChatUserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val currentUserId: String by lazy {
        authRepository.getCurrentUserId() ?: "5" // Default for testing
    }

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Always create/update dummy data for now
                setupDummyData()

                // Then load chats
                loadChats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to initialize: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredChats(): List<Chat> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.chats
        } else {
            _uiState.value.chats.filter { chat ->
                chat.chatName?.lowercase()?.contains(query) == true ||
                        chat.lastMessage?.lowercase()?.contains(query) == true
            }
        }
    }

    private suspend fun setupDummyData() {
        try {
            // Check if data already exists
            val existingChats = chatRepository.getUserChats(currentUserId).first()
            if (existingChats.isNotEmpty()) {
                Log.d("ChatListViewModel", "Data already exists")
                return
            }

            Log.d("ChatListViewModel", "Setting up dummy data...")

            // Insert dummy users
            val users = ChatTestDataHelper.createDummyUsers()
            users.forEach { user ->
                chatUserRepository.saveUser(user)
            }

            // Insert dummy chats with participants
            val chats = ChatTestDataHelper.createDummyChats()
            chats.forEach { chat ->
                val participants =
                    ChatTestDataHelper.createChatParticipants(chat.chatId, currentUserId)
                chatRepository.createChat(chat, participants.map { it.userId })
            }

            Log.d("ChatListViewModel", "Dummy data setup completed")

        } catch (e: Exception) {
            Log.e("ChatListViewModel", "Error setting up dummy data", e)
            throw e
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            chatRepository.getUserChats(currentUserId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
                .collect { chats ->
                    _uiState.value = _uiState.value.copy(
                        chats = chats,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadChats()
        }
    }

    // TODO: Replace with API call when ready
    fun loadChatsFromAPI() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Future API implementation
                // val response = chatApi.getUserChats(currentUserId)
                // _uiState.value = _uiState.value.copy(
                //     chats = response.chats,
                //     isLoading = false
                // )

                // For now, just load from local
                loadChats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun showDeleteDialog(chat: Chat) {
        _uiState.update { it.copy(showDeleteDialog = true, chatToDelete = chat) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, chatToDelete = null) }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }

                // Delete the chat (messages and participants will be deleted by cascade)
                chatRepository.deleteChat(chat.chatId)

                Log.d("ChatListViewModel", "Chat ${chat.chatId} deleted successfully")

                _uiState.update { it.copy(isLoading = false, chatToDelete = null) }

            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Error deleting chat", e)
                _uiState.update {
                    it.copy(
                        error = "Failed to delete chat: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}


//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.mobitechs.classapp.data.model.response.Chat
//import com.mobitechs.classapp.data.repository.AuthRepository
//import com.mobitechs.classapp.data.repository.chat.ChatRepository
//import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
//import com.mobitechs.classapp.screens.chat.ChatTestDataHelper
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//
//
//data class ChatListUiState(
//    val chats: List<Chat> = emptyList(),
//    val isLoading: Boolean = false,
//    val error: String? = null
//)
//class ChatListViewModel(
//    private val chatRepository: ChatRepository,
//    private val chatUserRepository: ChatUserRepository,
//    private val authRepository: AuthRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ChatListUiState())
//    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
//
//    private val currentUserId: String by lazy {
//        authRepository.getCurrentUserId() ?: "user123"
//    }
//
//    init {
//        if (currentUserId.isNotEmpty()) {
//            initializeData()
//        } else {
//            _uiState.value = _uiState.value.copy(
//                error = "User not logged in"
//            )
//        }
//    }
//
//    private fun initializeData() {
//        viewModelScope.launch {
//            createDummyDataIfNeeded()
//            loadChats()
//        }
//    }
//
//    private suspend fun createDummyDataIfNeeded() {
//        try {
//            // Check if chats already exist
//            val existingChats = chatRepository.getUserChats(currentUserId).first()
//            if (existingChats.isNotEmpty()) {
//                return
//            }
//
//            // Create dummy users
//            val users = ChatTestDataHelper.createDummyUsers()
//            users.forEach { user ->
//                chatUserRepository.saveUser(user)
//            }
//
//            // Create dummy chats (without messages for now)
//            val chats = ChatTestDataHelper.createDummyChats()
//            chats.forEach { chat ->
//                val participants = ChatTestDataHelper.createChatParticipants(chat.chatId, currentUserId)
//                chatRepository.createChat(chat, participants.map { it.userId })
//            }
//
//        } catch (e: Exception) {
//            Log.e("ChatListViewModel", "Error creating dummy data", e)
//        }
//    }
//
//    private fun loadChats() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            chatRepository.getUserChats(currentUserId)
//                .catch { e ->
//                    _uiState.value = _uiState.value.copy(
//                        error = e.message,
//                        isLoading = false
//                    )
//                }
//                .collect { chats ->
//                    _uiState.value = _uiState.value.copy(
//                        chats = chats,
//                        isLoading = false
//                    )
//                }
//        }
//    }
//}
