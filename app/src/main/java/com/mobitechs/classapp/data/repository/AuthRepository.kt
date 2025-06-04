package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.request.LoginRequest
import com.mobitechs.classapp.data.model.request.RegisterRequest
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {
    suspend fun login(request: LoginRequest): LoginResponse = withContext(Dispatchers.IO) {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            val loginResponse = response.body() ?: throw Exception("Empty response body")
            // Check if the status code is 200 before saving auth data
            if (loginResponse.status_code == 200) {
                saveAuthData(loginResponse.token, loginResponse.student)
            }
            return@withContext loginResponse
        } else {
            throw Exception("Login failed: ${response.message()}")
        }
    }

    suspend fun register(request: RegisterRequest): LoginResponse = withContext(Dispatchers.IO) {
        val response = apiService.register(request)
        if (response.isSuccessful) {
            val loginResponse = response.body() ?: throw Exception("Empty response body")
            if (loginResponse.status_code == 200) {
                saveAuthData(loginResponse.token, loginResponse.student)
            }
            return@withContext loginResponse
        } else {
            throw Exception("Registration failed: ${response.message()}")
        }
    }


    fun saveAuthData(token: String, user: Student) {
        sharedPrefsManager.saveAuthToken(token)
        sharedPrefsManager.saveUser(user)
    }

    fun getCurrentUser(): Student? {
        return sharedPrefsManager.getUser()
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefsManager.isLoggedIn()
    }

    fun logout() {
        sharedPrefsManager.logout()
    }
}