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
    val downloadProgress: Map<Int, Int> = emptyMap(),
    val downloadingContent: Set<Int> = emptySet()
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

    private fun setupDownloadCallbacks() {
        val secureDownloadManager = (downloadRepository as? MyDownloadsRepository)?.let {
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
                        state.downloadProgress + (contentId to 100)
                    } else {
                        state.downloadProgress - contentId
                    }

                    state.copy(
                        downloadingContent = newDownloadingContent,
                        downloadProgress = newDownloadProgress
                    )
                }

                viewModelScope.launch {
                    delay(1000)
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
                val response = courseRepository.getFreeContent()
                val courses = response.courses ?: emptyList()

                val groupedContent = mutableMapOf<Course, List<Content>>()
                val allFreeContent = mutableListOf<Content>()

                courses.forEach { course ->
                    // Assuming Course now has contents field
                    val freeContent = course.contents?.filter { content ->
                        content.is_free?.equals("Yes", ignoreCase = true) == true
                    } ?: emptyList()

                    if (freeContent.isNotEmpty()) {
                        groupedContent[course] = freeContent
                        allFreeContent.addAll(freeContent)
                    }
                }

                val sortedGroupedContent = groupedContent.toList()
                    .sortedByDescending { it.first.course_like }
                    .toMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allContent = allFreeContent,
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

    fun deleteDownload(contentId: Int) {
        viewModelScope.launch {
            val downloadedContent = downloadRepository.getDownloadByContentId(contentId)
            downloadedContent?.let {
                downloadRepository.deleteDownload(it)
            }
        }
    }

    suspend fun isContentDownloaded(contentId: Int): Boolean {
        return downloadRepository.isContentDownloaded(contentId)
    }

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