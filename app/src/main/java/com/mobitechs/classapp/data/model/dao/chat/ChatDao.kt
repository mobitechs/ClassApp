package com.mobitechs.classapp.data.model.dao.chat

import androidx.room.*
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("""
        SELECT c.* FROM chats c
        INNER JOIN chat_participants cp ON c.chatId = cp.chatId
        WHERE cp.userId = :userId
        ORDER BY c.lastMessageTimestamp DESC
    """)
    fun getUserChats(userId: String): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): Chat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Update
    suspend fun updateChat(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatParticipant(participant: ChatParticipantEntity)

    @Query("DELETE FROM chat_participants WHERE chatId = :chatId AND userId = :userId")
    suspend fun removeChatParticipant(chatId: String, userId: String)

    @Delete
    suspend fun deleteChat(chat: Chat)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("DELETE FROM chat_participants WHERE chatId = :chatId")
    suspend fun deleteChatParticipants(chatId: String)
}