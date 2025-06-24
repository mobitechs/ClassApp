package com.mobitechs.classapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.utils.Constants
import java.io.File
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedPrefsManager(
    private val context: Context,
    private val gson: Gson
) {

    companion object {
        private const val PREF_RECENT_SEARCHES = "recent_searches"
        private const val KEY_IS_PREMIUM_USER = "is_premium_user"
        private const val KEY_PREMIUM_EXPIRY_DATE = "premium_expiry_date"
    }

    // Create or get the master key for encryption
    private val masterKeyAlias by lazy {
        try {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } catch (e: Exception) {
            // If master key creation fails, clear and retry
            clearCorruptedData()
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        }
    }

    // Get encrypted shared preferences with error handling
    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                Constants.ENCRYPTED_PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Log the error
            e.printStackTrace()

            // Try to recover by clearing corrupted data
            clearCorruptedData()

            // Try creating again
            try {
                EncryptedSharedPreferences.create(
                    Constants.ENCRYPTED_PREFS_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e2: Exception) {
                // If it still fails, use regular SharedPreferences as fallback
                e2.printStackTrace()
                context.getSharedPreferences(
                    "${Constants.ENCRYPTED_PREFS_FILE_NAME}_fallback",
                    Context.MODE_PRIVATE
                )
            }
        }
    }

    private fun clearCorruptedData() {
        try {
            // Delete the encrypted shared preferences file
            val prefsFile = File(
                context.applicationInfo.dataDir + "/shared_prefs/" +
                        Constants.ENCRYPTED_PREFS_FILE_NAME + ".xml"
            )
            if (prefsFile.exists()) {
                prefsFile.delete()
            }

            // Also delete the backup file if it exists
            val backupFile = File(
                context.applicationInfo.dataDir + "/shared_prefs/" +
                        Constants.ENCRYPTED_PREFS_FILE_NAME + ".xml.bak"
            )
            if (backupFile.exists()) {
                backupFile.delete()
            }

            // Clear the master key from Android Keystore
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                keyStore.deleteEntry(masterKeyAlias)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Add these methods for tracking daily API calls
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setLastSyncDate(key: String) {
        try {
            val todayDate = dateFormat.format(Date())
            sharedPreferences.edit().putString("sync_$key", todayDate).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAlreadySyncedToday(key: String): Boolean {
        return try {
            val lastSyncDate = sharedPreferences.getString("sync_$key", "")
            val todayDate = dateFormat.format(Date())
            lastSyncDate == todayDate
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Auth token with error handling
    fun saveAuthToken(token: String) {
        try {
            sharedPreferences.edit().putString(Constants.PREF_AUTH_TOKEN, token).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAuthToken(): String? {
        return try {
            sharedPreferences.getString(Constants.PREF_AUTH_TOKEN, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearAuthToken() {
        try {
            sharedPreferences.edit().remove(Constants.PREF_AUTH_TOKEN).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Student data with error handling
    fun saveUser(user: Student) {
        try {
            val userJson = gson.toJson(user)
            sharedPreferences.edit().putString(Constants.PREF_USER, userJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUser(): Student? {
        return try {
            val userJson = sharedPreferences.getString(Constants.PREF_USER, null)
            if (userJson != null) {
                gson.fromJson(userJson, Student::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearUser() {
        try {
            sharedPreferences.edit().remove(Constants.PREF_USER).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Check if user is logged in with error handling
    fun isLoggedIn(): Boolean {
        return try {
            getAuthToken() != null && getUser() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Clear all data (logout) with error handling
    fun logout() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
            // If clearing fails, try to delete the files
            clearCorruptedData()
        }
    }

    // Save premium status - FIXED
    fun savePremiumStatus(isPremium: Boolean) {
        try {
            sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM_USER, isPremium).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get premium status
    fun isPremiumUser(): Boolean {
        return try {
            sharedPreferences.getBoolean(KEY_IS_PREMIUM_USER, false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Save premium expiry date - FIXED
    fun savePremiumExpiryDate(expiryDate: String?) {
        try {
            if (expiryDate != null) {
                sharedPreferences.edit().putString(KEY_PREMIUM_EXPIRY_DATE, expiryDate).apply()
            } else {
                sharedPreferences.edit().remove(KEY_PREMIUM_EXPIRY_DATE).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get premium expiry date
    fun getPremiumExpiryDate(): String? {
        return try {
            sharedPreferences.getString(KEY_PREMIUM_EXPIRY_DATE, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Check if premium is still valid (if using expiry dates)
    fun isPremiumValid(): Boolean {
        return try {
            val expiryDate = getPremiumExpiryDate()
            if (expiryDate == null) {
                return isPremiumUser()
            }

            // Parse and compare dates
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = formatter.parse(expiryDate)
            val today = Date()
            expiry?.after(today) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveRecentSearches(searches: List<String>) {
        try {
            val json = gson.toJson(searches)
            sharedPreferences.edit().putString(PREF_RECENT_SEARCHES, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRecentSearches(): List<String> {
        return try {
            val json = sharedPreferences.getString(PREF_RECENT_SEARCHES, null) ?: return emptyList()
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun clearRecentSearches() {
        try {
            sharedPreferences.edit().remove(PREF_RECENT_SEARCHES).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}