package com.mobitechs.classapp.utils

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Course

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
fun openCourseDetailsScreen(navController: NavController,course : Course) {
    navController.navigate(
        "course_detail?courseJson=${
            Gson().toJson(
                course
            )
        }"
    )
}