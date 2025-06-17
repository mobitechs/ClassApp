package com.mobitechs.classapp.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureDownloadManager(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track active downloads for cancellation
    private val activeDownloads = mutableMapOf<Int, Job>()

    // Encryption key - In production, store this securely in Android Keystore
    private val ENCRYPTION_KEY = "YourSecretKey123" // 16 characters for AES-128
    private val ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private val ENCRYPTION_ALGORITHM = "AES"

    // Progress callback
    var onProgressUpdate: ((contentId: Int, progress: Int) -> Unit)? = null
    var onDownloadComplete: ((contentId: Int, success: Boolean) -> Unit)? = null

    companion object {
        private const val SECURE_CONTENT_DIR = "secure_content"
        private const val BUFFER_SIZE = 4096
        private const val TAG = "SecureDownloadManager"
    }

    fun downloadContent(content: Content, course: Course) {
        // Cancel any existing download for this content
        cancelDownload(content.id)

        val downloadJob = scope.launch {
            try {
                Log.d(TAG, "Starting download for content ID: ${content.id}")

                // First save to Room DB
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
                    is_free = content.is_free ?: "",
                    is_offline_available = content.is_offline_available ?: "",
                    downloadedFilePath = null,
                    isDownloaded = false,
                    downloadedAt = 0,
                    course_tags = course.course_tags ?: ""
                )
                database.contentDao().insertDownload(downloadedContent)

                // Start secure download
                downloadAndEncrypt(content)

            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Download cancelled for content ID: ${content.id}")
                    // Clean up partial download
                    cleanupPartialDownload(content.id)
                } else {
                    Log.e(TAG, "Error starting download", e)
                }
                onDownloadComplete?.invoke(content.id, false)
            }
        }

        activeDownloads[content.id] = downloadJob
    }

    fun cancelDownload(contentId: Int) {
        activeDownloads[contentId]?.cancel()
        activeDownloads.remove(contentId)

        // Clean up partial download
        scope.launch {
            cleanupPartialDownload(contentId)
        }
    }

    private suspend fun cleanupPartialDownload(contentId: Int) {
        try {
            val secureDir = File(context.filesDir, SECURE_CONTENT_DIR)
            val partialFile = File(secureDir, "content_${contentId}_encrypted.*")
            if (partialFile.exists()) {
                partialFile.delete()
            }

            // Remove from database if not completed
            val download = database.contentDao().getDownloadByContentId(contentId)
            if (download != null && !download.isDownloaded) {
                database.contentDao().deleteDownload(download)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up partial download", e)
        }
    }

    private suspend fun downloadAndEncrypt(content: Content) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading and encrypting content: ${content.content_url}")

            val url = URL(content.content_url)
            val connection = url.openConnection()
            connection.connect()

            val fileSize = connection.contentLength
            val inputStream = connection.getInputStream()

            // Create secure directory in app's private storage
            val secureDir = File(context.filesDir, SECURE_CONTENT_DIR)
            if (!secureDir.exists()) {
                secureDir.mkdirs()
            }

            // Generate unique filename
            val fileName = "content_${content.id}_encrypted.${getExtension(content.content_type)}"
            val outputFile = File(secureDir, fileName)

            Log.d(TAG, "Saving to: ${outputFile.absolutePath}")

            // Setup encryption
            val key = generateKey()
            val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv

            // Save IV for decryption (prepend to file)
            val fos = FileOutputStream(outputFile)
            fos.write(iv)

            // Encrypt and write data
            val cos = CipherOutputStream(fos, cipher)
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            var totalBytesRead = 0

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                // Check if cancelled
                if (!isActive) {
                    throw CancellationException("Download cancelled")
                }

                cos.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                // Update progress
                val progress = if (fileSize > 0) {
                    ((totalBytesRead.toFloat() / fileSize) * 100).toInt()
                } else {
                    0
                }

                withContext(Dispatchers.Main) {
                    onProgressUpdate?.invoke(content.id, progress)
                }
            }

            cos.close()
            inputStream.close()

            Log.d(TAG, "Download complete. File size: ${outputFile.length()}")

            // Update database with encrypted file path
            database.contentDao().updateDownloadComplete(
                content.id,
                outputFile.absolutePath
            )

            activeDownloads.remove(content.id)

            withContext(Dispatchers.Main) {
                onDownloadComplete?.invoke(content.id, true)
            }

        } catch (e: Exception) {
            activeDownloads.remove(content.id)
            if (e is CancellationException) {
                throw e
            }
            Log.e(TAG, "Error downloading/encrypting", e)
            withContext(Dispatchers.Main) {
                onDownloadComplete?.invoke(content.id, false)
            }
        }
    }

    fun getDecryptedFile(downloadedContent: DownloadContent): File? {
        return try {
            val encryptedFile = File(downloadedContent.downloadedFilePath ?: return null)
            if (!encryptedFile.exists()) {
                Log.e(TAG, "Encrypted file not found: ${encryptedFile.absolutePath}")
                return null
            }

            Log.d(TAG, "Decrypting file: ${encryptedFile.absolutePath}")

            // Create temp file for decrypted content
            val tempDir = File(context.cacheDir, "temp_content")
            if (!tempDir.exists()) tempDir.mkdirs()

            val tempFile = File(tempDir, "temp_${downloadedContent.id}.${getExtension(downloadedContent.content_type)}")

            decryptFile(encryptedFile, tempFile)

            Log.d(TAG, "Decryption complete. Temp file: ${tempFile.absolutePath}")
            tempFile

        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting file", e)
            null
        }
    }

    fun getDecryptedUri(downloadedContent: DownloadContent): Uri? {
        val file = getDecryptedFile(downloadedContent)
        return file?.let { Uri.fromFile(it) }
    }

    private fun decryptFile(encryptedFile: File, outputFile: File) {
        val fis = encryptedFile.inputStream()

        // Read IV from file
        val iv = ByteArray(16)
        fis.read(iv)

        // Setup decryption
        val key = generateKey()
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

        // Decrypt file
        val cis = CipherInputStream(fis, cipher)
        val fos = FileOutputStream(outputFile)

        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int

        while (cis.read(buffer).also { bytesRead = it } != -1) {
            fos.write(buffer, 0, bytesRead)
        }

        fos.close()
        cis.close()
    }

    private fun generateKey(): SecretKey {
        // In production, use Android Keystore for secure key storage
        val keyBytes = ENCRYPTION_KEY.toByteArray(Charsets.UTF_8)
        return SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)
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
        val download = database.contentDao().getDownloadByContentId(contentId)
        return if (download?.isDownloaded == true && download.downloadedFilePath != null) {
            // Check if file actually exists
            val exists = File(download.downloadedFilePath).exists()
            Log.d(TAG, "Content $contentId downloaded: $exists")
            exists
        } else {
            false
        }
    }

    fun getAllDownloads(): Flow<List<DownloadContent>> {
        return database.contentDao().getAllDownloads()
    }

    suspend fun deleteDownload(download: DownloadContent) {
        // Cancel if downloading
        cancelDownload(download.id)

        // Delete encrypted file
        download.downloadedFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Deleted encrypted file: $path")
            }
        }

        // Delete from database
        database.contentDao().deleteDownload(download)
    }

    fun clearTempFiles() {
        // Clear temporary decrypted files
        val tempDir = File(context.cacheDir, "temp_content")
        if (tempDir.exists()) {
            tempDir.listFiles()?.forEach {
                it.delete()
                Log.d(TAG, "Deleted temp file: ${it.name}")
            }
        }
    }

    fun isDownloading(contentId: Int): Boolean {
        return activeDownloads.containsKey(contentId)
    }
}