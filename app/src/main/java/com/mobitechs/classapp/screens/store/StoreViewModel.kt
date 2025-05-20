package com.mobitechs.classapp.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State class with independent loading and error states for each data section
 */
data class StoreUiState(
    // Categories section
    val categories: List<CategoryItem> = emptyList(),
    val categoriesLoading: Boolean = true,
    val categoriesError: String = "",

    val selectedCategoryId: Int? = null,
    val selectedCategory: CategoryItem? = null,

    // Sub-categories section
    val subCategories: List<SubCategoryItem> = emptyList(),
    val subCategoriesLoading: Boolean = false,
    val subCategoriesError: String = "",

    // Subjects section
    val subjects: List<SubjectItem> = emptyList(),
    val subjectsLoading: Boolean = false,
    val subjectsError: String = "",

    // Selected filters
    val selectedSubCategoryId: String? = null,
    val selectedSubjectId: String? = null,
    val selectedPriceRange: PriceRange? = PriceRange("all", "All Prices"),

    // Course data
    val allCourses: List<Course> = emptyList(),
    val coursesLoading: Boolean = true,
    val coursesError: String = "",
    val filteredCourses: List<Course> = emptyList()
) {
    // Computed property for overall loading state (for main indicators)
    val isInitialLoading: Boolean
        get() = categoriesLoading && coursesLoading
}

/**
 * Data class for price range options
 */
data class PriceRange(
    val id: String,
    val displayName: String
)

/**
 * ViewModel for the Store screen with independent data loading operations
 */
