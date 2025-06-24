package com.mobitechs.classapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun getFirstName(fullName: String): String {
    // Check if the input string is not empty or null
    return if (!fullName.isNullOrBlank()) {
        val nameParts = fullName.split(" ") // Split by space
        nameParts.getOrNull(0)
            ?: "" // Return the first part (first name), or empty string if not available
    } else {
        "" // Return empty string if the fullName is empty or null
    }
}


fun openCourseDetailsScreen(navController: NavController, course: Course) {
    navController.navigate("course_detail?courseJson=${Gson().toJson(course)}")
}


fun openSeeAllCourse(navController: NavController, courses: List<Course>, courseType: String) {
    val coursesJson = Gson().toJson(courses)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("seeAllCoursesScreen?courseListJson=$encodedJson/courseType=$courseType")
}

fun openSeeAllCategory(navController: NavController, courses: List<CategoryItem>) {
    val coursesJson = Gson().toJson(courses)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("SeeAllCategoriesScreen?categoryListJson=$encodedJson")
}

fun openCategoryWiseDetailsScreen(
    navController: NavController,
    categoryId: String,
    categoryName: String
) {
    navController.navigate("categoryWiseDetailsScreen?categoryId=${categoryId}/categoryName=${categoryName}")
}

fun openVideoPlayer(navController: NavController, course: Course, videoUrl: String) {
    val coursesJson = Gson().toJson(course)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("video_player?courseJson=$encodedJson/videoUrl=$videoUrl")
}

fun openPDFReader(navController: NavController, course: Course, videoUrl: String) {
    val coursesJson = Gson().toJson(course)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("PDFReader?courseJson=$encodedJson/url=$videoUrl")
}

fun openChannelLinks(context: Context, channelUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error silently or show toast
    }
}


fun handleContentClick(
    course: Course,
    content: Content,
    is_purchased: Boolean,
    navController: NavController,
    context: Context
) {
    // Check if content is accessible
    if (content.is_free != "Yes" && !is_purchased) {
        showToast(context, "This content is not available in the free version")
        return
    }

    when (content.content_type.uppercase()) {
        "VIDEO" -> {
            openVideoPlayer(navController, course, content.content_url)
        }

        "AUDIO" -> {
            openVideoPlayer(navController, course, content.content_url)
        }

        "PDF" -> {
            openPDFReader(navController, course, content.content_url)
        }

        else -> {
            // Open in browser or default viewer
            showToast(context, "Opening ${content.content_type}...")
            // You can implement browser opening logic here
        }
    }
}


// while setting color to container
fun Int?.toComposeColor(default: Color = Color.Gray): Color {
    return this?.let { Color(it) } ?: default
}


fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c

    val (r, g, b) = when ((hue / 60f).toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        5 -> Triple(c, 0f, x)
        else -> Triple(0f, 0f, 0f)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}


fun updateCategoriesWithUIData(categoryList: List<CategoryItem>): List<CategoryItem> {
    // Define icons explicitly to avoid reflection issues
    val iconList = listOf(
        "Home", "School", "Book", "Science", "Calculate",
        "Computer", "Language", "History", "MusicNote", "Palette",
        "SportsSoccer", "Star", "Favorite", "ThumbUp", "ShoppingCart",
        "Person", "Work", "Email", "Phone", "LocationOn",
        "Search", "Info", "Help", "Assignment", "Dashboard",
        "Category", "Folder", "Build", "AccountCircle", "Settings",
        "CheckCircle", "Schedule", "Lightbulb", "Group", "Map",
        "Restaurant", "Flight", "Hotel", "LocalHospital", "School"
    ).distinct() // Remove any duplicates

    val totalCategories = categoryList.size

    return categoryList.mapIndexed { index, category ->
        // Assign icon based on index - guaranteed different for each category
        val iconName = iconList[index % iconList.size]

        // Generate unique colors (this part is already working)
        val hue = (360f / totalCategories) * index
        val hueWithVariation = (hue + (index * 13) % 20) % 360f

        val iconColor = hsvToColor(
            hue = hueWithVariation,
            saturation = 0.75f - (index % 3) * 0.1f,
            value = 0.55f - (index % 2) * 0.1f
        )


        val backgroundColor = hsvToColor(
            hue = hueWithVariation,
            saturation = 0.08f + (index % 3) * 0.02f,  // Changed from 0.15f to 0.08f (much lighter)
            value = 0.97f + (index % 2) * 0.02f        // Changed from 0.95f to 0.97f (brighter)
        )

//        val backgroundColor = hsvToColor(
//            hue = hueWithVariation,
//            saturation = 0.05f + (index % 3) * 0.02f,  // Very light: 0.05f to 0.07f
//            value = 0.98f + (index % 2) * 0.01f        // Very bright: 0.98f to 0.99f
//        )

        category.copy(
            iconName = iconName,
            iconColor = iconColor.toArgb(),
            backgroundColor = backgroundColor.toArgb(),
            courseCount = (50..300).random()
        )
    }
}

