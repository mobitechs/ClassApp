package com.mobitechs.classapp.data.repository


import android.util.Log
import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.dao.CourseDao
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.CourseResponse
import com.mobitechs.classapp.data.model.response.NoticeBoardResponse
import com.mobitechs.classapp.data.model.response.OfferBannerResponse
import com.mobitechs.classapp.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager,
    private val courseDao: CourseDao
) {


    suspend fun getCourseDetails(courseId: String): Course = withContext(Dispatchers.IO) {
        val response = apiService.getCourseDetails(courseId)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get course details: ${response.message()}")
        }
    }

    suspend fun getCourses(categoryId: Int = 0, subcategoryId: Int = 0, subjectId: Int = 0): CourseResponse = withContext(Dispatchers.IO) {
        // Check if already called API today
        if (sharedPrefsManager.isAlreadySyncedToday(Constants.KEY_tbl_course)) {
            Log.d("Course API", "Course Already called API today cat =$categoryId subCat = $subcategoryId subject = $subjectId")
            // Get from Room DB
            return@withContext CourseResponse(
                courses = courseDao.getCourses(categoryId,subcategoryId,subjectId),
                message = "Courses from RoomDb",
                status = true,
                status_code = 200
            )
        } else {
            val response = apiService.getCourses()
            if (response.isSuccessful) {
                val courseResponse = response.body() ?: throw Exception("Empty response body")
                // Save to Room DB
                courseDao.insertCourses(courseResponse.courses)
                sharedPrefsManager.setLastSyncDate(Constants.KEY_tbl_course)

                return@withContext courseResponse
            } else {
                throw Exception("Failed to get courses: ${response.message()}")
                // you can cache data from room db here and return
            }
        }

    }

    suspend fun getLatestCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getLatestCourses()
        if (response.isSuccessful) {
            Log.d("Course API", "Latest Course Already called API today")
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getFeaturedCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getFeaturedCourses()
        if (response.isSuccessful) {
            Log.d("Course API", "featured Course Already called API today")
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getPopularCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getPopularCourses()
        if (response.isSuccessful) {
            Log.d("Course API", "popular Course Already called API today")
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }

    suspend fun getCoursesByCategory(categoryId: String): CourseResponse =
        withContext(Dispatchers.IO) {
        val response = apiService.getCoursesByCategory(categoryId)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get featured courses: ${response.message()}")
            }
        }

    suspend fun getCourseByCategorySubCategory(
        categoryId: String,
        subCategoryId: String
    ): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getCourseByCategorySubCategory(categoryId,subCategoryId)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get featured courses: ${response.message()}")
        }
    }
    suspend fun getCoursesByCategorySubCategorySubject(
        categoryId: String,
        subCategoryId: String,
        subjectId: String
    ): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getCoursesByCategorySubCategorySubject(categoryId,subCategoryId,subjectId)
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
