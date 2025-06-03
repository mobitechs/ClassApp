package com.mobitechs.classapp


import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.api.AuthInterceptor
import com.mobitechs.classapp.data.api.RetrofitClient
import com.mobitechs.classapp.data.local.AppDatabase
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
import kotlin.getValue
import androidx.work.*
import com.mobitechs.classapp.utils.SyncWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ClassConnectApp : Application() {

    // Lazily instantiated dependencies
    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(applicationContext, gson) }
    val authInterceptor by lazy { AuthInterceptor(sharedPrefsManager) }
    val apiService by lazy {
        RetrofitClient.getRetrofitInstance(authInterceptor).create(ApiService::class.java)
    }

    // Add database instance
    val database by lazy { AppDatabase.getDatabase(this) }

    lateinit var appContext: Context

    // Repositories
    val authRepository by lazy { AuthRepository(apiService, sharedPrefsManager) }
    val userRepository by lazy { UserRepository(apiService, sharedPrefsManager) }
    val courseRepository by lazy { CourseRepository(apiService,sharedPrefsManager,database.courseDao()) }
    val categoryRepository by lazy { CategoryRepository(apiService,sharedPrefsManager,database.categoryDao(),database.subCategoryDao(),database.subjectDao()) }
    val notificationRepository by lazy { NotificationRepository(apiService) }
    val paymentRepository by lazy { PaymentRepository(apiService) }
    val freeContentRepository by lazy { FreeContentRepository(apiService) }
    val offlineDownloadRepository by lazy { OfflineDownloadRepository(apiService) }
    val searchRepository by lazy { SearchRepository(apiService) }
    val batchRepository by lazy { BatchRepository(apiService) }
    val policyTermConditionRepository by lazy { PolicyTermConditionRepository(apiService) }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        // Initialize Razorpay SDK
        Checkout.preload(applicationContext)

        // Schedule daily sync
        scheduleDailySync()

    }

    private fun scheduleDailySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        // Also run an immediate one-time sync on app launch
        val immediateSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "immediate_sync",
            ExistingWorkPolicy.REPLACE,
            immediateSyncRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        // Schedule for 2 AM
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If 2 AM has passed today, schedule for tomorrow
            if (timeInMillis <= currentTime) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis - currentTime
    }


}