package com.mobitechs.classapp.screens.search


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
import com.mobitechs.classapp.data.model.response.Course
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
    val topCategories: List<SearchCategory> = emptyList(),

    // Filters
    val selectedFilters: SearchFilters = SearchFilters(),
    val hasActiveFilters: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    override val courseRepository: CourseRepository
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
            val topCategories = listOf(
                SearchCategory(
                    id = "1",
                    name = "Mathematics",
                    courseCount = 156,
                    icon = Icons.Default.Calculate,
                    backgroundColor = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1976D2)
                ),
                SearchCategory(
                    id = "2",
                    name = "Science",
                    courseCount = 203,
                    icon = Icons.Default.Science,
                    backgroundColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF388E3C)
                ),
                SearchCategory(
                    id = "3",
                    name = "Engineering",
                    courseCount = 142,
                    icon = Icons.Default.Engineering,
                    backgroundColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFF57C00)
                ),
                SearchCategory(
                    id = "4",
                    name = "Business",
                    courseCount = 189,
                    icon = Icons.Default.Business,
                    backgroundColor = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2)
                ),
                SearchCategory(
                    id = "5",
                    name = "Arts",
                    courseCount = 97,
                    icon = Icons.Default.Palette,
                    backgroundColor = Color(0xFFFFEBEE),
                    iconColor = Color(0xFFC62828)
                ),
                SearchCategory(
                    id = "6",
                    name = "Language",
                    courseCount = 124,
                    icon = Icons.Default.Language,
                    backgroundColor = Color(0xFFE0F2F1),
                    iconColor = Color(0xFF00796B)
                )
            )

            _uiState.update {
                it.copy(
                    recentSearches = recentSearches,
                    popularSearches = popularSearches,
                    topCategories = topCategories
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

    fun toggleFavorite(courseId: String) {
        viewModelScope.launch {
            try {
                val is_favourited = searchRepository.toggleFavorite(courseId)

                // Update the course in search results
                _uiState.update { state ->
                    val updatedResults = state.searchCourses.map { course ->
                        if (course.id.toString() == courseId) {
                            course.copy(is_favourited = is_favourited)
                        } else {
                            course
                        }
                    }
                    state.copy(searchCourses = updatedResults)
                }
            } catch (e: Exception) {
                // Handle error silently or show a snackbar
            }
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

    fun loadMoreResults() {
        if (_uiState.value.isLoading || !_uiState.value.hasMorePages) return


    }

    override fun updateCourseInState(
        courseId: Int,
        transform: (Course) -> Course
    ) {

    }
}