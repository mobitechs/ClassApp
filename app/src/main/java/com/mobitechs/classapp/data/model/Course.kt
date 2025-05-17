package com.mobitechs.classapp.data.model

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val instructor: String,
    val category: Category,
    val subCategory: Category? = null,
    val tags: List<String>? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val rating: Float,
    val totalRatings: Int,
    val totalStudents: Int,
    val isFavorite: Boolean = false,
    val isWishlisted: Boolean = false,
    val isPurchased: Boolean = false,
    val isDownloaded: Boolean = false,
    val previewUrl: String? = null,
)