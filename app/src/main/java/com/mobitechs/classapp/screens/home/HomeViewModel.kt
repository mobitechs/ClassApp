package com.mobitechs.classapp.screens.home

import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.Notice
import com.mobitechs.classapp.data.model.response.OfferBanner
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import com.mobitechs.classapp.screens.store.CourseActionsViewModel
import com.mobitechs.classapp.screens.store.updateCourseList
import com.mobitechs.classapp.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    // Global state
    val isInitialLoading: Boolean = true,

    // Individual section states
    val offersBanners: List<OfferBanner> = emptyList(),
    val offersBannersError: String = "",
    val offersBannersLoading: Boolean = false,

    val noticeBoard: List<Notice> = emptyList(),
    val noticeBoardError: String = "",
    val noticeBoardLoading: Boolean = false,

    val popularCourses: List<Course> = emptyList(),
    val popularCoursesError: String = "",
    val popularCoursesLoading: Boolean = false,

    val featuredCourses: List<Course> = emptyList(),
    val featuredCoursesError: String = "",
    val featuredCoursesLoading: Boolean = false,

    val categories: List<CategoryItem> = emptyList(),
    val categoriesError: String = "",
    val categoriesLoading: Boolean = false,

    val hasNotifications: Boolean = false,
    val notificationCount: Int = 0
)

class HomeViewModel(
    override val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : CourseActionsViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isInitialLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

//    private val _toastEvent = Channel<String>()
//    val toastEvent = _toastEvent.receiveAsFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update {
            it.copy(
                isInitialLoading = true,
                offersBannersLoading = true,
                noticeBoardLoading = true,
                popularCoursesLoading = true,
                featuredCoursesLoading = true,
                categoriesLoading = true
            )
        }

        // Load each section independently
        loadOfferBanners()
        loadNoticeBoard()
        loadPopularCourses()
        loadFeaturedCourses()
        loadCategories()
        loadNotificationCount()
    }

    fun retryLoadSection(section: String) {
        when (section) {
            "offersBanners" -> loadOfferBanners()
            "noticeBoard" -> loadNoticeBoard()
            "popularCourses" -> loadPopularCourses()
            "featuredCourses" -> loadFeaturedCourses()
            "categories" -> loadCategories()
        }
    }

    private fun loadOfferBanners() {
        _uiState.update { it.copy(offersBannersLoading = true, offersBannersError = "") }

        viewModelScope.launch {
            try {
                val offersBanners = courseRepository.getOfferBanners()
                _uiState.update {
                    it.copy(
                        offersBanners = offersBanners.offerBanners,
                        offersBannersLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        offersBannersLoading = false,
                        offersBannersError = e.message ?: "Failed to load offers"
                    )
                }
            }
        }
    }

    private fun loadNoticeBoard() {
        _uiState.update { it.copy(noticeBoardLoading = true, noticeBoardError = "") }

        viewModelScope.launch {
            try {
                val noticeBoard = courseRepository.getNoticeboard()
                _uiState.update {
                    it.copy(
                        noticeBoard = noticeBoard.notice,
                        noticeBoardLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        noticeBoardLoading = false,
                        noticeBoardError = e.message ?: "Failed to load notices"
                    )
                }
            }
        }
    }

    private fun loadPopularCourses() {
        _uiState.update { it.copy(popularCoursesLoading = true, popularCoursesError = "") }

        viewModelScope.launch {
            try {
                var reqObj = GetCourseByRequest(sort = Constants.KEY_SORT_POPULAR)
                val popularCourses = courseRepository.getCoursesFilterWise(reqObj)
                _uiState.update {
                    it.copy(
                        popularCourses = popularCourses.courses,
                        popularCoursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        popularCoursesLoading = false,
                        popularCoursesError = e.message ?: "Failed to load popular courses"
                    )
                }
            }
        }
    }

    private fun loadFeaturedCourses() {
        _uiState.update { it.copy(featuredCoursesLoading = true, featuredCoursesError = "") }

        viewModelScope.launch {
            try {
                var reqObj = GetCourseByRequest(sort = Constants.KEY_SORT_FEATURED)
                val featuredCourses = courseRepository.getCoursesFilterWise(reqObj)
                _uiState.update {
                    it.copy(
                        featuredCourses = featuredCourses.courses,
                        featuredCoursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        featuredCoursesLoading = false,
                        featuredCoursesError = e.message ?: "Failed to load featured courses"
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        _uiState.update { it.copy(categoriesLoading = true, categoriesError = "") }

        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategories()
                val subCategories = categoryRepository.getAllSubCategories()
                val subject = categoryRepository.getAllSubject()
                _uiState.update {
                    it.copy(
                        categories = categories.categories,
                        categoriesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        categoriesLoading = false,
                        categoriesError = e.message ?: "Failed to load categories"
                    )
                }
            }
        }
    }

    private fun loadNotificationCount() {
        viewModelScope.launch {
            try {
                val notificationCount = 0 // Replace with actual API call when implemented
                _uiState.update {
                    it.copy(
                        notificationCount = notificationCount,
                        hasNotifications = notificationCount > 0
                    )
                }
            } catch (e: Exception) {
                // Silently fail notification count
            } finally {
                // Set initial loading to false after all components have started loading
                _uiState.update { it.copy(isInitialLoading = false) }
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
                popularCourses = state.popularCourses.updateCourseList(courseId, transform),
                // Update featured courses list
                featuredCourses = state.featuredCourses.updateCourseList(courseId, transform),

            )
        }
    }

}