package com.mobitechs.classapp.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
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

    val selectedCategoryId: Int? = 0,
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
    val selectedSubCategoryId: Int? = 0,
    val selectedSubjectId: Int? = 0,
    val selectedPriceRange: PriceRange = PriceRange(0f, 10000f, false), // Changed to support slider

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
 * Data class for price range options (updated for slider)
 */
data class PriceRange(
    val minPrice: Float,
    val maxPrice: Float,
    val isActive: Boolean = false
) {
    val displayName: String
        get() = when {
            !isActive -> "All Prices"
            minPrice == 0f && maxPrice >= 10000f -> "All Prices"
            minPrice == 0f -> "Under ₹${maxPrice.toInt()}"
            maxPrice >= 10000f -> "₹${minPrice.toInt()}+"
            else -> "₹${minPrice.toInt()} - ₹${maxPrice.toInt()}"
        }
}

/**
 * ViewModel for the Store screen with independent data loading operations
 */
class StoreViewModel(
    override val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository
) : CourseActionsViewModel() {

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
        getCategories()
        getAllSubCategories()
        getAllSubject()
        getLatestCourses()
    }



    /**
     * Selects a category and loads related data
     */
    fun selectCategory(categoryId: Int) {
        // Special case for "New Courses" (-1)

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
            getLatestCourses() // Load popular courses default courses

            // Load ALL subcategories and subjects (not filtered by category)
            getAllSubCategories()
            getAllSubject()
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
        getCoursesByCategory(categoryId)

        // Load subcategories for the selected category (filtered)
        getSubCategoryByCategory(categoryId)

        // When a specific category is selected, load ALL subjects (not filtered by category)
        // Users can then filter subjects by selecting a subcategory
        getSubjectsByCategory(categoryId)
    }

    /**
     * Selects a subcategory for filtering
     */
    fun selectSubCategory(subCategoryId: Int) {
        _uiState.update { state ->
            state.copy(
                selectedSubCategoryId = subCategoryId,
                // Also clear subject filter as it depends on subcategory
                selectedSubjectId = null
            )
        }

        //get course by sub categories
        getCoursesByCategorySubCategory(subCategoryId)
        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()

        // Load subjects for this subcategory
        getSubjectsBySubcategory(subCategoryId)
    }

    /**
     * Selects a subject for filtering
     */
    fun selectSubject(subjectId: Int) {
        _uiState.update { state ->
            state.copy(selectedSubjectId = subjectId)
        }

        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()
    }

    /**
     * Selects a price range for filtering (updated for slider)
     */
    fun selectPriceRange(minPrice: Float, maxPrice: Float) {
        _uiState.update { state ->
            state.copy(
                selectedPriceRange = PriceRange(
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    isActive = !(minPrice == 0f && maxPrice >= 10000f)
                )
            )
        }

        // Apply filters and update filtered courses
        applyFiltersAndUpdateState()
    }

    /**
     * Clear filter methods for Applied Filters component
     */
    fun clearCategoryFilter() {
        selectCategory(-1) // Reset to "New Courses"
    }

    fun clearSubCategoryFilter() {
        _uiState.update { state ->
            state.copy(
                selectedSubCategoryId = null,
                selectedSubjectId = null // Also clear subject when clearing subcategory
            )
        }
        applyFiltersAndUpdateState()
    }

    fun clearSubjectFilter() {
        _uiState.update { state ->
            state.copy(selectedSubjectId = null)
        }
        applyFiltersAndUpdateState()
    }

    fun clearPriceFilter() {
        _uiState.update { state ->
            state.copy(
                selectedPriceRange = PriceRange(0f, 10000f, false)
            )
        }
        applyFiltersAndUpdateState()
    }

    /**
     * Reset all filters at once
     */
    fun resetAllFilters() {
        _uiState.update { state ->
            state.copy(
                selectedCategoryId = null,
                selectedCategory = null,
                selectedSubCategoryId = null,
                selectedSubjectId = null,
                selectedPriceRange = PriceRange(0f, 10000f, false)
            )
        }

        // Load popular courses when all filters are reset
        getPopularCourses()

        // Reload ALL subcategories and subjects (not filtered by category)
        getAllSubCategories()
        getAllSubject()
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
     * Applies filters to the course list (updated for price slider)
     */
    private fun applyFilters(
        courses: List<Course>,
        subCategoryId: Int? = 0,
        subjectId: Int? = 0,
        priceRange: PriceRange
    ): List<Course> {
        var filteredCourses = courses

        // Apply subcategory filter
        if (subCategoryId != 0) {
            filteredCourses = filteredCourses.filter {
                it.sub_category_id == subCategoryId
            }
        }

        // Apply subject filter
        if (subjectId!=0) {
            filteredCourses = filteredCourses.filter {
                it.subject_id == subjectId
            }
        }

        // Apply price range filter (updated for slider)
        if (priceRange.isActive) {
            filteredCourses = filteredCourses.filter { course ->
                val price = course.course_discounted_price?.toFloatOrNull()
                    ?: course.course_price.toFloatOrNull() ?: 0f

                price >= priceRange.minPrice && price <= priceRange.maxPrice
            }
        }

        return filteredCourses
    }



    /**
     * Retry functions for each section when they fail
     */
    fun retryLoadCategories() {
        getCategories()
    }

    fun retryLoadSubCategories() {
        if (uiState.value.selectedCategoryId != null) {
            // If a specific category is selected, load filtered subcategories
            getSubCategoryByCategory(uiState.value.selectedCategoryId!!)
        } else {
            // If no category selected (New Courses), load ALL subcategories
            getAllSubCategories()
        }
    }

    fun retryLoadSubjects() {
        if (uiState.value.selectedSubCategoryId != null) {
            // If a subcategory is selected, load filtered subjects
            getSubjectsBySubcategory(uiState.value.selectedSubCategoryId!!)
        } else {
            // If no subcategory selected, load ALL subjects
            getAllSubject()
        }
    }

    fun retryLoadCourses() {
        if (uiState.value.selectedCategoryId != null) {
            getCoursesByCategory(uiState.value.selectedCategoryId!!)
        } else {
            getPopularCourses()
        }
    }




//    API Calls

    /**
     * Loads categories independently
     */
    private fun getCategories() {
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
    private fun getAllSubCategories() {
        _uiState.update { it.copy(subCategoriesLoading = true, subCategoriesError = "") }

        viewModelScope.launch {
            try {
                // Try to get all subcategories (without category filter)
                val response = categoryRepository.getAllSubCategories()
                _uiState.update { state ->
                    state.copy(
                        subCategories = response.subCategories,
                        subCategoriesLoading = false
                    )
                }
                // Debug log
                println("DEBUG: Loaded ${response.subCategories.size} subcategories")
                if (response.subCategories.isNotEmpty()) {
                    println("DEBUG: First subcategory: ${response.subCategories.first().name}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subCategoriesLoading = false,
                        subCategoriesError = e.message ?: "Failed to load subcategories"
                    )
                }
                // Debug log
                println("DEBUG: Error loading subcategories: ${e.message}")
            }
        }
    }

    /**
     * Loads subcategories for a specific category independently
     */
    private fun getSubCategoryByCategory(categoryId: Int) {
        _uiState.update { it.copy(subCategoriesLoading = true, subCategoriesError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getSubCategoryByCategory(categoryId)

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
     * Loads all subjects independently
     */
    private fun getAllSubject() {
        _uiState.update { it.copy(subjectsLoading = true, subjectsError = "") }

        viewModelScope.launch {
            try {
                // Try to get all subjects (without category filter)
                val response = categoryRepository.getAllSubject()
                _uiState.update { state ->
                    state.copy(
                        subjects = response.subjects,
                        subjectsLoading = false
                    )
                }
                // Debug log
                println("DEBUG: Loaded ${response.subjects.size} subjects")
                if (response.subjects.isNotEmpty()) {
                    println("DEBUG: First subject: ${response.subjects.first().name}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectsLoading = false,
                        subjectsError = e.message ?: "Failed to load subjects"
                    )
                }
                // Debug log
                println("DEBUG: Error loading subjects: ${e.message}")
            }
        }
    }

    /**
     * Loads subjects for a specific subcategory independently
     */
    private fun getSubjectsByCategory(categoryId: Int) {
        _uiState.update { it.copy(subjectsLoading = true, subjectsError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getSubjectByCategory(categoryId =  categoryId)

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
     * Loads subjects for a specific subcategory independently
     */
    private fun getSubjectsBySubcategory(subCategoryId: Int) {
        _uiState.update { it.copy(subjectsLoading = true, subjectsError = "") }

        viewModelScope.launch {
            try {
                val response = categoryRepository.getSubjectBySubCategory(subCategoryId)

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
     * Loads initial courses (popular courses) independently
     */
    private fun getLatestCourses() {
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
     * Loads popular courses independently
     */
    private fun getPopularCourses() {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                val coursesResponse = courseRepository.getPopularCourses()
                _uiState.update { state ->
                    state.copy(
                        allCourses = coursesResponse.courses,
                        filteredCourses = applyFilters(
                            coursesResponse.courses,
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
    private fun getCoursesByCategory(categoryId: Int) {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                var reqObj = GetCourseByRequest(categoryId = categoryId)
                val courses = courseRepository.getCoursesFilterWise(reqObj)
                _uiState.update { state ->
                    state.copy(
                        allCourses = courses.courses,
                        filteredCourses = applyFilters(
                            courses.courses,
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
    private fun getCoursesByCategorySubCategory(subCategoryId: Int) { //
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {

                var reqObj = GetCourseByRequest(categoryId = _uiState.value.selectedCategoryId, subCategoryId = subCategoryId)
                val courses = courseRepository.getCoursesFilterWise(reqObj)
                _uiState.update { state ->
                    state.copy(
                        allCourses = courses.courses,
                        filteredCourses = applyFilters(
                            courses.courses,
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
    private fun getCoursesByCategorySubCategorySubject(subjectId: Int) {
        _uiState.update { it.copy(coursesLoading = true, coursesError = "") }

        viewModelScope.launch {
            try {
                var reqObj = GetCourseByRequest(categoryId =  _uiState.value.selectedCategoryId,subCategoryId = _uiState.value.selectedSubCategoryId, subjectId = subjectId)
                val courses = courseRepository.getCoursesFilterWise(reqObj)

                _uiState.update { state ->
                    state.copy(
                        allCourses = courses.courses,
                        filteredCourses = applyFilters(
                            courses.courses,
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




    override fun updateCourseInState(
        courseId: Int,
        transform: (Course) -> Course
    ) {
        _uiState.update { state ->
            state.copy(
                // Update popular courses list
                filteredCourses = state.filteredCourses.updateCourse(courseId, transform),
                allCourses = state.allCourses.updateCourse(courseId, transform),

                )
        }
    }

}