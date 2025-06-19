package com.mobitechs.classapp.data.repository.chat

import com.mobitechs.classapp.data.model.dao.chat.ChatUserDao
import com.mobitechs.classapp.data.model.response.ChatUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatUserRepository(
    private val userDao: ChatUserDao
) {
    suspend fun getUser(userId: String): ChatUser? {
        return userDao.getUserById(userId)?.toDomain()
    }

    fun getAllUsers(): Flow<List<ChatUser>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveUser(user: ChatUser) {
        userDao.insertUser(user.toEntity())
    }

    suspend fun updateUser(user: ChatUser) {
        userDao.updateUser(user.toEntity())
    }
}

// Extension functions for mapping
private fun ChatUser.toDomain() = ChatUser(
    userId = userId,
    username = username,
    displayName = displayName,
    profilePictureUrl = profilePictureUrl,
    lastSeen = lastSeen,
    isOnline = isOnline
)

private fun ChatUser.toEntity() = ChatUser(
    userId = userId,
    username = username,
    displayName = displayName,
    profilePictureUrl = profilePictureUrl,
    lastSeen = lastSeen,
    isOnline = isOnline
)