package com.mobitechs.classapp.utils

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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