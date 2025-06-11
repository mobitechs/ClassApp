package com.mobitechs.classapp.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.PaymentData
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val courseContent: List<Content> = emptyList(),
    val isLoading: Boolean = false,
    val isContentLoading: Boolean = false,
    val error: String = "",
    val isProcessingPayment: Boolean = false,
    val paymentData: PaymentData? = null
)



class CourseDetailViewModel(
    override val courseRepository: CourseRepository,
    private val paymentRepository: PaymentRepository
) : CourseActionsViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    fun setCourse(course: Course) {
        _uiState.update { it.copy(course = course) }
    }

    fun loadCourseDetails(courseId: Int) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val course = courseRepository.getCourseDetails(courseId)
                _uiState.update {
                    it.copy(
                        course = course,
                        isLoading = false,
                        error = ""
                    )
                }
                // Load content after loading course details
                loadCourseContent(courseId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load course details"
                    )
                }
            }
        }
    }

    fun loadCourseContent(courseId: Int) {
        _uiState.update { it.copy(isContentLoading = true) }

        viewModelScope.launch {
            try {
                val content = courseRepository.getCourseContent(courseId)
                _uiState.update {
                    it.copy(
                        courseContent = content.content,
                        isContentLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isContentLoading = false,
                        error = e.message ?: "Failed to load course content"
                    )
                }
            }
        }
    }

    fun initiatePayment() {
        val course = uiState.value.course ?: return

        _uiState.update { it.copy(isProcessingPayment = true) }

        viewModelScope.launch {
            try {
                val paymentData = paymentRepository.initiatePayment(course.id.toString())
//                _uiState.update {
//                    it.copy(
//                        paymentData = paymentData,
//                        isProcessingPayment = false
//                    )
//                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingPayment = false,
                        error = e.message ?: "Payment initialization failed"
                    )
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
                    //loadCourseDetails()
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

    override fun updateCourseInState(
        courseId: Int,
        transform: (Course) -> Course
    ) {
        _uiState.update { state ->
            state.copy(
                // Update popular courses list
                course = state.course?.updateSingleCourse(courseId, transform)

            )
        }
    }
}