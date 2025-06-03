package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class SubjectResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val subjects: List<SubjectItem>
)


@Entity(tableName = "subjects")
data class SubjectItem(
    @PrimaryKey val id: Int,
    val name: String,
    val category_id: Int,
    val category_name: String,
    val subcategory_id: Int,
    val subcategory_name: String,
    val is_active: String,
    val added_by: String?,
    val created_at: String?,
    val deleted_at: String?,
    val updated_at: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

