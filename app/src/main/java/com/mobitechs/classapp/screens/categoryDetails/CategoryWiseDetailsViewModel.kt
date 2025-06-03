package com.mobitechs.classapp.screens.categoryDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


data class SubCategoryUiState(
    val isInitialLoading: Boolean = true,
    val subcategories: List<SubCategoryItem> = emptyList(),
    val subcategoriesError: String = "",
    val subcategoriesLoading: Boolean = false,

    val subject: List<SubjectItem> = emptyList(),
    val subjectError: String = "",
    val subjectLoading: Boolean = false,

    // Popular courses
    val popularCourses: List<Course> = emptyList(),
    val popularCoursesLoading: Boolean = false,
    val popularCoursesError: String = "",

    // Subcategory specific courses
    val subcategoryCourses: List<Course> = emptyList(),
    val subcategoryCoursesLoading: Boolean = false,
    val subcategoryCoursesError: String = "",

    // Subject specific courses
    val subjectCourses: List<Course> = emptyList(),
    val subjectCoursesLoading: Boolean = false,
    val subjectCoursesError: String = "",

    // All courses
    val allCourses: List<Course> = emptyList(),
    val allCoursesLoading: Boolean = false,
    val allCoursesError: String = "",

    val selectedCategoryId: Int? = null,
    val selectedSubCategoryId: String? = null,
    val selectedSubjectId: String? = null
)


