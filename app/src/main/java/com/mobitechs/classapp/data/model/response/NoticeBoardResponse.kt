package com.mobitechs.classapp.data.model.response

data class NoticeBoardResponse(
    val message: String,
    val notice: List<Notice>,
    val status: Boolean,
    val status_code: Int
)

data class Notice(
    val id: Int,
    val notice_title: String,
    val description: String,
    val offer_code: String,
    val url: String,
    val is_course: String,
    val course_id: Int,
    val course: Course,

    val is_active: String,
    val created_at: String,
    val deleted_at: Any,
    val updated_at: String,
    val added_by: Any,
)