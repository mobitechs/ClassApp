
package com.mobitechs.classapp.data.model.dao

import androidx.room.*
import com.mobitechs.classapp.data.model.response.DownloadContent
import kotlinx.coroutines.flow.Flow



@Dao
interface DownloadContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadContent)

    @Query("SELECT * FROM downloaded_content WHERE isDownloaded = 1")
    fun getAllDownloads(): Flow<List<DownloadContent>>

    @Query("SELECT * FROM downloaded_content WHERE isDownloaded = 1")
    suspend fun getAllDownloadsSync(): List<DownloadContent> // Add this for deleteAll

    @Query("SELECT * FROM downloaded_content WHERE id = :contentId")
    suspend fun getDownloadByContentId(contentId: Int): DownloadContent?

    @Query("UPDATE downloaded_content SET isDownloaded = 1, downloadedFilePath = :filePath WHERE id = :contentId")
    suspend fun updateDownloadComplete(contentId: Int, filePath: String)

    @Delete
    suspend fun deleteDownload(download: DownloadContent)

    @Query("DELETE FROM downloaded_content")
    suspend fun deleteAllDownloads()
}
