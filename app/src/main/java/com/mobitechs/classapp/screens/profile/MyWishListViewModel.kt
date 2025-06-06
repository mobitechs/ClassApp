package com.mobitechs.classapp.screens.profile


import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.screens.store.CourseActionsViewModel
import com.mobitechs.classapp.screens.store.updateCourse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishListUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val filteredCourses: List<Course> = emptyList(),
    val error: String = "",
    val searchQuery: String = ""
)

class MyWishListViewModel(
    override val courseRepository: CourseRepository
) : CourseActionsViewModel() {

    private val _uiState = MutableStateFlow(WishListUiState())
    val uiState: StateFlow<WishListUiState> = _uiState.asStateFlow()

    init {
        loadWishListCourses()
    }

    fun loadWishListCourses() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                // Use the dedicated favourite courses endpoint
                val courses = courseRepository.getAllWishlistCourses()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        courses = courses.courses,
                        filteredCourses = courses.courses
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load courses"
                    )
                }
            }
        }
    }

    fun searchCourses(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isEmpty()) {
            _uiState.update { it.copy(filteredCourses = it.courses) }
            return
        }

        val lowercaseQuery = query.lowercase()
        val filtered = _uiState.value.courses.filter { course ->
            // Search in course name
            course.course_name.lowercase().contains(lowercaseQuery) ||
                    // Search in category name
                    course.category_name?.lowercase()?.contains(lowercaseQuery) == true ||
                    // Search in subcategory name
                    course.sub_category_name?.lowercase()?.contains(lowercaseQuery) == true ||
                    // Search in subject name
                    course.subject_name?.lowercase()?.contains(lowercaseQuery) == true ||
                    // Search in course tags
                    course.course_tags?.lowercase()?.contains(lowercaseQuery) == true ||
                    // Search in course description
                    course.course_description?.lowercase()?.contains(lowercaseQuery) == true
        }

        _uiState.update { it.copy(filteredCourses = filtered) }
    }


    override fun updateCourseInState(
        courseId: Int,
        transform: (Course) -> Course
    ) {
        _uiState.update { state ->
            state.copy(
                // Update popular courses list
                courses = state.courses.updateCourse(courseId, transform),
                filteredCourses = state.filteredCourses.updateCourse(courseId, transform),

                )
        }
    }
}