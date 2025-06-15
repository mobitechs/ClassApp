package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.utils.SimpleDownloadManager
import kotlinx.coroutines.flow.Flow
import java.io.File

class MyDownloadsRepository(
    private val database: AppDatabase,
    private val downloadManager: SimpleDownloadManager
) {

    // Get all downloads from database
    fun getAllDownloads(): Flow<List<DownloadContent>> {
        return database.contentDao().getAllDownloads()
    }

    // Get download by content ID
    suspend fun getDownloadByContentId(contentId: Int): DownloadContent? {
        return database.contentDao().getDownloadByContentId(contentId)
    }

    // Start download
    suspend fun downloadContent(content: Content, course: Course) {
        downloadManager.downloadContent(content, course)
    }

    // Check if content is downloaded
    suspend fun isContentDownloaded(contentId: Int): Boolean {
        return downloadManager.isContentDownloaded(contentId)
    }

    // Delete single download
    suspend fun deleteDownload(download: DownloadContent) {
        // Delete file from storage
        deleteFileFromStorage(download.downloadedFilePath)

        // Delete from database
        database.contentDao().deleteDownload(download)
    }

    // Delete all downloads
    suspend fun deleteAllDownloads() {
        val allDownloads = database.contentDao().getAllDownloadsSync() // Need sync version

        // Delete all files
        allDownloads.forEach { download ->
            deleteFileFromStorage(download.downloadedFilePath)
        }

        // Delete all from database
        allDownloads.forEach { download ->
            database.contentDao().deleteDownload(download)
        }
    }

    // Calculate total size of all downloads
    fun calculateTotalSize(downloads: List<DownloadContent>): Long {
        return downloads.sumOf { download ->
            getFileSize(download.downloadedFilePath)
        }
    }

    // Get file size
    fun getFileSize(filePath: String?): Long {
        return filePath?.let { path ->
            try {
                File(path).length()
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }

    // Check if file exists
    fun checkFileExists(filePath: String?): Boolean {
        return filePath?.let { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    // Private helper to delete file
    private fun deleteFileFromStorage(filePath: String?) {
        filePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Log error but continue
            }
        }
    }
}
