package com.mobitechs.classapp.data.local


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.User
import com.mobitechs.classapp.utils.Constants

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

    // User data
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(Constants.PREF_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(Constants.PREF_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
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
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}