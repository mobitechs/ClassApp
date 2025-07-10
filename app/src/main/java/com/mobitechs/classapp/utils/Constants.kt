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
    const val CONNECT_YOUTUBE = "https://www.youtube.com/@AdityaGarkal."
    const val CONNECT_TELEGRAM = "https://t.me/AdityaGarkal"
    const val CONNECT_WHATSAPP = "https://whatsapp.com/channel/0029VamsDsw4IBhJMZiavq2L/103"
    const val CONNECT_FACEBOOK = "https://www.facebook.com/Pratik1082"


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
    const val RAZORPAY_KEY_ID = "rzp_test_cMgcyHVPwSF1sq" // Replace with actual key in build config

    //    const val RAZORPAY_KEY_ID = "rzp_test_e9mokpUFZZRN4g" // Replace with actual key in build config
    const val RAZORPAY_KEY_SECRET = "9uttQ5gw8WlYwkvRYVKnGE8I" // Replace with actual key in build config

    // Deep link URI schemes
    const val DEEP_LINK_SCHEME = "classconnect"
    const val DEEP_LINK_HOST = "app"


    // 1. Big Buck Bunny (Open Source Movie) - Various Qualities
    const val video1 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    const val video2 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
//    const val ytVideo = "https://www.youtube.com/watch?v=RzmBnllRnh8"

    // 2. Elephant's Dream (Open Source Movie)
    const val video3 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"

    // 3. For Bigger Blazes (Google Sample)
    const val video4 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    // 4. For Bigger Escape (Google Sample)
    const val video5 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"


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


    const val KEY_tbl_course = "courses"
    const val KEY_tbl_categories = "categories"
    const val KEY_tbl_sub_categories = "subCategories"
    const val KEY_tbl_subjects = "subjects"
    const val KEY_tbl_offerBanners = "offerBanners"
    const val KEY_tbl_notifications = "notifications"
    const val KEY_tbl_notices = "notices"

    const val KEY_SORT_NEW = "newest"
    const val KEY_SORT_POPULAR = "newest"
    const val KEY_SORT_FEATURED = "newest"
    const val KEY_SORT_PRICE_ASC = "priceAsc"
    const val KEY_SORT_PRICE_DESC = "priceDesc"


}