// Updated helper function with better error handling
fun getIconFromFieldName(fieldName: String?): ImageVector {
    if (fieldName.isNullOrEmpty()) return Icons.Default.Category

    // Map of common icon names to their actual icons
    val iconMap = mapOf(
        "Home" to Icons.Default.Home,
        "School" to Icons.Default.School,
        "Book" to Icons.Default.Book,
        "Science" to Icons.Default.Science,
        "Calculate" to Icons.Default.Calculate,
        "Computer" to Icons.Default.Computer,
        "Language" to Icons.Default.Language,
        "History" to Icons.Default.History,
        "MusicNote" to Icons.Default.MusicNote,
        "Palette" to Icons.Default.Palette,
        "SportsSoccer" to Icons.Default.SportsSoccer,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "ThumbUp" to Icons.Default.ThumbUp,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Person" to Icons.Default.Person,
        "Work" to Icons.Default.Work,
        "Email" to Icons.Default.Email,
        "Phone" to Icons.Default.Phone,
        "LocationOn" to Icons.Default.LocationOn,
        "Search" to Icons.Default.Search,
        "Info" to Icons.Default.Info,
        "Help" to Icons.Default.Help,
        "Assignment" to Icons.Default.Assignment,
        "Dashboard" to Icons.Default.Dashboard,
        "Category" to Icons.Default.Category,
        "Folder" to Icons.Default.Folder,
        "Build" to Icons.Default.Build,
        "AccountCircle" to Icons.Default.AccountCircle,
        "Settings" to Icons.Default.Settings,
        "CheckCircle" to Icons.Default.CheckCircle,
        "Schedule" to Icons.Default.Schedule,
        "Lightbulb" to Icons.Default.Lightbulb,
        "Group" to Icons.Default.Group,
        "Map" to Icons.Default.Map,
        "Restaurant" to Icons.Default.Restaurant,
        "Flight" to Icons.Default.Flight,
        "Hotel" to Icons.Default.Hotel,
        "LocalHospital" to Icons.Default.LocalHospital
    )

    // First try to get from map
    iconMap[fieldName]?.let { return it }

    // If not in map, try reflection as fallback
    return try {
        val field = Icons.Default::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.get(Icons.Default) as? ImageVector ?: Icons.Default.Category
    } catch (e: Exception) {
        Icons.Default.Category
    }
}


fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()

    return String.format(
        "%.1f %s",
        sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

/**
 * Format timestamp to date string
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Format timestamp to date and time string
 */
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


/**
 * Get file extension from content type
 */
fun getFileExtension(contentType: String?): String {
    return when (contentType?.uppercase()) {
        "VIDEO" -> "mp4"
        "AUDIO" -> "mp3"
        "PDF" -> "pdf"
        "DOCUMENT" -> "doc"
        "IMAGE" -> "jpg"
        else -> "file"
    }
}

/**
 * Get MIME type from content type
 */
fun getMimeType(contentType: String?): String {
    return when (contentType?.uppercase()) {
        "VIDEO" -> "video/mp4"
        "AUDIO" -> "audio/mp3"
        "PDF" -> "application/pdf"
        "DOCUMENT" -> "application/msword"
        "IMAGE" -> "image/jpeg"
        else -> "*/*"
    }
}

/**
 * Format duration in seconds to readable format
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%d:%02d", minutes, secs)
    }
}

/**
 * Check if file size is within limit (in MB)
 */
fun isFileSizeWithinLimit(sizeInBytes: Long, limitInMB: Int): Boolean {
    val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
    return sizeInMB <= limitInMB
}

/**
 * Get relative time string (e.g., "2 hours ago")
 */
fun getRelativeTimeString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
        weeks < 4 -> "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
        months < 12 -> "$months ${if (months == 1L) "month" else "months"} ago"
        else -> "$years ${if (years == 1L) "year" else "years"} ago"
    }
}