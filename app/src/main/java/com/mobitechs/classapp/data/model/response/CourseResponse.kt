package com.mobitechs.classapp.data.model.response

data class CourseResponse(
    val courses: List<Course>,
    val message: String,
    val status: Boolean,
    val status_code: Int
)


data class Course(
    val id: Int,
    val course_name: String,
    val course_description: String,
    val image: String,
    val course_duration: Any,
    val course_expiry_date: Any,
    val course_price: String,
    val course_discounted_price: String,
    val offer_code: String,
    val course_like: Int,
    val course_tags: String,
    val isFavorite: Boolean,
    val isPurchased: Boolean,
    val isWishlisted: Boolean,
    val instructor: String,

    val category: CategoryItem,
    val course_category_id: Int,
    val sub_category: SubCategoryItem,
    val course_subcategory_id: Int,
    val subject: SubjectItem,
    val course_subject_id: Int,

    val is_active: String,

    val created_at: String,
    val updated_at: String,
    val deleted_at: Any,
    val added_by: Any,
)