package com.mobitechs.classapp.data.repository.chat


import com.mobitechs.classapp.data.model.dao.chat.MessageDao
import com.mobitechs.classapp.data.model.response.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(
    private val messageDao: MessageDao
) {
    fun getChatMessages(chatId: String): Flow<List<ChatMessage>> {
        return messageDao.getChatMessages(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun sendMessage(message: ChatMessage) {
        messageDao.insertMessage(message.toEntity())
    }

    suspend fun markMessagesAsRead(chatId: String, currentUserId: String) {
        messageDao.markMessagesAsRead(chatId, currentUserId)
    }

    suspend fun clearChatMessages(chatId: String) {
        messageDao.deleteAllChatMessages(chatId)
    }
}

// Extension functions for mapping
private fun ChatMessage.toDomain() = ChatMessage(
    messageId = messageId,
    chatId = chatId,
    senderId = senderId,
    content = content,
    timestamp = timestamp,
    isRead = isRead,
    messageType = messageType,
    mediaUrl = mediaUrl
)

private fun ChatMessage.toEntity() = ChatMessage(
    messageId = messageId,
    chatId = chatId,
    senderId = senderId,
    content = content,
    timestamp = timestamp,
    isRead = isRead,
    messageType = messageType,
    mediaUrl = mediaUrl
)