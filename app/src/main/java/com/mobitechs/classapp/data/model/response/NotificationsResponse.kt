package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class NotificationsResponse(
    val message: String,
    val notifications: List<NotificationItem>,
    val status: Boolean,
    val status_code: Int
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: Int,
    val added_by: String?,
//    val course: Course,
    val course_id: Int,
    val created_at: String?,
    val deleted_at: String?,
    val description: String,

    val is_active: String,
    val is_course: String?,
    val notice_title: String,
    val offer_code: String?,
    val updated_at: String?,
    val url: String,
    val lastSyncedAt: Long = System.currentTimeMillis()
)