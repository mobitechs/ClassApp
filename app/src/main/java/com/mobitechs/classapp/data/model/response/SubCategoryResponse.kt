package com.mobitechs.classapp.data.model.response

data class SubCategoryResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val subCategories: List<SubCategoryItem>
)



data class SubCategoryItem(
    val added_by: Any,
    val category: CategoryItem,
    val category_id: Int,
    val created_at: String,
    val deleted_at: Any,
    val id: Int,
    val is_active: String,
    val name: String,
    val updated_at: String
)
