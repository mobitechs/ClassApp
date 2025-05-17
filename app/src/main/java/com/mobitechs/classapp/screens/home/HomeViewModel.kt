package com.mobitechs.classapp.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.Category
import com.mobitechs.classapp.data.model.Course
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val banners: List<String> = emptyList(),
    val offers: List<Course> = emptyList(),
    val featuredCourses: List<Course> = emptyList(),
    val categories: List<Category> = emptyList(),
    val popularCourses: List<Course> = emptyList(),
    val hasNotifications: Boolean = false,
    val notificationCount: Int = 0
)

class HomeViewModel(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {

                // Load all data
                val featuredCourses = courseRepository.getFeaturedCourses()
                val offers = courseRepository.getOfferCourses()
                val popularCourses = courseRepository.getPopularCourses()
                val categories = categoryRepository.getCategories()
                val notificationCount = notificationRepository.getUnreadNotificationsCount()
                val banners = courseRepository.getBanners()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredCourses = featuredCourses,
                        offers = offers,
                        popularCourses = popularCourses,
                        categories = categories,
                        notificationCount = notificationCount,
                        hasNotifications = notificationCount > 0,
                        banners = banners,
//                        user = student
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

    fun toggleFavorite(courseId: String) {
        viewModelScope.launch {
            try {
                val result = courseRepository.toggleFavorite(courseId)

                // Update the courses in the state
                _uiState.update { state ->
                    val updateCourseList = { courses: List<Course> ->
                        courses.map { course ->
                            if (course.id == courseId) {
                                course.copy(isFavorite = result)
                            } else {
                                course
                            }
                        }
                    }

                    state.copy(
                        featuredCourses = updateCourseList(state.featuredCourses),
                        offers = updateCourseList(state.offers),
                        popularCourses = updateCourseList(state.popularCourses)
                    )
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}