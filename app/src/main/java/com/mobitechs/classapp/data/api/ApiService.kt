package com.mobitechs.classapp.data.api

import com.mobitechs.classapp.data.model.Batch
import com.mobitechs.classapp.data.model.Category
import com.mobitechs.classapp.data.model.Course
import com.mobitechs.classapp.data.model.NotificationItem
import com.mobitechs.classapp.data.model.OtpVerificationRequest
import com.mobitechs.classapp.data.model.OtpVerificationResponse
import com.mobitechs.classapp.data.model.PaymentResponse
import com.mobitechs.classapp.data.model.StudyMaterial

import com.mobitechs.classapp.data.model.response.LoginRequest
import com.mobitechs.classapp.data.model.response.LoginResponse
import com.mobitechs.classapp.data.model.response.RefreshTokenRequest
import com.mobitechs.classapp.data.model.response.RefreshTokenResponse
import com.mobitechs.classapp.data.model.response.RegisterRequest
import com.mobitechs.classapp.data.model.response.Student
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


    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): Response<OtpVerificationResponse>


    @GET("students/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<LoginResponse>


    @POST("user/profile")
    suspend fun updateUserProfile(@Body user: Student): Response<Student>

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


    @GET("getCategories")
    suspend fun getCategories(): Response<List<Category>>


    @GET("getSubcategories/{categoryId}")
    suspend fun getSubcategories(@Path("categoryId") categoryId: String): Response<List<Category>>


    @GET("getNotifications")
    suspend fun getNotifications(): Response<List<NotificationItem>>


    @GET("initiatePayment/{paymentId}")
    suspend fun initiatePayment(@Path("paymentId") paymentId: String): Response<PaymentResponse>

    @GET("verifyPayment/{paymentId}")
    suspend fun verifyPayment(
        @Path("paymentId") paymentId: String,
        @Path("orderId") orderId: String,
        @Path("signature") signature: String
    ): Response<PaymentResponse>


}