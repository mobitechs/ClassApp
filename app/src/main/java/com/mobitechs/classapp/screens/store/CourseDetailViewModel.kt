package com.mobitechs.classapp.screens.store


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.PaymentResponse
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val course: Course? = null,
    val isProcessingPayment: Boolean = false,
    val paymentData: PaymentResponse? = null,
    val isCoursePreloaded: Boolean = false // ✅ NEW
)

class CourseDetailViewModel(
    private val courseRepository: CourseRepository,
    private val paymentRepository: PaymentRepository,
    private val savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState(isLoading = true))
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private val courseId: String = savedStateHandle?.get<String>("courseId") ?: ""


    init {
        if (courseId.isNotEmpty()) {
            loadCourseDetails()
        }

    }

    fun setCourseId(id: String) {
        if (id.isNotEmpty() && id != courseId) {
            loadCourseDetails(id)
        }
    }

    fun setCourse(course: Course) {
        _uiState.update {
            it.copy(course = course, isLoading = false, isCoursePreloaded = true)
        }
    }

    private fun loadCourseDetails(id: String = courseId) {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val course = courseRepository.getCourseDetails(id)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        course = course
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

//    fun toggleFavorite() {
//        uiState.value.course?.id?.let { courseId ->
//            viewModelScope.launch {
//                try {
//                    val result = courseRepository.toggleFavorite(courseId.toString())
//
//                    // Update the course in the state
//                    _uiState.update { state ->
//                        val updatedCourse = state.course?.copy(isFavorite = result)
//                        state.copy(course = updatedCourse)
//                    }
//                } catch (e: Exception) {
//                    // Handle error silently
//                }
//            }
//        }
//    }

//    fun toggleWishlist() {
//        uiState.value.course?.id?.let { courseId ->
//            viewModelScope.launch {
//                try {
//                    val result = courseRepository.toggleWishlist(courseId.toString())
//
//                    // Update the course in the state
//                    _uiState.update { state ->
//                        val updatedCourse = state.course?.copy(isWishlisted = result)
//                        state.copy(course = updatedCourse)
//                    }
//                } catch (e: Exception) {
//                    // Handle error silently
//                }
//            }
//        }
//    }

    fun initiatePayment() {
        uiState.value.course?.id?.let { courseId ->
            _uiState.update { it.copy(isProcessingPayment = true, error = "") }

            viewModelScope.launch {
                try {
                    val paymentResponse = paymentRepository.initiatePayment(courseId.toString())

                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            paymentData = paymentResponse
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            error = e.message ?: "Failed to initiate payment"
                        )
                    }
                }
            }
        }
    }

    fun updatePurchaseStatus(
        courseId: String,
        paymentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Make API call to your backend
                // val result = apiService.updatePurchaseStatus(courseId, paymentId)

                // Update local state
                _uiState.update { currentState ->
                    val updatedCourse = currentState.course?.copy(isPurchased = true)
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

    fun verifyPayment(paymentId: String, orderId: String, signature: String) {
        _uiState.update { it.copy(isProcessingPayment = true, error = "") }

        viewModelScope.launch {
            try {
                val result = paymentRepository.verifyPayment(paymentId, orderId, signature)

                if (result.status == "success") {
                    // Update the course as purchased
                    loadCourseDetails()
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            error = result.message ?: "Payment verification failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingPayment = false,
                        error = e.message ?: "Failed to verify payment"
                    )
                }
            }
        }
    }
}