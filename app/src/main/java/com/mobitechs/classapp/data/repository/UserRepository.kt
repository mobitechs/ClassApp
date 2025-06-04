package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    suspend fun getUserProfile(): LoginResponse = withContext(Dispatchers.IO) {
        val response = apiService.getUserProfile(sharedPrefsManager.getUser()?.id.toString())
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get user profile: ${response.message()}")
        }
    }

    suspend fun updateUserProfile(user: Student): Student = withContext(Dispatchers.IO) {
        val response = apiService.updateUserProfile(user)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to update user profile: ${response.message()}")
        }
    }



}