class StoreViewModel(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Private mutable state flow
    private val _uiState = MutableStateFlow(StoreUiState())

    // Public immutable state flow exposed to the UI
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        // Load initial data when ViewModel is created
        loadInitialData()
    }

    /**
     * Loads all required data for the Store screen independently
     */
    private fun loadInitialData() {
        // Load each data type independently
        loadCategories()
        loadAllSubCategories()
        loadAllSubjects()
        loadInitialCourses()
    }

    /**
     * Loads categories independently
     */
    private fun loadCategories() {
        _uiState.update { it.copy(categoriesLoading = true, categoriesError = "") }

        viewModelScope.launch {
            try {
                val categoriesResponse = categoryRepository.getCategories()
                _uiState.update { state ->
                    state.copy(
                        categories = categoriesResponse.categories,
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

    /**
     * Loads all subcategories independently
     */
    private fun loadAllSubCategories() {
        _uiState.update { it.copy(subCategoriesLoading = true, subCategoriesError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getAllSubCategories()
                _uiState.update { state ->
                    state.copy(
                        subCategories = response.subCategories,
                        subCategoriesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subCategoriesLoading = false,
                        subCategoriesError = e.message ?: "Failed to load subcategories"
                    )
                }
            }
        }
    }

    /**
     * Loads all subjects independently
     */
    private fun loadAllSubjects() {
        _uiState.update { it.copy(subjectsLoading = true, subjectsError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getAllSubject()
                _uiState.update { state ->
                    state.copy(
                        subjects = response.subjects,
                        subjectsLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectsLoading = false,
                        subjectsError = e.message ?: "Failed to load subjects"
                    )
                }
            }
        }
    }

    /**
     * Loads initial courses (popular courses) independently
     */
    private fun loadInitialCourses() {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                val courseResponse = courseRepository.getLatestCourses()
                _uiState.update { state ->
                    state.copy(
                        allCourses = courseResponse.courses,
                        filteredCourses = courseResponse.courses,
                        coursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        coursesLoading = false,
                        coursesError = e.message ?: "Failed to load courses"
                    )
                }
            }
        }
    }

    /**
     * Selects a category and loads related data
     */
    fun selectCategory(categoryId: Int) {
        // Special case for "Top Picks" (-1)
        if (categoryId == -1) {
            _uiState.update { state ->
                state.copy(
                    selectedCategoryId = null,
                    selectedCategory = null,
                    // Reset filters when changing categories
                    selectedSubCategoryId = null,
                    selectedSubjectId = null
                )
            }
            loadPopularCourses()
            return
        }

        // Regular category
        val selectedCategory = uiState.value.categories.find { it.id == categoryId }

        _uiState.update { state ->
            state.copy(
                selectedCategoryId = categoryId,
                selectedCategory = selectedCategory,
                // Reset filters when changing categories
                selectedSubCategoryId = null,
                selectedSubjectId = null
            )
        }

        // Load courses for the selected category
        loadCoursesByCategory(categoryId)

        // Load subcategories for the selected category
        loadSubCategoriesByCategory(categoryId)
    }

    /**
     * Loads subcategories for a specific category independently
     */
    private fun loadSubCategoriesByCategory(categoryId: Int) {
        _uiState.update { it.copy(subCategoriesLoading = true, subCategoriesError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getCategoryWiseSubCategory(categoryId.toString())

                _uiState.update { state ->
                    state.copy(
                        subCategories = response.subCategories,
                        subCategoriesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        subCategoriesLoading = false,
                        subCategoriesError = e.message ?: "Failed to load subcategories"
                        // Keep existing subcategories on error
                    )
                }
            }
        }
    }

    /**
     * Loads popular courses independently
     */
    private fun loadPopularCourses() {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                val coursesResponse = courseRepository.getPopularCourses()
                _uiState.update { state ->
                    state.copy(
                        allCourses = coursesResponse,
                        filteredCourses = applyFilters(
                            coursesResponse,
                            state.selectedSubCategoryId,
                            state.selectedSubjectId,
                            state.selectedPriceRange
                        ),
                        coursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        coursesLoading = false,
                        coursesError = e.message ?: "Failed to load courses"
                        // Keep existing courses on error
                    )
                }
            }
        }
    }

    /**
     * Loads courses by category independently
     */
    private fun loadCoursesByCategory(categoryId: Int) {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                val courses = courseRepository.getCourses(categoryId = categoryId.toString())
                _uiState.update { state ->
                    state.copy(
                        allCourses = courses,
                        filteredCourses = applyFilters(
                            courses,
                            state.selectedSubCategoryId,
                            state.selectedSubjectId,
                            state.selectedPriceRange
                        ),
                        coursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        coursesLoading = false,
                        coursesError = e.message ?: "Failed to load courses"
                        // Keep existing courses on error
                    )
                }
            }
        }
    }

    /**
     * Selects a subcategory for filtering
     */
    fun selectSubCategory(subCategoryId: String) {
        _uiState.update { state ->
            state.copy(
                selectedSubCategoryId = subCategoryId,
                // Also clear subject filter as it depends on subcategory
                selectedSubjectId = null
            )
        }

        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()

        // Load subjects for this subcategory
        loadSubjectsBySubcategory(subCategoryId)
    }

    /**
     * Loads subjects for a specific subcategory independently
     */
    private fun loadSubjectsBySubcategory(subCategoryId: String) {
        _uiState.update { it.copy(subjectsLoading = true, subjectsError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getSubjectSubCategoryWise(subCategoryId)

                _uiState.update { state ->
                    state.copy(
                        subjects = response.subjects,
                        subjectsLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectsLoading = false,
                        subjectsError = e.message ?: "Failed to load subjects"
                        // Keep existing subjects on error
                    )
                }
            }
        }
    }

    /**
     * Selects a subject for filtering
     */
    fun selectSubject(subjectId: String) {
        _uiState.update { state ->
            state.copy(selectedSubjectId = subjectId)
        }

        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()
    }

    /**
     * Selects a price range for filtering
     */
    fun selectPriceRange(priceRange: PriceRange) {
        _uiState.update { state ->
            state.copy(selectedPriceRange = priceRange)
        }

        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()
    }

    /**
     * Applies all current filters and updates the UI state
     */
    private fun applyFiltersAndUpdateState() {
        val currentState = uiState.value

        _uiState.update { state ->
            state.copy(
                filteredCourses = applyFilters(
                    currentState.allCourses,
                    currentState.selectedSubCategoryId,
                    currentState.selectedSubjectId,
                    currentState.selectedPriceRange
                )
            )
        }
    }

    /**
     * Applies filters to the course list
     */
    private fun applyFilters(
        courses: List<Course>,
        subCategoryId: String?,
        subjectId: String?,
        priceRange: PriceRange?
    ): List<Course> {
        var filteredCourses = courses

        // Apply subcategory filter
        if (!subCategoryId.isNullOrEmpty()) {
            filteredCourses = filteredCourses.filter {
                it.course_subcategory_id.toString() == subCategoryId
            }
        }

        // Apply subject filter
        if (!subjectId.isNullOrEmpty()) {
            filteredCourses = filteredCourses.filter {
                it.course_subject_id.toString() == subjectId
            }
        }

        // Apply price range filter
        if (priceRange != null && priceRange.id != "all") {
            filteredCourses = when (priceRange.id) {
                "free" -> filteredCourses.filter {
                    it.course_price == "0" || it.course_discounted_price == "0"
                }
                "0-500" -> filteredCourses.filter {
                    val price = it.course_discounted_price?.toDoubleOrNull()
                        ?: it.course_price.toDoubleOrNull() ?: 0.0
                    price > 0 && price <= 500
                }
                "500-1000" -> filteredCourses.filter {
                    val price = it.course_discounted_price?.toDoubleOrNull()
                        ?: it.course_price.toDoubleOrNull() ?: 0.0
                    price > 500 && price <= 1000
                }
                "1000-2000" -> filteredCourses.filter {
                    val price = it.course_discounted_price?.toDoubleOrNull()
                        ?: it.course_price.toDoubleOrNull() ?: 0.0
                    price > 1000 && price <= 2000
                }
                "2000+" -> filteredCourses.filter {
                    val price = it.course_discounted_price?.toDoubleOrNull()
                        ?: it.course_price.toDoubleOrNull() ?: 0.0
                    price > 2000
                }
                else -> filteredCourses
            }
        }

        return filteredCourses
    }

    /**
     * Selects a category directly from the bottom sheet
     */
    fun selectCategoryFromFilter(categoryId: String) {
        selectCategory(categoryId.toInt())
    }

    /**
     * Retry functions for each section when they fail
     */
    fun retryLoadCategories() {
        loadCategories()
    }

    fun retryLoadSubCategories() {
        if (uiState.value.selectedCategoryId != null) {
            loadSubCategoriesByCategory(uiState.value.selectedCategoryId!!)
        } else {
            loadAllSubCategories()
        }
    }

    fun retryLoadSubjects() {
        if (uiState.value.selectedSubCategoryId != null) {
            loadSubjectsBySubcategory(uiState.value.selectedSubCategoryId!!)
        } else {
            loadAllSubjects()
        }
    }

    fun retryLoadCourses() {
        if (uiState.value.selectedCategoryId != null) {
            loadCoursesByCategory(uiState.value.selectedCategoryId!!)
        } else {
            loadPopularCourses()
        }
    }
}