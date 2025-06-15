package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

data class CourseContentResponse(
    val content: List<Content>,
    val message: String,
    val status: Boolean,
    val status_code: Int
)


data class Content(
    val id: Int,
    val content_type: String,
    val content_url: String,
    val course_id: Int,
    var is_free: String?,
    var is_offline_available: String?,
    var course: Course?,
    var is_active: String,
    var offer_code: String?,
    var added_by: String?,
    var updated_at: String?,
    var created_at: String?,
    var deleted_at: String?
)


@Entity(tableName = "downloaded_content")
data class DownloadContent(
    @PrimaryKey
    val id: Int,
    val content_type: String,
    val content_url: String,
    val course_id: Int,
    var course_name: String?,
    var course_description: String?,
    var category_name: String?,
    var sub_category_name: String?,
    var subject_name: String?,
    var course_tags: String?,
    var is_free: String?,
    var is_offline_available: String?,
    var downloadedFilePath: String?,
    var isDownloaded: Boolean = false,
    var downloadedAt: Long = System.currentTimeMillis(),

)