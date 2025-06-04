package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CourseResponse(
    val courses: List<Course>,
    val message: String,
    val status: Boolean,
    val status_code: Int
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val course_name: String,
    val course_description: String?,
    val image: String?,
    val course_duration: String?,
    val course_expiry_date: String?,
    val course_price: String,
    val course_discounted_price: String,
    val offer_code: String?,
    val course_like: Int,
    val course_tags: String?,
    val instructor: String?,

    val category_id: Int?,
    val category_name: String?,
    val sub_category_id: Int?,
    val sub_category_name: String?,
    val subject_id: Int?,
    val subject_name: String?,

    val isFavorite: Boolean,
    val isPurchased: Boolean,
    val isWishlisted: Boolean,
    val is_liked: Boolean,

    val is_active: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val added_by: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
)