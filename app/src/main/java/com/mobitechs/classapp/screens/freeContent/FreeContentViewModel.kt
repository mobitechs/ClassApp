package com.mobitechs.classapp.screens.freeContent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.FreeContentRepository
import com.mobitechs.classapp.data.repository.MyDownloadsRepository
import kotlinx.coroutines.delay
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
    val expandedCourseIds: Set<Int> = emptySet(),
    val downloadProgress: Map<Int, Int> = emptyMap(), // contentId to progress percentage
    val downloadingContent: Set<Int> = emptySet() // content IDs currently downloading
)

class FreeContentViewModel(
    private val repository: FreeContentRepository,
    private val courseRepository: CourseRepository,
    private val downloadRepository: MyDownloadsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreeContentUiState())
    val uiState: StateFlow<FreeContentUiState> = _uiState.asStateFlow()

    init {
        setupDownloadCallbacks()
    }

    //    private fun setupDownloadCallbacks() {
//        val secureDownloadManager = (downloadRepository as? MyDownloadsRepository)?.let {
//            // Access the secure download manager through reflection or make it public
//            // For now, we'll assume you have access to it
//            it.secureDownloadManager
//        }
//
//        secureDownloadManager?.apply {
//            onProgressUpdate = { contentId, progress ->
//                _uiState.update { state ->
//                    state.copy(
//                        downloadProgress = state.downloadProgress + (contentId to progress)
//                    )
//                }
//            }
//
//            onDownloadComplete = { contentId, success ->
//                _uiState.update { state ->
//                    state.copy(
//                        downloadingContent = state.downloadingContent - contentId,
//                        downloadProgress = state.downloadProgress - contentId
//                    )
//                }
//            }
//        }
//    }
    private fun setupDownloadCallbacks() {
        val secureDownloadManager = (downloadRepository as? MyDownloadsRepository)?.let {
            // Access the secure download manager through reflection or make it public
            // For now, we'll assume you have access to it
            it.secureDownloadManager
        }

        secureDownloadManager?.apply {
            onProgressUpdate = { contentId, progress ->
                _uiState.update { state ->
                    state.copy(
                        downloadProgress = state.downloadProgress + (contentId to progress)
                    )
                }
            }

            onDownloadComplete = { contentId, success ->
                _uiState.update { state ->
                    val newDownloadingContent = state.downloadingContent - contentId
                    val newDownloadProgress = if (success) {
                        // Keep progress at 100 briefly to trigger UI update
                        state.downloadProgress + (contentId to 100)
                    } else {
                        // Remove progress on failure
                        state.downloadProgress - contentId
                    }

                    state.copy(
                        downloadingContent = newDownloadingContent,
                        downloadProgress = newDownloadProgress
                    )
                }

                // Clean up progress after a delay
                viewModelScope.launch {
                    delay(1000) // Give UI time to react
                    _uiState.update { state ->
                        state.copy(
                            downloadProgress = state.downloadProgress - contentId
                        )
                    }
                }
            }
        }
    }

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
            course_description = null,
            course_price = "0",
            course_discounted_price = "0",
            course_like = 0,
            course_tags = null,
            category_id = null,
            category_name = null,
            sub_category_id = null,
            sub_category_name = null,
            subject_id = null,
            subject_name = null,
            image = null,
            instructor = null,
            course_duration = null,
            course_expiry_date = null,
            offer_code = null,
            is_favourited = false,
            is_purchased = false,
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

    // Download content using repository
    fun downloadContent(content: Content, course: Course) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    downloadingContent = state.downloadingContent + content.id,
                    downloadProgress = state.downloadProgress + (content.id to 0)
                )
            }
            downloadRepository.downloadContent(content, course)
        }
    }

    // Cancel download
    fun cancelDownload(contentId: Int) {
        viewModelScope.launch {
            downloadRepository.cancelDownload(contentId)
            _uiState.update { state ->
                state.copy(
                    downloadingContent = state.downloadingContent - contentId,
                    downloadProgress = state.downloadProgress - contentId
                )
            }
        }
    }

    // Delete downloaded content
    fun deleteDownload(contentId: Int) {
        viewModelScope.launch {
            val downloadedContent = downloadRepository.getDownloadByContentId(contentId)
            downloadedContent?.let {
                downloadRepository.deleteDownload(it)
            }
        }
    }

    // Check if content is downloaded using repository
    suspend fun isContentDownloaded(contentId: Int): Boolean {
        return downloadRepository.isContentDownloaded(contentId)
    }

    // Get downloaded content details
    suspend fun getDownloadedContent(contentId: Int): DownloadContent? {
        return downloadRepository.getDownloadByContentId(contentId)
    }

    fun isDownloading(contentId: Int): Boolean {
        return _uiState.value.downloadingContent.contains(contentId)
    }

    fun getDownloadProgress(contentId: Int): Int {
        return _uiState.value.downloadProgress[contentId] ?: 0
    }
}