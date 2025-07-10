package com.mobitechs.classapp.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.Payment
import com.mobitechs.classapp.data.model.response.PaymentListResponse
import com.mobitechs.classapp.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val course: Course? = null,
    val courseContent: List<Content> = emptyList(),
    val isLoading: Boolean = false,
    val isContentLoading: Boolean = false,
    val error: String = "",
    val isProcessingPayment: Boolean = false,
    // Fixed: Changed to List<Payment> instead of PaymentListResponse?
    val paymentListResponse: List<Payment> = emptyList(),
    val isPaymentHistoryLoading: Boolean = false,
    val paymentHistoryError: String = ""
)

class PaymentViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    // Fixed: Added the callback version that UI expects
    fun getAllPaymentList(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPaymentHistoryLoading = true,
                    paymentHistoryError = ""
                )
            }

            try {
                val result = paymentRepository.getAllPaymentList()

                _uiState.update { currentState ->
                    currentState.copy(
                        paymentListResponse = result.payments, // Extract payments list
                        isPaymentHistoryLoading = false,
                        paymentHistoryError = ""
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = "Failed to load payment history: ${e.message}"
                _uiState.update {
                    it.copy(
                        paymentHistoryError = errorMessage,
                        isPaymentHistoryLoading = false
                    )
                }
                onError(errorMessage)
            }
        }
    }

    // Keep the version without callbacks as well
    fun getAllPaymentList() {
        getAllPaymentList(
            onSuccess = {},
            onError = {}
        )
    }

    fun updatePurchaseStatus(
        courseId: String,
        paymentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = paymentRepository.updatePurchaseStatus(paymentId, courseId)

                _uiState.update { currentState ->
                    val updatedCourse = currentState.course?.copy(is_purchased = true)
                    currentState.copy(
                        course = updatedCourse,
                        isProcessingPayment = false
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to update purchase: ${e.message}",
                        isProcessingPayment = false
                    )
                }
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                error = "",
                paymentHistoryError = ""
            )
        }
    }

    fun retryPaymentHistory() {
        getAllPaymentList()
    }
}