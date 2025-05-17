package com.mobitechs.classapp.data.model


data class NotificationResponse(
    val statusCode: Int = 0,
    val status: String = "",
    val message: String = "",
    val data: List<Category>?
)

data class NotificationItem(
    val id: String,
    val name: String,
    val isRead: Boolean = false,
    val icon: String? = null,
    val parentId: String? = null
)