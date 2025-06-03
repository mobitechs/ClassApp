package com.mobitechs.classapp.data.local


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedPrefsManager(
    private val context: Context,
    private val gson: Gson
) {

    // Create or get the master key for encryption
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Get encrypted shared preferences
    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            Constants.ENCRYPTED_PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    // Add these methods for tracking daily API calls
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setLastSyncDate(key: String) {
        val todayDate = dateFormat.format(Date())
        sharedPreferences.edit().putString("sync_$key", todayDate).apply()
    }

    fun isAlreadySyncedToday(key: String): Boolean {
        val lastSyncDate = sharedPreferences.getString("sync_$key", "")
        val todayDate = dateFormat.format(Date())
        return lastSyncDate == todayDate
    }

    // Auth token
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(Constants.PREF_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(Constants.PREF_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        sharedPreferences.edit().remove(Constants.PREF_AUTH_TOKEN).apply()
    }

    // Student data
    fun saveUser(user: Student) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(Constants.PREF_USER, userJson).apply()
    }

    fun getUser(): Student? {
        val userJson = sharedPreferences.getString(Constants.PREF_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, Student::class.java)
        } else {
            null
        }
    }


    fun clearUser() {
        sharedPreferences.edit().remove(Constants.PREF_USER).apply()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getUser() != null
    }

    // Clear all data (logout)
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }



}