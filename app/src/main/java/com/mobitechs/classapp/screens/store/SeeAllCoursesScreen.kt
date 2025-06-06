package com.mobitechs.classapp.screens.store


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.screens.common.CourseCardEmptyMessageWithoutBox
import com.mobitechs.classapp.screens.common.CourseCardRectangular
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.profile.SearchAppBar
import com.mobitechs.classapp.utils.openCourseDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllCoursesScreen(
    navController: NavController,
    coursesJson: String,
    courseType: String,
    homeViewModel: HomeViewModel
) {
    // Deserialize the courses from JSON
    val gson = Gson()
    val courseListType = object : TypeToken<List<Course>>() {}.type
    val courses: List<Course> = try {
        gson.fromJson(coursesJson, courseListType)
    } catch (e: Exception) {
        emptyList()
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter courses based on search query
    val filteredCourses = remember(searchQuery, courses) {
        if (searchQuery.isEmpty()) {
            courses
        } else {
            val query = searchQuery.trim().lowercase()
            courses.filter { course ->
                course.course_name.lowercase().contains(query) ||
                        course.course_description?.lowercase()?.contains(query) == true ||
                        course.category_name?.lowercase()?.contains(query) == true ||
                        course.sub_category_name?.lowercase()?.contains(query) == true ||
                        course.subject_name?.lowercase()?.contains(query) == true
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCloseSearch = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = when(courseType) {
                                "popular" -> "Popular Courses"
                                "featured" -> "Featured Courses"
                                else -> "All Courses"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredCourses.isEmpty()) {
                CourseCardEmptyMessageWithoutBox(
                    message = if (searchQuery.isNotEmpty())
                        "No courses found matching \"$searchQuery\""
                    else
                        "No ${courseType} courses available",
                    description = if (searchQuery.isNotEmpty())
                        "Try searching with different keywords"
                    else
                        "Check back later for new courses",
                    Icons.Default.School
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCourses) { course ->
                        CourseCardRectangular(
                            course = course,
                            onClick = {
                                openCourseDetailsScreen(navController, course)
                            },
                            onFavoriteClick = {
                                homeViewModel.handleFavoriteClick(course.id, course.is_favourited)
                            },
                            onWishlistClick = {
                                homeViewModel.handleWishlistClick(course.id, course.is_in_wishlist)
                            }
                        )
                    }
                }
            }
        }
    }
}
