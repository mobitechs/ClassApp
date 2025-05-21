package com.mobitechs.classapp.utils


object Constants {
    // API related
    const val BASE_URL = "https://mobitechs.in/mobitech_laravel_classmate/public/api/"
    const val API_DOMAIN = "mobitechs.in"

    // Certificate pinning (example value - should be replaced with actual certificate hash)
    const val CERTIFICATE_PIN = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="

    // Shared Preferences
    const val ENCRYPTED_PREFS_FILE_NAME = "class_connect_secure_prefs"
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER = "user_data"

    // Study material types
    const val MATERIAL_TYPE_VIDEO = "VIDEO"
    const val MATERIAL_TYPE_PDF = "PDF"
    const val MATERIAL_TYPE_DOCUMENT = "DOCUMENT"

    // Notification redirect types
    const val REDIRECT_TYPE_COURSE = "COURSE"
    const val REDIRECT_TYPE_BATCH = "BATCH"
    const val REDIRECT_TYPE_EXTERNAL = "EXTERNAL"

    // Material filter options
    const val FILTER_NEW = "NEW"
    const val FILTER_FREE = "FREE"
    const val FILTER_TRENDING = "TRENDING"

    // Payment related
    const val RAZORPAY_KEY_ID = "rzp_test_Wjyo4GzP0wDOnH" // Replace with actual key in build config
//    const val RAZORPAY_KEY_ID = "rzp_test_e9mokpUFZZRN4g" // Replace with actual key in build config
    const val RAZORPAY_KEY_SECRET = "GA4aaivEKCFoXLXoR2xD6wun" // Replace with actual key in build config

    // Deep link URI schemes
    const val DEEP_LINK_SCHEME = "classconnect"
    const val DEEP_LINK_HOST = "app"




    // Keys for SharedPreferences
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_TOKEN_EXPIRY = "token_expiry"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_PHONE = "user_phone"
    const val KEY_USER_GENDER = "user_gender"
    const val KEY_USER_CITY = "user_city"
    const val KEY_USER_PINCODE = "user_pincode"
    const val KEY_LAST_ACTIVE = "last_active"
    const val KEY_CREATED_AT = "created_at"
    const val KEY_UPDATED_AT = "updated_at"
    const val KEY_IS_ACTIVE = "is_active"
    const val KEY_ADDRESS = "address"
    const val KEY_ADHAR_IMAGE = "adhar_image"
    const val KEY_ADHAR_NO = "adhar_no"
    const val KEY_BLOOD_GROUP = "blood_group"
    const val KEY_DELETED_AT = "deleted_at"
    const val KEY_ADDED_BY = "added_by"

    const val KEY_PAN_IMAGE = "pan_image"
    const val KEY_PAN_NO = "pan_no"
    const val KEY_PHOTO = "photo"
    const val KEY_SIGNATURE = "signature"
    const val KEY_PASSWORD = "password"
    const val KEY_TOKEN = "token"



}