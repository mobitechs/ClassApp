package com.mobitechs.classapp.screens.store


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.Category
import com.mobitechs.classapp.data.model.Course
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val categories: List<Category> = emptyList(),
    val subCategories: List<Category> = emptyList(),
    val selectedCategoryId: String = "",
    val selectedSubCategoryId: String = "",
    val courses: List<Course> = emptyList(),
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false
)

class StoreViewModel(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(isLoading = true))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val PAGE_SIZE = 20

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategories()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = categories,
                        selectedCategoryId = categories.firstOrNull()?.id ?: ""
                    )
                }

                // Load subcategories for the first category
                if (categories.isNotEmpty()) {
                    loadSubCategories(categories.first().id)
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

    fun loadSubCategories(categoryId: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = "",
                selectedCategoryId = categoryId,
                selectedSubCategoryId = "",
                courses = emptyList()
            )
        }

        viewModelScope.launch {
            try {
                val subCategories = categoryRepository.getSubcategories(categoryId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        subCategories = subCategories
                    )
                }

                // Load courses for this category
                loadCourses(categoryId, null, 1)
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

    fun selectSubCategory(subCategoryId: String) {
        _uiState.update {
            it.copy(
                selectedSubCategoryId = subCategoryId,
                courses = emptyList(),
                currentPage = 1
            )
        }

        // Load courses for this subcategory
        loadCourses(uiState.value.selectedCategoryId, subCategoryId, 1)
    }

    fun loadCourses(categoryId: String, subCategoryId: String?, page: Int) {
        val isFirstPage = page == 1

        _uiState.update {
            it.copy(
                isLoading = isFirstPage,
                isLoadingMore = !isFirstPage,
                error = "",
                currentPage = page
            )
        }

        viewModelScope.launch {
            try {
                val courses = courseRepository.getCourses(
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    searchQuery = uiState.value.searchQuery,
                    page = page,
                    limit = PAGE_SIZE
                )

                _uiState.update {
                    val updatedCourses = if (isFirstPage) {
                        courses
                    } else {
                        it.courses + courses
                    }

                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        courses = updatedCourses,
                        hasMorePages = courses.size == PAGE_SIZE
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun loadMoreCourses() {
        if (!uiState.value.isLoadingMore && uiState.value.hasMorePages) {
            val nextPage = uiState.value.currentPage + 1
            loadCourses(
                categoryId = uiState.value.selectedCategoryId,
                subCategoryId = uiState.value.selectedSubCategoryId.takeIf { it.isNotEmpty() },
                page = nextPage
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun search() {
        loadCourses(
            categoryId = uiState.value.selectedCategoryId,
            subCategoryId = uiState.value.selectedSubCategoryId.takeIf { it.isNotEmpty() },
            page = 1
        )
    }

    fun toggleFavorite(courseId: String) {
        viewModelScope.launch {
            try {
                val result = courseRepository.toggleFavorite(courseId)

                // Update the course in the list
                _uiState.update { state ->
                    val updatedCourses = state.courses.map { course ->
                        if (course.id == courseId) {
                            course.copy(isFavorite = result)
                        } else {
                            course
                        }
                    }

                    state.copy(courses = updatedCourses)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun toggleWishlist(courseId: String) {
        viewModelScope.launch {
            try {
                val result = courseRepository.toggleWishlist(courseId)

                // Update the course in the list
                _uiState.update { state ->
                    val updatedCourses = state.courses.map { course ->
                        if (course.id == courseId) {
                            course.copy(isWishlisted = result)
                        } else {
                            course
                        }
                    }

                    state.copy(courses = updatedCourses)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}