class SubCategoryViewModel(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubCategoryUiState(isInitialLoading = true))
    val uiState: StateFlow<SubCategoryUiState> = _uiState.asStateFlow()

    init {
        // Don't load immediately, wait for category ID to be set
    }

    fun setSelectedCategory(categoryId: Int) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadSubCategory()
    }

    fun setSelectedSubcategory(subcategoryId: String) {
        _uiState.update { it.copy(selectedSubCategoryId = subcategoryId) }
    }

    fun setSelectedSubject(subjectId: String) {
        _uiState.update { it.copy(selectedSubjectId = subjectId) }
    }

    fun loadSubCategory() {
        _uiState.update {
            it.copy(
                isInitialLoading = true,
                subcategoriesLoading = true,
                subjectLoading = true,
                popularCoursesLoading = true,
                allCoursesLoading = true
            )
        }

        loadSubcategories()
        loadSubject()
        loadPopularCourses()
        loadAllCourses()
    }

    private fun loadSubcategories() {
        _uiState.update { it.copy(subcategoriesLoading = true, subcategoriesError = "") }

        viewModelScope.launch {
            val categoryId = _uiState.value.selectedCategoryId?.toString() ?: "1"
            try {
                val subCategory = categoryRepository.getSubCategoryByCategory(categoryId)
                _uiState.update {
                    it.copy(
                        subcategories = subCategory.subCategories,
                        subcategoriesLoading = false
                    )
                }

                // Load courses for the first subcategory
                if (subCategory.subCategories.isNotEmpty()) {
                    val firstSubcategoryId = subCategory.subCategories.first().id.toString()
                    setSelectedSubcategory(firstSubcategoryId)
                    loadCoursesBySubcategory(firstSubcategoryId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subcategoriesLoading = false,
                        subcategoriesError = e.message ?: "Failed to load subcategories"
                    )
                }
            }
        }
    }

    fun loadSubject() {
        _uiState.update { it.copy(subjectLoading = true, subjectError = "") }

        viewModelScope.launch {
            try {
                val subject = categoryRepository.getAllSubject()
                _uiState.update {
                    it.copy(
                        subject = subject.subjects,
                        subjectLoading = false
                    )
                }

                // Load courses for the first subject
                if (subject.subjects.isNotEmpty()) {
                    val firstSubjectId = subject.subjects.first().id.toString()
                    setSelectedSubject(firstSubjectId)
                    loadCoursesBySubject(firstSubjectId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectLoading = false,
                        subjectError = e.message ?: "Failed to load subjects"
                    )
                }
            }
        }
    }

    private fun loadPopularCourses() {
        _uiState.update { it.copy(popularCoursesLoading = true, popularCoursesError = "") }

        viewModelScope.launch {
            try {
                val popularCourses = courseRepository.getPopularCourses()
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

    private fun loadAllCourses() {
        _uiState.update { it.copy(allCoursesLoading = true, allCoursesError = "") }

        viewModelScope.launch {
            try {
                val categoryId = _uiState.value.selectedCategoryId ?: 1
                val allCourses = courseRepository.getCourses(categoryId = categoryId)
                _uiState.update {
                    it.copy(
                        allCourses = allCourses.courses,
                        allCoursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        allCoursesLoading = false,
                        allCoursesError = e.message ?: "Failed to load all courses"
                    )
                }
            } finally {
                _uiState.update { it.copy(isInitialLoading = false) }
            }
        }
    }

    fun loadCoursesBySubcategory(subcategoryId: String) {
        _uiState.update {
            it.copy(
                subcategoryCoursesLoading = true,
                subcategoryCoursesError = "",
                selectedSubCategoryId = subcategoryId
            )
        }

        viewModelScope.launch {
            try {
                val categoryId = _uiState.value.selectedCategoryId
                val subCategoryWiseCourses = courseRepository.getCourseByCategorySubCategory(categoryId.toString(), subcategoryId.toString())
                _uiState.update {
                    it.copy(
                        subcategoryCourses = subCategoryWiseCourses.courses,
                        subcategoryCoursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subcategoryCoursesLoading = false,
                        subcategoryCoursesError = e.message ?: "Failed to load subcategory courses"
                    )
                }
            }
        }
    }

    fun loadCoursesBySubject(subjectId: String) {
        _uiState.update {
            it.copy(
                subjectCoursesLoading = true,
                subjectCoursesError = "",
                selectedSubjectId = subjectId
            )
        }

        viewModelScope.launch {
            try {
//                // Since the API doesn't have subject-specific filtering from the provided code,
//                // we'll filter client-side or you can update based on your actual API
//                val courses = courseRepository.getCourses(limit = 10)
//                val filteredCourses = courses.filter { it.course_subject_id == subjectId.toInt() }

                val categoryId = _uiState.value.selectedCategoryId.toString()
                val subCategoryId = _uiState.value.selectedSubCategoryId.toString()
                val subjectWiseCourses = courseRepository.getCoursesByCategorySubCategorySubject(categoryId, subCategoryId, subjectId.toString())

                _uiState.update {
                    it.copy(
                        subjectCourses = subjectWiseCourses.courses,
                        subjectCoursesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectCoursesLoading = false,
                        subjectCoursesError = e.message ?: "Failed to load subject courses"
                    )
                }
            }
        }
    }

    fun toggleFavorite(courseId: String) {
//        viewModelScope.launch {
//            try {
//                val result = courseRepository.toggleFavorite(courseId)
//
//                // Update all course lists
//                _uiState.update { state ->
//                    val updateCourseList = { courses: List<Course> ->
//                        courses.map { course ->
//                            if (course.id == courseId.toInt()) {
//                                course.copy(isFavorite = result)
//                            } else {
//                                course
//                            }
//                        }
//                    }
//
//                    state.copy(
//                        popularCourses = updateCourseList(state.popularCourses),
//                        subcategoryCourses = updateCourseList(state.subcategoryCourses),
//                        subjectCourses = updateCourseList(state.subjectCourses),
//                        allCourses = updateCourseList(state.allCourses)
//                    )
//                }
//            } catch (e: Exception) {
//                // Handle error silently
//            }
//        }
    }

    fun retryLoadSection(section: String) {
        when (section) {
            "subcategories" -> loadSubcategories()
            "subject" -> loadSubject()
            "popularCourses" -> loadPopularCourses()
            "allCourses" -> loadAllCourses()
        }
    }
}