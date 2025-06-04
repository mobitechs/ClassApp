package com.mobitechs.classapp.data.model.response





data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long
)





data class LoginResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val student: Student,
    val token: String
)

data class Student(
    val id: Int,
    val added_by: String,
    val address: String,
    val adharImage: String,
    val adharNo: String,
    val bloodGroup: String,
    val city: String,
    val created_at: String,
    val deleted_at: String,
    val email: String,
    val gender: String,
    val is_active: Int,
    val name: String,
    val panImage: String,
    val panNo: String,
    val password: String,
    val phone: String,
    val photo: String,
    val pincode: String,
    val signature: String,
    val updated_at: String
)




data class CommonResponse(
    val status: Boolean,
    val status_code: Int,
    val message: String
)