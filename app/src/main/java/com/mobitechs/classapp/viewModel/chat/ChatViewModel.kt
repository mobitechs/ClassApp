package com.mobitechs.classapp.viewModel.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatMessage
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.MessageRepository
import com.mobitechs.classapp.screens.chat.ChatTestDataHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class ChatUiState(
    val chat: Chat? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false,
    val isSending: Boolean = false,
    val showClearDialog: Boolean = false,
    val showPhotoOptions: Boolean = false,
    val tempPhotoUri: Uri? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var chatId: String = ""
    private var messagesFlow: Flow<List<ChatMessage>>? = null

    val currentUserId: String by lazy {
        authRepository.getCurrentUserId() ?: "5"
    }

    fun initializeChat(chatId: String) {
        if (this.chatId == chatId && _uiState.value.isInitialized) {
            return
        }

        this.chatId = chatId
        _uiState.value = ChatUiState(isInitialized = true, isLoading = true)

        viewModelScope.launch {
            try {
                // Setup dummy messages if needed
                setupDummyMessages()

                // Load chat info
                loadChat()

                // Start observing messages
                observeMessages()

                // Mark messages as read
                markMessagesAsRead()

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to initialize chat", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load chat: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun setupDummyMessages() {
        try {
            val existingMessages = messageRepository.getChatMessages(chatId).first()
            if (existingMessages.isNotEmpty()) {
                Log.d("ChatViewModel", "Messages already exist for chat $chatId")
                return
            }

            val messages = ChatTestDataHelper.createDummyMessages(chatId, currentUserId)
            messages.forEach { message ->
                messageRepository.sendMessage(message)
            }

            Log.d("ChatViewModel", "Created ${messages.size} dummy messages for chat $chatId")

        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error setting up dummy messages", e)
        }
    }

    private fun loadChat() {
        viewModelScope.launch {
            try {
                val chat = chatRepository.getChat(chatId)
                if (chat != null) {
                    _uiState.update { state ->
                        state.copy(chat = chat)
                    }
                    Log.d("ChatViewModel", "Loaded chat: ${chat.chatName}")
                } else {
                    Log.e("ChatViewModel", "Chat not found: $chatId")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading chat", e)
                _uiState.update { state ->
                    state.copy(error = e.message)
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            messageRepository.getChatMessages(chatId)
                .catch { e ->
                    Log.e("ChatViewModel", "Error observing messages", e)
                    _uiState.update { state ->
                        state.copy(error = e.message, isLoading = false)
                    }
                }
                .collect { messages ->
                    Log.d("ChatViewModel", "Received ${messages.size} messages")
                    _uiState.update { state ->
                        state.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            try {
                messageRepository.markMessagesAsRead(chatId, currentUserId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error marking messages as read", e)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || chatId.isEmpty()) {
            Log.w("ChatViewModel", "Cannot send empty message or chatId is empty")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            try {
                val message = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = currentUserId,
                    content = content.trim(),
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    messageType = "TEXT"
                )

                Log.d("ChatViewModel", "Sending message: ${message.content}")

                // Save message to database
                messageRepository.sendMessage(message)

                // Update chat's last message
                _uiState.value.chat?.let { chat ->
                    val updatedChat = chat.copy(
                        lastMessage = content.trim(),
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                    chatRepository.updateChat(updatedChat)

                    // Update local state
                    _uiState.update { state ->
                        state.copy(chat = updatedChat)
                    }
                }

                _uiState.update { it.copy(isSending = false) }
                Log.d("ChatViewModel", "Message sent successfully")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _uiState.update { state ->
                    state.copy(
                        error = "Failed to send message: ${e.message}",
                        isSending = false
                    )
                }
            }
        }
    }

    fun showClearDialog() {
        _uiState.update { it.copy(showClearDialog = true) }
    }

    fun hideClearDialog() {
        _uiState.update { it.copy(showClearDialog = false) }
    }

    fun clearChat() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, showClearDialog = false) }

                // Clear all messages
                messageRepository.clearChatMessages(chatId)

                // Update chat's last message to null
                _uiState.value.chat?.let { chat ->
                    val updatedChat = chat.copy(
                        lastMessage = null,
                        lastMessageTimestamp = null
                    )
                    chatRepository.updateChat(updatedChat)
                    _uiState.update { it.copy(chat = updatedChat) }
                }

                Log.d("ChatViewModel", "Chat cleared successfully")

                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error clearing chat", e)
                _uiState.update {
                    it.copy(
                        error = "Failed to clear chat: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showPhotoOptions() {
        _uiState.update { it.copy(showPhotoOptions = true) }
    }

    fun hidePhotoOptions() {
        _uiState.update { it.copy(showPhotoOptions = false) }
    }


    fun sendPhotoMessage(photoUri: Uri) {
        if (chatId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, showPhotoOptions = false) }

            try {
                // Copy photo to internal storage
                val photoFileName = "photo_${UUID.randomUUID()}.jpg"
                val photoFile = File(context.filesDir, "images/$photoFileName")
                photoFile.parentFile?.mkdirs()

                context.contentResolver.openInputStream(photoUri)?.use { input ->
                    photoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val message = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = currentUserId,
                    content = "Photo",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    messageType = "IMAGE",
                    mediaUrl = photoFile.absolutePath
                )

                Log.d("ChatViewModel", "Sending photo message: ${message.mediaUrl}")

                messageRepository.sendMessage(message)

                // Update chat's last message
                _uiState.value.chat?.let { chat ->
                    val updatedChat = chat.copy(
                        lastMessage = "📷 Photo",
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                    chatRepository.updateChat(updatedChat)
                    _uiState.update { state ->
                        state.copy(chat = updatedChat)
                    }
                }

                _uiState.update { it.copy(isSending = false, tempPhotoUri = null) }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending photo", e)
                _uiState.update { state ->
                    state.copy(
                        error = "Failed to send photo: ${e.message}",
                        isSending = false
                    )
                }
            }
        }
    }

    fun createTempPhotoUri(): Uri {
        // Create a unique filename
        val timestamp = System.currentTimeMillis()
        val imageFileName = "chat_photo_${timestamp}.jpg"

        // Use cache directory for temporary files
        val storageDir = context.cacheDir

        // Create the temp file
        val tempFile = File(storageDir, imageFileName).apply {
            createNewFile()
        }

        // Get URI from FileProvider
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error creating temp photo URI", e)
            // Fallback to external cache if internal fails
            val externalFile = File(context.externalCacheDir, imageFileName).apply {
                createNewFile()
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                externalFile
            )
        }

        _uiState.update { it.copy(tempPhotoUri = uri) }
        return uri
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

