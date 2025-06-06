package com.mobitechs.classapp.screens.profile



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.screens.common.CourseCardEmptyMessageWithoutBox
import com.mobitechs.classapp.screens.common.CourseCardRectangular
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.openCourseDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWishlistScreen(
    viewModel: MyWishListViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    ToastObserver(viewModel)

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = {
                        searchQuery = it
                        viewModel.searchCourses(it)
                    },
                    onCloseSearch = {
                        isSearchActive = false
                        searchQuery = ""
                        viewModel.searchCourses("")
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("My Wishlist") },
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error.isNotEmpty() -> {
                    ErrorView(
                        message = uiState.error,
                        onRetry = { viewModel.loadWishListCourses() }
                    )
                }

                uiState.filteredCourses.isEmpty() -> {


                    CourseCardEmptyMessageWithoutBox(
                        message = if (searchQuery.isNotEmpty())
                            "No courses found matching \"$searchQuery\""
                        else
                            "No courses in your wishlist.",
                        description = if (searchQuery.isNotEmpty())
                            "Try searching with different keywords"
                        else
                            "Start adding courses to your wishlist",
                        iconToShow = Icons.Default.BookmarkBorder
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.filteredCourses) { course ->
                            CourseCardRectangular(
                                course = course,
                                onClick = {
                                    openCourseDetailsScreen(navController, course)
                                },
                                onFavoriteClick = {
                                    viewModel.handleFavoriteClick(course.id, course.is_favourited)
                                },
                                onWishlistClick = {
                                    viewModel.handleWishlistClick(course.id, course.is_in_wishlist)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
