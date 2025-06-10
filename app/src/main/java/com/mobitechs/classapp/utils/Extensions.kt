package com.mobitechs.classapp.utils

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector



fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
fun getFirstName(fullName: String): String {
    // Check if the input string is not empty or null
    return if (!fullName.isNullOrBlank()) {
        val nameParts = fullName.split(" ") // Split by space
        nameParts.getOrNull(0) ?: "" // Return the first part (first name), or empty string if not available
    } else {
        "" // Return empty string if the fullName is empty or null
    }
}


fun openCourseDetailsScreen(navController: NavController,course : Course) {
    navController.navigate("course_detail?courseJson=${Gson().toJson(course)}")
}


fun openSeeAllCourse(navController: NavController,courses : List<Course>, courseType:String) {
    val coursesJson = Gson().toJson(courses)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("seeAllCoursesScreen?courseListJson=$encodedJson/courseType=$courseType")
}

fun openSeeAllCategory(navController: NavController,courses : List<CategoryItem>) {
    val coursesJson = Gson().toJson(courses)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("SeeAllCategoriesScreen?categoryListJson=$encodedJson")
}

fun openCategoryWiseDetailsScreen(navController: NavController, categoryId:String, categoryName:String) {
    navController.navigate("categoryWiseDetailsScreen?categoryId=${categoryId}/categoryName=${categoryName}")
}

fun openVideoPlayer(navController: NavController, course:Course, videoUrl:String) {
    val coursesJson = Gson().toJson(course)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("video_player?courseJson=$encodedJson/videoUrl=$videoUrl")
}

fun openPDFReader(navController: NavController, course:Course, videoUrl:String) {
    val coursesJson = Gson().toJson(course)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("PDFReader?courseJson=$encodedJson/url=$videoUrl")
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