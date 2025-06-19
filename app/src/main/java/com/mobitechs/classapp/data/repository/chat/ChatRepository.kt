package com.mobitechs.classapp.data.repository.chat
import com.mobitechs.classapp.data.model.dao.chat.ChatDao
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatParticipantEntity
import com.mobitechs.classapp.utils.enumClasses.ChatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
class ChatRepository(
    private val chatDao: ChatDao
) {
    fun getUserChats(userId: String): Flow<List<Chat>> {
        return chatDao.getUserChats(userId)
    }

    suspend fun getChat(chatId: String): Chat? {
        return chatDao.getChatById(chatId)
    }

    suspend fun createChat(chat: Chat, participantIds: List<String>) {
        chatDao.insertChat(chat)
        participantIds.forEach { userId ->
            chatDao.insertChatParticipant(
                ChatParticipantEntity(
                    chatId = chat.chatId,
                    userId = userId
                )
            )
        }
    }

    suspend fun findPrivateChatBetweenUsers(userId1: String, userId2: String): Chat? {
        return chatDao.getPrivateChatBetweenUsers(userId1, userId2)
    }

    suspend fun updateChat(chat: Chat) {
        chatDao.updateChat(chat)
    }

    suspend fun deleteChat(chatId: String) {
        // Delete in order: messages -> participants -> chat
        // Messages are deleted automatically due to foreign key CASCADE
        chatDao.deleteChatParticipants(chatId)
        chatDao.deleteChatById(chatId)
    }
}

// Extension functions for mapping
private fun Chat.toDomain() = Chat(
    chatId = chatId,
    chatName = chatName,
    chatType = ChatType.valueOf(chatType).toString(),
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    createdAt = createdAt,
    chatImageUrl = chatImageUrl
)

private fun Chat.toEntity() = Chat(
    chatId = chatId,
    chatName = chatName,
    chatType = ChatType.valueOf(chatType).toString(),
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    createdAt = createdAt,
    chatImageUrl = chatImageUrl
)