package com.mobitechs.classapp.utils

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Course
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
fun openCourseDetailsScreen(navController: NavController,course : Course) {
    navController.navigate("course_detail?courseJson=${Gson().toJson(course)}")
}


fun openSeeAllCourse(navController: NavController,courses : List<Course>, courseType:String) {
    val coursesJson = Gson().toJson(courses)
    val encodedJson = URLEncoder.encode(coursesJson, StandardCharsets.UTF_8.toString())
    navController.navigate("seeAllCoursesScreen?courseListJson=$encodedJson/courseType=$courseType")
}
fun openCategoryWiseDetailsScreen(navController: NavController, categoryId:String, categoryName:String) {
    navController.navigate("categoryWiseDetailsScreen?categoryId=${categoryId}/categoryName=${categoryName}")
}