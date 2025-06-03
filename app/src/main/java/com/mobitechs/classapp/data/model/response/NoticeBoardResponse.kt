package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class NoticeBoardResponse(
    val message: String,
    val notice: List<Notice>,
    val status: Boolean,
    val status_code: Int
)

@Entity(tableName = "noticeBoard")
data class Notice(
    @PrimaryKey val id: Int,
    val notice_title: String,
    val description: String,
    val offer_code: String,
    val url: String,
    val is_course: String,
    val course_id: Int,
//    val course: Course,

    val is_active: String,
    val created_at: String?,
    val deleted_at: String?,
    val updated_at: String?,
    val added_by: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
)