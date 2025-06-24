package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CategoryResponse(
    val categories: List<CategoryItem>,
    val message: String,
    val status: Boolean,
    val status_code: Int
)


@Entity(tableName = "categories")
data class CategoryItem(
    @PrimaryKey val id: Int,
    val name: String,
    val is_active: String,

//    @Ignore
    var iconName: String? = null,
    var iconColor: Int? = null,
    var backgroundColor: Int? = null,
    var courseCount: Int? = null,

    val added_by: String?,
    val created_at: String?,
    val deleted_at: String?,
    val updated_at: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
)




