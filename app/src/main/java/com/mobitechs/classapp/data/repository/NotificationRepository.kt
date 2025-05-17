package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): List<NotificationItem> = withContext(Dispatchers.IO) {
        val response = apiService.getNotifications()
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get notifications: ${response.message()}")
        }
    }

    suspend fun getUnreadNotificationsCount(): Int = withContext(Dispatchers.IO) {
        try {
            val notifications = getNotifications()
            return@withContext notifications.count { !it.isRead }
        } catch (e: Exception) {
            return@withContext 0
        }
    }
}