package com.mobitechs.classapp.data.model.request



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


data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)