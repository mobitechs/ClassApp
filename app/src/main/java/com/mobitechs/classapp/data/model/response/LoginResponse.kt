package com.mobitechs.classapp.data.model.response



data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long
)


data class LoginRequest(
    val email: String,
    val password: String
)
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val password_confirmation: String,
    val gender: String,
    val city: String,
    val pincode: String
)

data class LoginResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val student: Student,
    val token: String
)

data class Student(
    val added_by: Any,
    val address: Any,
    val adharImage: Any,
    val adharNo: String,
    val bloodGroup: Any,
    val city: String,
    val created_at: String,
    val deleted_at: Any,
    val email: String,
    val gender: String,
    val id: Int,
    val is_active: Int,
    val name: String,
    val panImage: Any,
    val panNo: Any,
    val password: String,
    val phone: String,
    val photo: Any,
    val pincode: String,
    val signature: Any,
    val updated_at: String
)