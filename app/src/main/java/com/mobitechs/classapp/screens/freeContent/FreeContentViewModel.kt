package com.mobitechs.classapp.screens.freeContent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.FreeContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FreeContentUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val allContent: List<Content> = emptyList(),
    val groupedContent: Map<Course, List<Content>> = emptyMap(),
    val searchQuery: String = "",
    val selectedContentType: String? = null,
    val expandedCourseIds: Set<Int> = emptySet() // Add this to track expanded courses
)

class FreeContentViewModel(
    private val repository: FreeContentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreeContentUiState())
    val uiState: StateFlow<FreeContentUiState> = _uiState.asStateFlow()

    fun toggleCourseExpanded(courseId: Int) {
        _uiState.update { state ->
            val newExpandedIds = if (state.expandedCourseIds.contains(courseId)) {
                state.expandedCourseIds - courseId
            } else {
                state.expandedCourseIds + courseId
            }
            state.copy(expandedCourseIds = newExpandedIds)
        }
    }

    fun loadFreeContent() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                // Get free content from API
                val response = courseRepository.getFreeContent()
                val freeContent = response.content?.filter {
                    it.is_free == "Yes"
                } ?: emptyList()

                // Group content by course ID first
                val contentByCourseId = freeContent.groupBy { it.course_id }

                // Create course objects (either from content or dummy)
                val groupedContent = mutableMapOf<Course, List<Content>>()

                contentByCourseId.forEach { (courseId, contents) ->
                    // Check if any content has course object
                    val courseFromContent = contents.firstOrNull { it.course != null }?.course

                    val course = courseFromContent ?: createDummyCourse(courseId, contents.size)
                    groupedContent[course] = contents
                }

                // Sort by course likes (descending) or by course ID
                val sortedGroupedContent = groupedContent.toList()
                    .sortedByDescending { it.first.course_like }
                    .toMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allContent = freeContent,
                        groupedContent = sortedGroupedContent,
                        error = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load free content"
                    )
                }
            }
        }
    }

    private fun createDummyCourse(courseId: Int, contentCount: Int): Course {
        return Course(
            id = courseId,
            course_name = "Course ID: $courseId",
            course_description = null, // Keep blank
            course_price = "0",
            course_discounted_price = "0",
            course_like = 0, // No likes to show
            course_tags = null, // No tags
            category_id = null,
            category_name = null,
            sub_category_id = null,
            sub_category_name = null,
            subject_id = null,
            subject_name = null,
            image = null, // No image
            instructor = null, // No instructor
            course_duration = null,
            course_expiry_date = null,
            offer_code = null,
            is_favourited = false,
            isPurchased = false,
            is_in_wishlist = false,
            is_liked = false,
            is_active = "Active",
            created_at = null,
            updated_at = null,
            deleted_at = null,
            added_by = null,
            lastSyncedAt = System.currentTimeMillis()
        )
    }


}