package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class ChatUser(
    @PrimaryKey
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false
)


@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val chatId: String,
    val chatName: String? = null,
    val chatType: String, // "PRIVATE" or "GROUP"
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val chatImageUrl: String? = null
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatUser::class,
            parentColumns = ["userId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatMessage(
    @PrimaryKey
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: String = "TEXT", // TEXT, IMAGE, FILE
    val mediaUrl: String? = null
)

@Entity(
    tableName = "chat_participants",
    primaryKeys = ["chatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatUser::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatParticipantEntity(
    val chatId: String,
    val userId: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false
)