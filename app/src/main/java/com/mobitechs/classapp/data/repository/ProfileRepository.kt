package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    // Get user from SharedPrefsManager (session)
    fun getUserFromSession(): Student? {
        return sharedPrefsManager.getUser()
    }

    suspend fun getUserProfile(): Student = withContext(Dispatchers.IO) {
        val userId = sharedPrefsManager.getUser()?.id ?: throw Exception("User not logged in")
        val response = apiService.getUserProfile(userId.toString())
        if (response.isSuccessful) {
            response.body()?.student ?: throw Exception("No student data found")
        } else {
            throw Exception("Failed to get profile: ${response.message()}")
        }
    }

    suspend fun updateProfile(student: Student): Student = withContext(Dispatchers.IO) {
        val response = apiService.updateUserProfile(student)
        if (response.isSuccessful) {
            response.body()?.let { updatedStudent ->
                // Update local storage
                sharedPrefsManager.saveUser(updatedStudent)
                updatedStudent
            } ?: throw Exception("No data in response")
        } else {
            throw Exception("Failed to update profile: ${response.message()}")
        }
    }
}