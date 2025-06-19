package com.mobitechs.classapp.data.model.dao.chat

import androidx.room.*
import com.mobitechs.classapp.data.model.response.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // Order by timestamp ASC so newest messages appear at bottom
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getChatMessages(chatId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): ChatMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND senderId != :currentUserId AND isRead = 0")
    suspend fun markMessagesAsRead(chatId: String, currentUserId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND senderId != :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(chatId: String, userId: String): Int


    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteAllChatMessages(chatId: String)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)


}