package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.response.NotificationsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): NotificationsResponse = withContext(Dispatchers.IO) {
        val response = apiService.getNotifications()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get notifications: ${response.message()}")
        }
    }


}