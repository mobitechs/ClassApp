package com.mobitechs.classapp.data.api

import com.mobitechs.classapp.data.model.Batch
import com.mobitechs.classapp.data.model.PaymentResponse
import com.mobitechs.classapp.data.model.StudyMaterial
import com.mobitechs.classapp.data.model.request.CommonCourseRequest
import com.mobitechs.classapp.data.model.request.GetCourseByRequest
import com.mobitechs.classapp.data.model.request.LoginRequest
import com.mobitechs.classapp.data.model.request.RefreshTokenRequest
import com.mobitechs.classapp.data.model.request.RegisterRequest
import com.mobitechs.classapp.data.model.response.CategoryResponse
import com.mobitechs.classapp.data.model.response.CommonResponse
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.CourseContentResponse
import com.mobitechs.classapp.data.model.response.CourseResponse
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.NoticeBoardResponse
import com.mobitechs.classapp.data.model.response.NotificationsResponse
import com.mobitechs.classapp.data.model.response.OfferBannerResponse
import com.mobitechs.classapp.data.model.response.RefreshTokenResponse
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.data.model.response.SubCategoryResponse
import com.mobitechs.classapp.data.model.response.SubjectResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth endpoints
    @POST("student/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("student/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("students/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<LoginResponse>

    @POST("user/profile")
    suspend fun updateUserProfile(@Body user: Student): Response<Student>


    //Home

    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @GET("offer-banners")
    suspend fun getOfferBanners(): Response<OfferBannerResponse>

    @GET("noticeboard")
    suspend fun getNoticeboard(): Response<NoticeBoardResponse>


    //Courses ---------------------------------------------------------------------------------------
    @POST("courses-with-filters")
    suspend fun getCourses(@Body request: GetCourseByRequest): Response<CourseResponse>

    @GET("courses/{id}")
    suspend fun getCourseDetails(@Path("id") courseId: Int): Response<Course>

    @GET("content/{id}")
    suspend fun getCourseContent(@Path("id") courseId: Int): Response<CourseContentResponse>

    @GET("free-content")
    suspend fun getFreeContent(): Response<CourseContentResponse>


    //Courses Fav ---------------------------------------------------------------------------------------
    @POST("favourite-courses")
    suspend fun addToFavorite(@Body request: CommonCourseRequest): Response<CommonResponse>

    @GET("favourite-courses")
    suspend fun getAllFavoriteCourses(): Response<CourseResponse>


    @DELETE("remove-favourite-courses/{studentId}/{courseId}")
    suspend fun removeFromFavorite(
        @Path("studentId") studentId: String,
        @Path("courseId") courseId: String
    ): Response<CommonResponse>

    //Courses wishlist---------------------------------------------------------------------------------------
    @POST("whishlist-courses")
    suspend fun addToWishlist(@Body request: CommonCourseRequest): Response<CommonResponse>

    @GET("wishlist-courses")
    suspend fun getAllWishlistCourses(): Response<CourseResponse>

    @DELETE("remove-whishlist-courses/{studentId}/{courseId}")
    suspend fun removeFromWishlist(
        @Path("studentId") studentId: String,
        @Path("courseId") courseId: String
    ): Response<CommonResponse>

    //Courses like---------------------------------------------------------------------------------------
    @POST("like-courses")
    suspend fun likeCourse(@Body request: CommonCourseRequest): Response<CommonResponse>

    @DELETE("unlike-courses/{studentId}/{courseId}")
    suspend fun dislikeCourse(
        @Path("studentId") studentId: String,
        @Path("courseId") courseId: String
    ): Response<CommonResponse>

    //Category ---------------------------------------------------------------------------------------
    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("sub-categories")
    suspend fun getAllSubCategories(): Response<SubCategoryResponse>

    @GET("sub-categories/{categoryId}")
    suspend fun getSubCategoryByCategory(@Path("categoryId") categoryId: Int): Response<SubCategoryResponse>

    @GET("subjects")
    suspend fun getAllSubject(): Response<SubjectResponse>

    @GET("subjects/{categoryId}")
    suspend fun getSubjectByCategory(@Path("categoryId") categoryId: Int): Response<SubjectResponse>

    @GET("subjects/{subCategoryId}")
    suspend fun getSubjectsBySubcategory(@Path("subCategoryId") categoryId: Int): Response<SubjectResponse>


    //Batches endpoints  ---------------------------------------------------------------------------------------
    @GET("batches")
    suspend fun getUserBatches(): Response<List<Batch>>

    @POST("batches/join")
    suspend fun joinBatchByCode(@Query("code") batchCode: String): Response<Batch>

    @GET("batches/{id}/materials")
    suspend fun getBatchMaterials(
        @Path("id") batchId: String,
        @Query("type") type: String? = null,
        @Query("query") searchQuery: String? = null,
        @Query("filter") filter: String? = null
    ): Response<List<StudyMaterial>>


    //payment ---------------------------------------------------------------------------------------
    @GET("initiatePayment/{paymentId}")
    suspend fun initiatePayment(@Path("paymentId") paymentId: String): Response<PaymentResponse>

    @GET("verifyPayment/{paymentId}")
    suspend fun verifyPayment(
        @Path("paymentId") paymentId: String,
        @Path("orderId") orderId: String,
        @Path("signature") signature: String
    ): Response<PaymentResponse>


}