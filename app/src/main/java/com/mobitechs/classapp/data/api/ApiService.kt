package com.mobitechs.classapp.data.api

import com.mobitechs.classapp.data.model.Batch
import com.mobitechs.classapp.data.model.PaymentResponse
import com.mobitechs.classapp.data.model.StudyMaterial
import com.mobitechs.classapp.data.model.response.CategoryResponse
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.CourseResponse
import com.mobitechs.classapp.data.model.response.LoginRequest
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.NoticeBoardResponse
import com.mobitechs.classapp.data.model.response.NotificationItem
import com.mobitechs.classapp.data.model.response.NotificationsResponse
import com.mobitechs.classapp.data.model.response.OfferBannerResponse
import com.mobitechs.classapp.data.model.response.RefreshTokenRequest
import com.mobitechs.classapp.data.model.response.RefreshTokenResponse
import com.mobitechs.classapp.data.model.response.RegisterRequest
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.data.model.response.SubCategoryResponse
import com.mobitechs.classapp.data.model.response.SubjectResponse
import retrofit2.Response
import retrofit2.http.Body
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


    //Courses
    @GET("courses?sort=newest")
    suspend fun getCourses(): Response<CourseResponse>

    @GET("courses?sort=newest")
    suspend fun getLatestCourses(): Response<CourseResponse>

    @GET("courses?sort=newest")
    suspend fun getPopularCourses(): Response<CourseResponse>


    @GET("courses?sort=newest")
    suspend fun getFeaturedCourses(): Response<CourseResponse>

    @GET("categorywiseCourses/{categoryId}")
    suspend fun getCoursesByCategory(@Path("categoryId") categoryId: String): Response<CourseResponse>

    @GET("categorywiseCourses/{categoryId}/{subCategoryId}")
    suspend fun getCourseByCategorySubCategory(@Path("categoryId") categoryId: String, @Path("subCategoryId") subCategoryId: String): Response<CourseResponse>


    @GET("categorywiseCourses/{categoryId}/{subCategoryId}/{subjectId}")
    suspend fun getCoursesByCategorySubCategorySubject(@Path("categoryId") categoryId: String, @Path("subCategoryId") subCategoryId: String, @Path("subjectId") subjectId: String): Response<CourseResponse>




    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("sub-categories")
    suspend fun getAllSubCategories(): Response<SubCategoryResponse>

    @GET("sub-categories/{categoryId}")
    suspend fun getSubCategoryByCategory(@Path("categoryId") categoryId: String): Response<SubCategoryResponse>

    @GET("subjects")
    suspend fun getAllSubject(): Response<SubjectResponse>

    @GET("subjects/{categoryId}")
    suspend fun getSubjectByCategory(@Path("categoryId") categoryId: String): Response<SubjectResponse>

    @GET("subjects/{subCategoryId}")
    suspend fun getSubjectsBySubcategory(@Path("subCategoryId") categoryId: String): Response<SubjectResponse>


    // Courses
    @GET("courses")
    suspend fun getCourses(
        @Query("category") categoryId: String? = null,
        @Query("query") searchQuery: String? = null,
        @Query("popular") popular: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<Course>>

    @GET("courses/{id}")
    suspend fun getCourseDetails(@Path("id") courseId: String): Response<Course>

    // Batches endpoints
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


    @GET("initiatePayment/{paymentId}")
    suspend fun initiatePayment(@Path("paymentId") paymentId: String): Response<PaymentResponse>

    @GET("verifyPayment/{paymentId}")
    suspend fun verifyPayment(
        @Path("paymentId") paymentId: String,
        @Path("orderId") orderId: String,
        @Path("signature") signature: String
    ): Response<PaymentResponse>


}