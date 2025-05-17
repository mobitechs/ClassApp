package com.mobitechs.classapp.data.model



//data class RegisterRequest(
//    val name: String,
//    val phone: String,
//    val gender: String,
//    val password: String
//)

//data class RegisterResponse(
//    val message: String,
//    val userId: String,
//    val otpSent: Boolean,
//    val expiresIn: Int // OTP expiry time in seconds
//)

data class OtpVerificationRequest(
    val phone: String? = null,
    val email: String? = null,
    val otp: String
)

data class OtpVerificationResponse(
    val message: String,
    val token: String,
    val user: User
)