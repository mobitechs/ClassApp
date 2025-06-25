package com.mobitechs.classapp

import android.app.Application
import android.content.Context
import android.util.Log
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
import com.mobitechs.classapp.data.repository.MyDownloadsRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import com.mobitechs.classapp.data.repository.PolicyTermConditionRepository
import com.mobitechs.classapp.data.repository.ProfileRepository
import com.mobitechs.classapp.data.repository.SearchRepository
import com.mobitechs.classapp.data.repository.ThemeRepository
import com.mobitechs.classapp.data.repository.UserRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
import com.mobitechs.classapp.data.repository.chat.MessageRepository
import com.mobitechs.classapp.utils.SecureDownloadManager
import com.razorpay.Checkout


class ClassConnectApp : Application() {

    companion object {
        private const val TAG = "ClassConnectApp"
        private const val PREFS_FRESH_INSTALL = "fresh_install_prefs"
        private const val KEY_INSTALL_ID = "install_id"
    }

    // Lazily instantiated dependencies
    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(applicationContext, gson) }
    val authInterceptor by lazy { AuthInterceptor(sharedPrefsManager) }
    val apiService by lazy {
        RetrofitClient.getRetrofitInstance(authInterceptor).create(ApiService::class.java)
    }

    // Create download repository instance


    // Add database instance
    val database by lazy { AppDatabase.getDatabase(this) }

    lateinit var appContext: Context

    val myDownloadsRepository: MyDownloadsRepository by lazy {
        val database = AppDatabase.getDatabase(appContext)
//        val downloadManager = SimpleDownloadManager(appContext)
        val downloadManager = SecureDownloadManager(appContext)
        MyDownloadsRepository(database, downloadManager)
    }


    // Repositories
    val themeRepository by lazy { ThemeRepository(appContext) }
    val authRepository by lazy { AuthRepository(apiService, sharedPrefsManager) }
    val userRepository by lazy { UserRepository(apiService, sharedPrefsManager) }
    val courseRepository by lazy {
        CourseRepository(
            apiService,
            sharedPrefsManager,
            database.courseDao()
        )
    }
    val categoryRepository by lazy {
        CategoryRepository(
            apiService,
            sharedPrefsManager,
            database.categoryDao(),
            database.subCategoryDao(),
            database.subjectDao()
        )
    }
    val notificationRepository by lazy { NotificationRepository(apiService) }
    val paymentRepository by lazy { PaymentRepository(apiService) }
    val freeContentRepository by lazy { FreeContentRepository(apiService) }

    //    val myDownloadsRepository by lazy { MyDownloadsRepository(apiService) }
    val searchRepository by lazy { SearchRepository(apiService, sharedPrefsManager) }
    val batchRepository by lazy { BatchRepository(apiService) }
    val policyTermConditionRepository by lazy { PolicyTermConditionRepository(apiService) }
    val chatUserRepository by lazy { ChatUserRepository(database.chatUserDao()) }
    val chatRepository by lazy { ChatRepository(database.chatDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao()) }
    val profileRepository by lazy { ProfileRepository(apiService, sharedPrefsManager) }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Check for fresh install and clear data if needed
        checkAndHandleFreshInstall()

        // Initialize Razorpay SDK
        Checkout.preload(applicationContext)
    }

    private fun checkAndHandleFreshInstall() {
        try {
            val prefs = getSharedPreferences(PREFS_FRESH_INSTALL, Context.MODE_PRIVATE)
            val packageInfo = packageManager.getPackageInfo(packageName, 0)

            // Use first install time as unique identifier
            val currentInstallId = packageInfo.firstInstallTime.toString()
            val savedInstallId = prefs.getString(KEY_INSTALL_ID, null)

            if (savedInstallId == null || savedInstallId != currentInstallId) {
                Log.d(TAG, "Fresh install detected. Clearing all data...")

                // This is a fresh install or reinstall
                clearAllAppData()

                // Save the new install ID
                prefs.edit().putString(KEY_INSTALL_ID, currentInstallId).apply()

                Log.d(TAG, "All data cleared for fresh install")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking fresh install", e)
        }
    }

    private fun clearAllAppData() {
        // Clear all SharedPreferences
        clearAllSharedPreferences()

        // Clear all databases
        clearAllDatabases()

        // Clear cache
        clearCache()

        // Clear internal storage
        clearInternalStorage()
    }

    private fun clearAllSharedPreferences() {
        try {
            val prefsDir = java.io.File(applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { file ->
                    // Don't clear the fresh install tracking preferences
                    if (file.name != "$PREFS_FRESH_INSTALL.xml") {
                        val prefName = file.name.removeSuffix(".xml")
                        getSharedPreferences(prefName, Context.MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing shared preferences", e)
        }
    }

    private fun clearAllDatabases() {
        try {
            // Clear Room database
            AppDatabase.destroyInstance()

            // Delete all database files
            val databaseList = databaseList()
            for (database in databaseList) {
                deleteDatabase(database)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing databases", e)
        }
    }

    private fun clearCache() {
        try {
            cacheDir.deleteRecursively()
            codeCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    private fun clearInternalStorage() {
        try {
            filesDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing internal storage", e)
        }
    }
}