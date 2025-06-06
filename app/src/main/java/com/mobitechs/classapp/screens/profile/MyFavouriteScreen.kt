package com.mobitechs.classapp.screens.profile


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.screens.common.CourseCardEmptyMessageWithoutBox
import com.mobitechs.classapp.screens.common.CourseCardRectangular
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.openCourseDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavouriteScreen(
    viewModel: FavouriteViewModel,
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
                    searchFor = "Search by name, category, subject...",
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
                    title = { Text("My Favourites") },
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
                        onRetry = { viewModel.loadFavouriteCourses() }
                    )
                }

                uiState.filteredCourses.isEmpty() -> {
                    CourseCardEmptyMessageWithoutBox(
                        message = if (searchQuery.isNotEmpty())
                            "No courses found matching \"$searchQuery\""
                        else
                            "No favourite courses yet",
                        description = if (searchQuery.isNotEmpty())
                            "Try searching with different keywords"
                        else
                            "Start adding courses to your favourites",
                        Icons.Default.FavoriteBorder
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.filteredCourses,
                            key = { course -> course.id } // Important for animation
                        ) { course ->
                            var isVisible by remember { mutableStateOf(true) }

                            AnimatedVisibility(
                                visible = isVisible,
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                CourseCardRectangular(
                                    course = course,
                                    onClick = {
                                        openCourseDetailsScreen(navController, course)
                                    },
                                    onFavoriteClick = {
                                        // Trigger animation before removing
                                        isVisible = false
                                        // Delay the actual removal slightly for smooth animation
                                        viewModel.handleFavoriteClick(
                                            course.id,
                                            course.is_favourited
                                        )
                                    },
                                    onWishlistClick = {
                                        viewModel.handleWishlistClick(
                                            course.id,
                                            course.is_in_wishlist
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    searchFor: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(searchFor)
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Close search"
                )
            }
        },
        actions = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        }
    )
}


@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}