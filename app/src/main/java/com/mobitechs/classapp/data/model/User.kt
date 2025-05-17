package com.mobitechs.classapp.data.model

import java.util.Date

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val gender: String,
    val profileImage: String? = null,
    val createdAt: Date
)