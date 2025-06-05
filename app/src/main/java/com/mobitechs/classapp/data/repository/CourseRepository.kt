package com.mobitechs.classapp.data.repository


import android.util.Log
import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.dao.CourseDao
import com.mobitechs.classapp.data.model.request.CommonCourseRequest
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
import com.mobitechs.classapp.data.model.response.CommonResponse
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.CourseResponse
import com.mobitechs.classapp.data.model.response.NoticeBoardResponse
import com.mobitechs.classapp.data.model.response.OfferBannerResponse
import com.mobitechs.classapp.data.model.response.SubjectResponse
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

    suspend fun getCoursesFilterWise(reqObj: GetCourseByRequest): CourseResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getCourses(reqObj)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get courses: ${response.message()}")
            }
        }






    suspend fun getOfferBanners(): OfferBannerResponse = withContext(Dispatchers.IO) {
        val response = apiService.getOfferBanners()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get Offer Banner: ${response.message()}")
        }
    }

    suspend fun getNoticeboard(): NoticeBoardResponse = withContext(Dispatchers.IO) {
        val response = apiService.getNoticeboard()
        if (response.isSuccessful) {
            // Filter courses that have a discounted price
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get Notice board: ${response.message()}")
        }
    }


    suspend fun getAllFavoriteCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getAllFavoriteCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get Favorite courses: ${response.message()}")
        }
    }

    suspend fun getAllWishlistCourses(): CourseResponse = withContext(Dispatchers.IO) {
        val response = apiService.getAllWishlistCourses()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get wishListed courses: ${response.message()}")
        }
    }

    suspend fun addToFavorite(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.addToFavorite(CommonCourseRequest(userId.toString(),courseId.toString()))
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to add to favorite: ${response.message()}")
            }
        }

    suspend fun removeFromFavorite(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.removeFromFavorite(userId.toString(),courseId.toString())
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to remove from favorite: ${response.message()}")
            }
        }

    suspend fun addToWishlist(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.addToWishlist(CommonCourseRequest(userId.toString(),courseId.toString()))
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to add to wishlist: ${response.message()}")
            }
        }

    suspend fun removeFromWishlist(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.removeFromWishlist(userId.toString(),courseId.toString())
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to remove from wishlist: ${response.message()}")
            }
        }

    suspend fun likeCourse(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.likeCourse(CommonCourseRequest(userId.toString(),courseId.toString()))
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
            }
        }

    suspend fun dislikeCourse(courseId: Int): CommonResponse =
        withContext(Dispatchers.IO) {
            val userId = sharedPrefsManager.getUser()?.id.toString()
            val response = apiService.dislikeCourse(userId.toString(),courseId.toString())
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
            }
        }




}
