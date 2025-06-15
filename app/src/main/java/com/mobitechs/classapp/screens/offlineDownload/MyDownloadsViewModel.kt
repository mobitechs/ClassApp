package com.mobitechs.classapp.screens.offlineDownload

// 1. MyDownloadsViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.data.repository.MyDownloadsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class MyDownloadsUiState(
    val isLoading: Boolean = false,
    val downloads: List<DownloadContent> = emptyList(),
    val groupedDownloads: Map<Int, List<DownloadContent>> = emptyMap(), // Grouped by courseId
    val totalSize: Long = 0L,
    val totalCourses: Int = 0,
    val error: String? = null,
    val expandedCourseIds: Set<Int> = emptySet()
)

class MyDownloadsViewModel(
    private val downloadRepository: MyDownloadsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyDownloadsUiState())
    val uiState: StateFlow<MyDownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            downloadRepository.getAllDownloads()
                .collect { downloadsList ->
                    // Group by course
                    val grouped = downloadsList.groupBy { it.course_id }

                    // Calculate total size using repository
                    val totalSize = downloadRepository.calculateTotalSize(downloadsList)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            downloads = downloadsList,
                            groupedDownloads = grouped,
                            totalSize = totalSize,
                            totalCourses = grouped.size,
                            error = null
                        )
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

    fun deleteDownload(download: DownloadContent) {
        viewModelScope.launch {
            try {
                downloadRepository.deleteDownload(download)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete: ${e.message}")
                }
            }
        }
    }

    fun deleteAllDownloads() {
        viewModelScope.launch {
            try {
                downloadRepository.deleteAllDownloads()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to clear downloads: ${e.message}")
                }
            }
        }
    }

    fun checkFileExists(download: DownloadContent): Boolean {
        return downloadRepository.checkFileExists(download.downloadedFilePath)
    }

    fun getFileSize(download: DownloadContent): Long {
        return downloadRepository.getFileSize(download.downloadedFilePath)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
