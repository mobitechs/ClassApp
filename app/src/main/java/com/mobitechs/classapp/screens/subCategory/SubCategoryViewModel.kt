package com.mobitechs.classapp.screens.subCategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)


class SubCategoryViewModel(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubCategoryUiState(isInitialLoading = true))
    val uiState: StateFlow<SubCategoryUiState> = _uiState.asStateFlow()

    init {
        loadSubCategory()
    }


    fun loadSubCategory() {
        _uiState.update {
            it.copy(
                isInitialLoading = true,
                subcategoriesLoading = true,
                subjectLoading = true
            )
        }

        loadSubcategories()
        loadSubject()

    }

    private fun loadSubcategories() {
        _uiState.update { it.copy(isInitialLoading = true, subcategoriesError = "") }

        viewModelScope.launch {
            val categoryId = "1"
            try {
                val subCategory = categoryRepository.getSubCategoryByCategory(categoryId)
                _uiState.update {
                    it.copy(
                        subcategories = subCategory.subCategories,
                        subcategoriesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subcategoriesLoading = false,
                        subcategoriesError = e.message ?: "Failed to load subjects"
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

    fun retryLoadSection(section: String) {
        when (section) {
            "subcategories" -> loadSubcategories()
            "subject" -> loadSubject()
        }
    }
}