package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.PaymentResponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val apiService: ApiService
) {
    suspend fun initiatePayment(courseId: String): PaymentResponse = withContext(Dispatchers.IO) {
        val response = apiService.initiatePayment(courseId)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to initiate payment: ${response.message()}")
        }
    }

    suspend fun verifyPayment(
        paymentId: String,
        orderId: String,
        signature: String
    ): PaymentResponse = withContext(Dispatchers.IO) {
        val response = apiService.verifyPayment(paymentId, orderId, signature)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to verify payment: ${response.message()}")
        }
    }
}