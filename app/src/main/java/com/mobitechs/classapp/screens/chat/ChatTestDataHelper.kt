package com.mobitechs.classapp.screens.chat

import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatMessage
import com.mobitechs.classapp.data.model.response.ChatParticipantEntity
import com.mobitechs.classapp.data.model.response.ChatUser
import java.util.UUID
import kotlin.random.Random

object ChatTestDataHelper {

    // Create dummy users
    fun createDummyUsers(): List<ChatUser> {
        return listOf(
            ChatUser(
                userId = "5", // Current user
                username = "current_user",
                displayName = "You",
                profilePictureUrl = "https://i.pravatar.cc/150?img=1",
                isOnline = true
            ),
            ChatUser(
                userId = "user_john",
                username = "john_doe",
                displayName = "John Doe",
                profilePictureUrl = "https://i.pravatar.cc/150?img=2",
                isOnline = true,
                lastSeen = System.currentTimeMillis() - 5 * 60 * 1000 // 5 minutes ago
            ),
            ChatUser(
                userId = "user_jane",
                username = "jane_smith",
                displayName = "Jane Smith",
                profilePictureUrl = "https://i.pravatar.cc/150?img=3",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2 hours ago
            ),
            ChatUser(
                userId = "user_mike",
                username = "mike_wilson",
                displayName = "Mike Wilson",
                profilePictureUrl = "https://i.pravatar.cc/150?img=4",
                isOnline = true
            ),
            ChatUser(
                userId = "user_sarah",
                username = "sarah_jones",
                displayName = "Sarah Jones",
                profilePictureUrl = "https://i.pravatar.cc/150?img=5",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 1 day ago
            )
        )
    }

    // Create dummy chats
    fun createDummyChats(): List<Chat> {
        val now = System.currentTimeMillis()
        return listOf(
            Chat(
                chatId = "chat_1",
                chatName = "John Doe",
                chatType = "PRIVATE",
                lastMessage = "Hey, how are you doing?",
                lastMessageTimestamp = now - 5 * 60 * 1000, // 5 minutes ago
                chatImageUrl = "https://i.pravatar.cc/150?img=2"
            ),
            Chat(
                chatId = "chat_2",
                chatName = "Project Team",
                chatType = "GROUP",
                lastMessage = "Mike: Great work on the presentation!",
                lastMessageTimestamp = now - 30 * 60 * 1000, // 30 minutes ago
                chatImageUrl = "https://i.pravatar.cc/150?img=10"
            ),
            Chat(
                chatId = "chat_3",
                chatName = "Jane Smith",
                chatType = "PRIVATE",
                lastMessage = "See you tomorrow at the meeting",
                lastMessageTimestamp = now - 2 * 60 * 60 * 1000, // 2 hours ago
                chatImageUrl = "https://i.pravatar.cc/150?img=3"
            ),
            Chat(
                chatId = "chat_4",
                chatName = "Family Group",
                chatType = "GROUP",
                lastMessage = "Mom: Dinner at 7pm today",
                lastMessageTimestamp = now - 4 * 60 * 60 * 1000, // 4 hours ago
                chatImageUrl = "https://i.pravatar.cc/150?img=11"
            ),
            Chat(
                chatId = "chat_5",
                chatName = "Mike Wilson",
                chatType = "PRIVATE",
                lastMessage = "Thanks for the help!",
                lastMessageTimestamp = now - 24 * 60 * 60 * 1000, // 1 day ago
                chatImageUrl = "https://i.pravatar.cc/150?img=4"
            )
        )
    }

    // Create dummy messages for a chat
    fun createDummyMessages(chatId: String, currentUserId: String = "user123"): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val now = System.currentTimeMillis()

        when (chatId) {
            "chat_1" -> {
                // Private chat with John
                messages.addAll(listOf(
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_john",
                        content = "Photo",
                        timestamp = now - 3 * 60 * 1000,
                        isRead = true,
                        messageType = "IMAGE",
                        mediaUrl = "https://picsum.photos/300/200" // Sample image URL
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUserId,
                        content = "I'm good! Just finished work. How about you?",
                        timestamp = now - 4 * 60 * 1000,
                        isRead = true
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_john",
                        content = "Same here. Want to grab coffee tomorrow?",
                        timestamp = now - 3 * 60 * 1000,
                        isRead = true
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUserId,
                        content = "Sure! What time works for you?",
                        timestamp = now - 2 * 60 * 1000,
                        isRead = true
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_john",
                        content = "How about 10 AM at the usual place?",
                        timestamp = now - 1 * 60 * 1000,
                        isRead = false
                    )
                ))
            }
            "chat_2" -> {
                // Group chat - Project Team
                messages.addAll(listOf(
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUserId,
                        content = "Hi team, I've uploaded the presentation draft",
                        timestamp = now - 2 * 60 * 60 * 1000
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_jane",
                        content = "Thanks! I'll review it now",
                        timestamp = now - 90 * 60 * 1000
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_mike",
                        content = "Great work on the presentation!",
                        timestamp = now - 30 * 60 * 1000
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = "user_jane",
                        content = "I agree, looks really good 👍",
                        timestamp = now - 25 * 60 * 1000
                    ),
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUserId,
                        content = "Thanks everyone! Let's meet tomorrow to finalize",
                        timestamp = now - 20 * 60 * 1000
                    )
                ))
            }
            else -> {
                // Generic messages for other chats
                messages.add(
                    ChatMessage(
                        messageId = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = if (Random.nextBoolean()) currentUserId else "user_${Random.nextInt(1, 5)}",
                        content = "This is a test message",
                        timestamp = now - Random.nextLong(1, 60) * 60 * 1000
                    )
                )
            }
        }

        return messages.sortedByDescending { it.timestamp }
    }

    // Create chat participants
    fun createChatParticipants(chatId: String, currentUserId: String = "user123"): List<ChatParticipantEntity> {
        return when (chatId) {
            "chat_1" -> listOf(
                ChatParticipantEntity(chatId, currentUserId),
                ChatParticipantEntity(chatId, "user_john")
            )
            "chat_2" -> listOf(
                ChatParticipantEntity(chatId, currentUserId, isAdmin = true),
                ChatParticipantEntity(chatId, "user_jane"),
                ChatParticipantEntity(chatId, "user_mike"),
                ChatParticipantEntity(chatId, "user_sarah")
            )
            "chat_3" -> listOf(
                ChatParticipantEntity(chatId, currentUserId),
                ChatParticipantEntity(chatId, "user_jane")
            )
            "chat_4" -> listOf(
                ChatParticipantEntity(chatId, currentUserId),
                ChatParticipantEntity(chatId, "user_john"),
                ChatParticipantEntity(chatId, "user_jane"),
                ChatParticipantEntity(chatId, "user_mike")
            )
            "chat_5" -> listOf(
                ChatParticipantEntity(chatId, currentUserId),
                ChatParticipantEntity(chatId, "user_mike")
            )
            else -> listOf(
                ChatParticipantEntity(chatId, currentUserId)
            )
        }
    }
}