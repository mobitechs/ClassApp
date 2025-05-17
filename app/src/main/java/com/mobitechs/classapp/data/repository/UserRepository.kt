package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService
) {

    suspend fun getUserProfile(): User = withContext(Dispatchers.IO) {
        val response = apiService.getUserProfile()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get user profile: ${response.message()}")
        }
    }

    suspend fun updateUserProfile(user: User): User = withContext(Dispatchers.IO) {
        val response = apiService.updateUserProfile(user)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to update user profile: ${response.message()}")
        }
    }
}

