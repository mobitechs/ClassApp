package com.mobitechs.classapp.data.model

import java.util.Date

data class StudyMaterial(
    val id: String,
    val batchId: String,
    val title: String,
    val description: String? = null,
    val type: String, // VIDEO, PDF, etc.
    val url: String,
    val fileSize: Long? = null,
    val duration: Long? = null, // For video files (in seconds)
    val thumbnailUrl: String? = null,
    val isDownloaded: Boolean = false,
    val createdAt: Date
)