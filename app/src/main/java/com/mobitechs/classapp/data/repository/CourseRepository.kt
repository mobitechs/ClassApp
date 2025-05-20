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
        // This is simplified - in a real app, you would have a specific API endpoint for featured courses
        val response = apiService.getLatestCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }
    suspend fun getFeaturedCourses(): List<Course> = withContext(Dispatchers.IO) {
        // This is simplified - in a real app, you would have a specific API endpoint for featured courses
        val response = apiService.getCourses(popular = true, limit = 5)
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getPopularCourses(): List<Course> = withContext(Dispatchers.IO) {
        // This is simplified - in a real app, you would have a specific API endpoint for popular courses
        val response = apiService.getCourses(popular = true, limit = 10)
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get popular courses: ${response.message()}")
        }
    }


    suspend fun getOfferBanners(): OfferBannerResponse = withContext(Dispatchers.IO) {
        // This is simplified - in a real app, you would have a specific API endpoint for courses with offers
        val response = apiService.getOfferBanners()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get courses with offers: ${response.message()}")
        }
    }
    suspend fun getNoticeboard(): NoticeBoardResponse = withContext(Dispatchers.IO) {
        // This is simplified - in a real app, you would have a specific API endpoint for courses with offers
        val response = apiService.getNoticeboard()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get courses with offers: ${response.message()}")
        }
    }


    // Simplified banner API - in real implementation, this would be a separate endpoint
    suspend fun getBanners(): List<String> = withContext(Dispatchers.IO) {
        // Returning hardcoded banner URLs for demonstration
        return@withContext listOf(
            "https://example.com/banner1.jpg",
            "https://example.com/banner2.jpg",
            "https://example.com/banner3.jpg"
        )
    }

    // Simplified toggle favorite function
    suspend fun toggleFavorite(courseId: String): Boolean {
        // In a real implementation, this would make an API call
        return true // Returns new favorite state
    }

    // Simplified toggle wishlist function
    suspend fun toggleWishlist(courseId: String): Boolean {
        // In a real implementation, this would make an API call
        return true // Returns new wishlist state
    }
}
