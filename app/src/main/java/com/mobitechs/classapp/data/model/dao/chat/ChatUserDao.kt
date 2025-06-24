package com.mobitechs.classapp.data.model.dao.chat

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobitechs.classapp.data.model.response.ChatUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): ChatUser?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<ChatUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ChatUser)

    @Update
    suspend fun updateUser(user: ChatUser)

    @Delete
    suspend fun deleteUser(user: ChatUser)
}