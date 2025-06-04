package com.mobitechs.classapp.data.model.request

data class GetCourseByRequest (
    val categoryId: Int? = 0,
    val subCategoryId: Int? = 0,
    val subjectId: Int? = 0,
    val sort: String? = ""
)