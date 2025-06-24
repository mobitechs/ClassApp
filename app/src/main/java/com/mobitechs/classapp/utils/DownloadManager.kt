package com.mobitechs.classapp.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SimpleDownloadManager(private val context: Context) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun downloadContent(content: Content, course: Course) {
        // First save to Room DB
        scope.launch {
            val downloadedContent = DownloadContent(
                id = content.id,
                content_type = content.content_type,
                content_url = content.content_url,
                course_id = content.course_id,
                course_name = course.course_name,
                course_description = course.course_description,
                category_name = course.category_name,
                sub_category_name = course.sub_category_name,
                subject_name = course.subject_name,
                is_free = "",
                is_offline_available = "",
                downloadedFilePath = null,
                isDownloaded = false,
                downloadedAt = 0,
                course_tags = ""
            )
            database.contentDao().insertDownload(downloadedContent)

            // Now start download
            startDownload(content)
        }
    }

    private fun startDownload(content: Content) {
        val fileName = "content_${content.id}.${getExtension(content.content_type)}"

        val request = DownloadManager.Request(Uri.parse(content.content_url))
            .setTitle("Downloading ${content.content_type}")
            .setDescription("ClassApp Content")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "ClassApp/$fileName"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)

        // Monitor download completion (simplified version)
        monitorDownload(downloadId, content.id)
    }

    private fun monitorDownload(downloadId: Long, contentId: Int) {
        // In a real app, you'd use a BroadcastReceiver or WorkManager
        // For simplicity, we'll just mark it as downloaded
        scope.launch {
            // Wait a bit (in real app, monitor actual download)
            delay(2000)

            // Update Room DB with file path
            val filePath =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/ClassApp/content_$contentId.mp4"
            database.contentDao().updateDownloadComplete(contentId, filePath)
        }
    }

    private fun getExtension(contentType: String?): String {
        return when (contentType?.uppercase()) {
            "VIDEO" -> "mp4"
            "AUDIO" -> "mp3"
            "PDF" -> "pdf"
            else -> "file"
        }
    }

    suspend fun isContentDownloaded(contentId: Int): Boolean {
        return database.contentDao().getDownloadByContentId(contentId)?.isDownloaded ?: false
    }

    fun getAllDownloads() = database.contentDao().getAllDownloads()
}