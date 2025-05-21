package com.mobitechs.classapp


import android.app.Application
import com.google.gson.Gson
import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.api.AuthInterceptor
import com.mobitechs.classapp.data.api.RetrofitClient
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.BatchRepository
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.FreeContentRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import com.mobitechs.classapp.data.repository.OfflineDownloadRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import com.mobitechs.classapp.data.repository.PolicyTermConditionRepository
import com.mobitechs.classapp.data.repository.SearchRepository
import com.mobitechs.classapp.data.repository.UserRepository
import com.razorpay.Checkout

class ClassConnectApp : Application() {

    // Lazily instantiated dependencies
    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(applicationContext, gson) }
    val authInterceptor by lazy { AuthInterceptor(sharedPrefsManager) }
    val apiService by lazy {
        RetrofitClient.getRetrofitInstance(authInterceptor).create(ApiService::class.java)
    }

    // Repositories
    val authRepository by lazy { AuthRepository(apiService, sharedPrefsManager) }
    val userRepository by lazy { UserRepository(apiService, sharedPrefsManager) }
    val courseRepository by lazy { CourseRepository(apiService) }
    val batchRepository by lazy { BatchRepository(apiService) }
    val categoryRepository by lazy { CategoryRepository(apiService) }
    val notificationRepository by lazy { NotificationRepository(apiService) }
    val paymentRepository by lazy { PaymentRepository(apiService) }
    val freeContentRepository by lazy { FreeContentRepository(apiService) }
    val offlineDownloadRepository by lazy { OfflineDownloadRepository(apiService) }
    val searchRepository by lazy { SearchRepository(apiService) }
    val policyTermConditionRepository by lazy { PolicyTermConditionRepository(apiService) }

    override fun onCreate() {
        super.onCreate()

        // Initialize Razorpay SDK
        Checkout.preload(applicationContext)

    }


}