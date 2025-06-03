package com.mobitechs.classapp.data.model.response

data class FreeContentResponse(
    val content: List<Content>,
    val message: String,
    val status: Boolean,
    val status_code: Int
)


data class Content(
    val id: Int,
    val added_by: String?,
    val content_type: String,
    val content_url: String,
    val course_id: Int,
    val created_at: String?,
    val deleted_at: String?,
    val is_active: String,
    val is_free: String?,
    val is_offline_available: String?,
    val offer_code: String?,
    val updated_at: String?
)