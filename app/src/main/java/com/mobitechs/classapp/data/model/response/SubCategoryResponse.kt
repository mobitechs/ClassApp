package com.mobitechs.classapp.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

data class SubCategoryResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val subCategories: List<SubCategoryItem>
)


@Entity(tableName = "subCategories")
data class SubCategoryItem(
    @PrimaryKey
    val id: Int,
    val name: String,
    val added_by: String?,
    val category_id: Int,
    val category_name: String,
    val is_active: String,
//    val category: CategoryItem,
    val created_at: String?,
    val deleted_at: String?,
    val updated_at: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
