package com.mobitechs.classapp.data.model

data class CategoryResponse(
    val statusCode: Int = 0,
    val status: String = "",
    val message: String = "",
    val data: List<Category>?
)

data class Category(
    val id: String,
    val name: String,
    val icon: String? = null,
    val parentId: String? = null
)