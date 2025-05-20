package com.mobitechs.classapp.data.model.response

data class NotificationsResponse(
    val message: String,
    val notifications: List<NotificationItem>,
    val status: Boolean,
    val status_code: Int
)

data class NotificationItem(
    val added_by: Any,
    val course: Course,
    val course_id: Int,
    val created_at: String,
    val deleted_at: Any,
    val description: String,
    val id: Int,
    val is_active: String,
    val is_course: String,
    val notice_title: String,
    val offer_code: String,
    val updated_at: String,
    val url: String
)