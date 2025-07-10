package com.mobitechs.classapp.screens.store

import androidx.lifecycle.viewModelScope
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
    val isProcessingPayment: Boolean = false
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