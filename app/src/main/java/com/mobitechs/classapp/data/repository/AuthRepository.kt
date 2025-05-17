package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.OtpVerificationRequest
import com.mobitechs.classapp.data.model.OtpVerificationResponse
import com.mobitechs.classapp.data.model.User
import com.mobitechs.classapp.data.model.response.LoginRequest
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {
    suspend fun login(request: LoginRequest): LoginResponse = withContext(Dispatchers.IO) {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Login failed: ${response.message()}")
        }
    }

    suspend fun register(request: RegisterRequest): LoginResponse = withContext(Dispatchers.IO) {
        val response = apiService.register(request)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Registration failed: ${response.message()}")
        }
    }

    suspend fun verifyOtp(request: OtpVerificationRequest): OtpVerificationResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.verifyOtp(request)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("OTP verification failed: ${response.message()}")
            }
        }

    fun saveAuthData(token: String, user: User) {
        sharedPrefsManager.saveAuthToken(token)
        sharedPrefsManager.saveUser(user)
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefsManager.isLoggedIn()
    }

    fun logout() {
        sharedPrefsManager.clearAll()
    }
}