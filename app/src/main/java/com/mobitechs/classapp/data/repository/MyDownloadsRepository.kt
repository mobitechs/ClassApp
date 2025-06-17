package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.utils.SecureDownloadManager
import kotlinx.coroutines.flow.Flow
import java.io.File

class MyDownloadsRepository(
    private val database: AppDatabase,
    val secureDownloadManager: SecureDownloadManager // Made public for access
) {

    // Get all downloads from database
    fun getAllDownloads(): Flow<List<DownloadContent>> {
        return database.contentDao().getAllDownloads()
    }

    // Get download by content ID
    suspend fun getDownloadByContentId(contentId: Int): DownloadContent? {
        return database.contentDao().getDownloadByContentId(contentId)
    }

    // Start secure download
    suspend fun downloadContent(content: Content, course: Course) {
        secureDownloadManager.downloadContent(content, course)
    }

    // Cancel download
    fun cancelDownload(contentId: Int) {
        secureDownloadManager.cancelDownload(contentId)
    }

    // Check if content is downloaded
    suspend fun isContentDownloaded(contentId: Int): Boolean {
        return secureDownloadManager.isContentDownloaded(contentId)
    }

    // Check if content is downloading
    fun isDownloading(contentId: Int): Boolean {
        return secureDownloadManager.isDownloading(contentId)
    }

    // Delete single download
    suspend fun deleteDownload(download: DownloadContent) {
        secureDownloadManager.deleteDownload(download)
    }

    // Delete all downloads
    suspend fun deleteAllDownloads() {
        val allDownloads = database.contentDao().getAllDownloadsSync()

        // Delete all downloads using secure manager
        allDownloads.forEach { download ->
            secureDownloadManager.deleteDownload(download)
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

    // Clear temporary decrypted files
    fun clearTempFiles() {
        secureDownloadManager.clearTempFiles()
    }
}