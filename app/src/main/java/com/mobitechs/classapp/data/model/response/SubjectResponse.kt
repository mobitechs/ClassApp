package com.mobitechs.classapp.data.model.response

data class SubjectResponse(
    val message: String,
    val status: Boolean,
    val status_code: Int,
    val subjects: List<SubjectItem>
)


data class SubjectItem(
    val added_by: Any,
    val created_at: String,
    val deleted_at: Any?,
    val id: Int,
    val is_active: String,
    val name: String,
    val updated_at: String
)

