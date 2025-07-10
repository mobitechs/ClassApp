package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.response.PaymentListResponse
import com.mobitechs.classapp.data.model.response.PaymentStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val apiService: ApiService
) {


    suspend fun updatePurchaseStatus(paymentId: String, courseId: String): PaymentStatusResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.updatePurchaseStatus(paymentId, courseId)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to initiate payment: ${response.message()}")
            }
        }

    suspend fun getAllPaymentList(): PaymentListResponse = withContext(Dispatchers.IO) {
        val response = apiService.getAllPaymentList()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to initiate payment: ${response.message()}")
        }
    }


}