package com.mobitechs.classapp.data.model

import java.util.Date

data class Batch(
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val coverImage: String? = null,
    val totalStudents: Int,
    val createdAt: Date
)