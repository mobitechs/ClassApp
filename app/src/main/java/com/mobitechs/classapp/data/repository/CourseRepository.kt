package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.response.Course

import com.mobitechs.classapp.data.model.response.CourseResponse
import com.mobitechs.classapp.data.model.response.NoticeBoardResponse
import com.mobitechs.classapp.data.model.response.OfferBannerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository(
    private val apiService: ApiService
) {

    suspend fun getCourses(
        categoryId: String? = null,
        subCategoryId: String? = null,
        searchQuery: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): List<Course> = withContext(Dispatchers.IO) {
        val response = apiService.getCourses(
            categoryId = categoryId,
            searchQuery = searchQuery,
            page = page,
            limit = limit
        )

        if (response.isSuccessful) {
            val courses = response.body() ?: emptyList()

            // Filter by subcategory if needed (client-side filtering)
            return@withContext if (subCategoryId != null) {
                courses.filter { it.course_subject_id == subCategoryId.toInt() }
            } else {
                courses
            }
        } else {
            throw Exception("Failed to get courses: ${response.message()}")
        }
    }


    suspend fun getCourseDetails(courseId: String): Course = withContext(Dispatchers.IO) {
        val response = apiService.getCourseDetails(courseId)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get course details: ${response.message()}")
        }
    }

    suspend fun getLatestCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getLatestCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }
    suspend fun getFeaturedCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getFeaturedCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getPopularCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getPopularCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getCoursesByCategory(categoryId: String): CourseResponse = withContext(Dispatchers.IO) {
//        val response = apiService.getCoursesByCategory(categoryId)
        val response = apiService.getLatestCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getCourseByCategorySubCategory(categoryId: String,subCategoryId: String): CourseResponse = withContext(Dispatchers.IO) {
//        val response = apiService.getCourseByCategorySubCategory(categoryId,subCategoryId)
        val response = apiService.getLatestCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }


    suspend fun getOfferBanners(): OfferBannerResponse = withContext(Dispatchers.IO) {
        val response = apiService.getOfferBanners()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get courses with offers: ${response.message()}")
        }
    }
    suspend fun getNoticeboard(): NoticeBoardResponse = withContext(Dispatchers.IO) {
        val response = apiService.getNoticeboard()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get courses with offers: ${response.message()}")
        }
    }


}
