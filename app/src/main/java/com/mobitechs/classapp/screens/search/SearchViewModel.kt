package com.mobitechs.classapp.screens.search


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.SearchRepository
import com.mobitechs.classapp.screens.store.CourseActionsViewModel
import com.mobitechs.classapp.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val searchCourses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    // Search suggestions and history
    val recentSearches: List<String> = emptyList(),
    val popularSearches: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),

    // Categories for browsing
    val topCategories: List<CategoryItem> = emptyList(),

    // Filters
    val selectedFilters: SearchFilters = SearchFilters(),
    val hasActiveFilters: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    override val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository,
) : CourseActionsViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load recent searches from local storage
            val recentSearches = searchRepository.getRecentSearches()

            // Load popular searches
            val popularSearches = searchRepository.getPopularSearches()

            // Create sample categories for browsing
            val topCategories = categoryRepository.getCategories()



            _uiState.update {
                it.copy(
                    recentSearches = recentSearches,
                    popularSearches = popularSearches,
                    topCategories = topCategories.categories
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous search job
        searchJob?.cancel()

        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchCourses = emptyList(),
                    searchSuggestions = emptyList()
                )
            }
            return
        }

        // Debounce search with 300ms delay
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        try {

            //_uiState.value.selectedFilters pass it as filter value
            var reqObj = GetCourseByRequest(sort = Constants.KEY_SORT_POPULAR)
            val courses = courseRepository.getCoursesFilterWise(reqObj)

            if (courses.courses.isNotEmpty()) {
                searchRepository.addRecentSearch(query)
                refreshRecentSearches()
            }

            _uiState.update {
                it.copy(
                    searchCourses = courses.courses,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while searching"
                )
            }
        }

    }

    private fun refreshRecentSearches() {
        viewModelScope.launch {
            val recentSearches = searchRepository.getRecentSearches()
            _uiState.update { it.copy(recentSearches = recentSearches) }
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchCourses = emptyList(),
                searchSuggestions = emptyList(),
                error = null
            )
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            searchRepository.clearRecentSearches()
            _uiState.update { it.copy(recentSearches = emptyList()) }
        }
    }

    fun updateFilter(filterKey: String, value: Any) {
        _uiState.update { state ->
            val updatedFilters = when (filterKey) {
                "priceRange" -> state.selectedFilters.copy(priceRange = value as PriceRange)
                "minRating" -> state.selectedFilters.copy(minRating = value as Int)
                "hasCertificate" -> state.selectedFilters.copy(hasCertificate = value as Boolean)
                "offlineAvailable" -> state.selectedFilters.copy(offlineAvailable = value as Boolean)
                else -> state.selectedFilters
            }

            val hasActiveFilters = updatedFilters != SearchFilters()

            state.copy(
                selectedFilters = updatedFilters,
                hasActiveFilters = hasActiveFilters
            )
        }
    }

    fun applyFilters() {
        if (_uiState.value.searchQuery.isNotEmpty()) {
            viewModelScope.launch {
                performSearch(_uiState.value.searchQuery)
            }
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedFilters = SearchFilters(),
                hasActiveFilters = false
            )
        }
    }



    override fun updateCourseInState(courseId: Int, transform: (Course) -> Course) {
        _uiState.update { state ->
            state.copy(
                searchCourses = state.searchCourses.map { course ->
                    if (course.id == courseId) transform(course) else course
                }
            )
        }
    }
}