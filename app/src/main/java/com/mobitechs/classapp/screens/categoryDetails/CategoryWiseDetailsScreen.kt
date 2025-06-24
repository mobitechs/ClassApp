package com.mobitechs.classapp.screens.categoryDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.Screen
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem
import com.mobitechs.classapp.screens.common.CourseCardEmptyMessage
import com.mobitechs.classapp.screens.common.CourseCardPopularFeatured
import com.mobitechs.classapp.screens.common.CourseCardRectangular
import com.mobitechs.classapp.screens.common.SectionTitle
import com.mobitechs.classapp.ui.theme.AppTheme
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.openCourseDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryWiseDetailsScreen(
    viewModel: SubCategoryViewModel,
    navController: NavController,
    categoryId: Int = 1, // Pass this from navigation
    categoryName: String = "Category" // Pass this from navigation
) {
    val uiState by viewModel.uiState.collectAsState()

    ToastObserver(viewModel)

    // Initialize with the passed category ID
    LaunchedEffect(categoryId) {
        viewModel.setSelectedCategory(categoryId)
    }

    // State for selected subcategory and subject
    var selectedSubcategory by remember { mutableStateOf<SubCategoryItem?>(null) }
    var selectedSubject by remember { mutableStateOf<SubjectItem?>(null) }

    // Set first subcategory as selected by default when subcategories are loaded
    LaunchedEffect(uiState.subcategories) {
        if (uiState.subcategories.isNotEmpty() && selectedSubcategory == null) {
            selectedSubcategory = uiState.subcategories.first()
            viewModel.setSelectedSubcategory(selectedSubcategory!!.id)
        }
    }

    // Set first subject as selected by default when subjects are loaded
    LaunchedEffect(uiState.subject) {
        if (uiState.subject.isNotEmpty() && selectedSubject == null) {
            selectedSubject = uiState.subject.first()
            viewModel.setSelectedSubject(selectedSubject!!.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.SearchScreen.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = AppTheme.topAppBarColors,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Popular Courses Section
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(
                title = "Popular Courses",
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Popular courses horizontal scroll
            if (uiState.popularCoursesLoading) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.popularCourses.isEmpty()) {
                CourseCardEmptyMessage(
                    message = "No popular courses available at the moment",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.popularCourses) { course ->
                        CourseCardPopularFeatured(
                            course = course,
                            onClick = { openCourseDetailsScreen(navController, course) },
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

            // Subcategories section
            if (uiState.subcategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                FilterSection(
                    title = "Subcategories",
                    items = uiState.subcategories,
                    selectedItem = selectedSubcategory,
                    onItemClick = {
                        selectedSubcategory = it
                        viewModel.setSelectedSubcategory(it.id)
                        viewModel.loadCoursesBySubcategory(it.id)
                    },
                    itemContent = { item, isSelected ->
                        FilterChip(
                            text = item.name,
                            isSelected = isSelected,
                            onClick = {
                                selectedSubcategory = item
                                viewModel.setSelectedSubcategory(item.id)
                                viewModel.loadCoursesBySubcategory(item.id)
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Subcategory courses
                if (selectedSubcategory != null) {
                    when {
                        uiState.subcategoryCoursesLoading -> {
                            Box(
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.subcategoryCourses.isEmpty() -> {
                            CourseCardEmptyMessage(
                                message = "No courses available for ${selectedSubcategory?.name}",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.subcategoryCourses) { course ->
                                    CourseCardPopularFeatured(
                                        course = course,
                                        onClick = {
                                            openCourseDetailsScreen(
                                                navController,
                                                course
                                            )
                                        },
                                        onFavoriteClick = {
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

            // Subjects section
            if (uiState.subject.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                FilterSection(
                    title = "Subjects",
                    items = uiState.subject,
                    selectedItem = selectedSubject,
                    onItemClick = {
                        selectedSubject = it
                        viewModel.setSelectedSubject(it.id)
                        viewModel.loadCoursesBySubject(it.id)
                    },
                    itemContent = { item, isSelected ->
                        FilterChip(
                            text = item.name,
                            isSelected = isSelected,
                            onClick = {
                                selectedSubject = item
                                viewModel.setSelectedSubject(item.id)
                                viewModel.loadCoursesBySubject(item.id)
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Subject courses
                if (selectedSubject != null) {
                    when {
                        uiState.subjectCoursesLoading -> {
                            Box(
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.subjectCourses.isEmpty() -> {
                            CourseCardEmptyMessage(
                                message = "No courses available for ${selectedSubject?.name}",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.subjectCourses) { course ->
                                    CourseCardPopularFeatured(
                                        course = course,
                                        onClick = {
                                            openCourseDetailsScreen(
                                                navController,
                                                course
                                            )
                                        },
                                        onFavoriteClick = {
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

            // All Courses Section
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle(
                title = "All Courses",
            )
            Spacer(modifier = Modifier.height(4.dp))

            // All courses
            when {
                uiState.allCoursesLoading -> {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.allCourses.isEmpty() -> {
                    CourseCardEmptyMessage(
                        message = "No courses available in this category",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.allCourses.forEach { course ->
                            CourseCardRectangular(
                                course = course,
                                onClick = { openCourseDetailsScreen(navController, course) },
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun <T> FilterSection(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemClick: (T) -> Unit,
    itemContent: @Composable (T, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                itemContent(item, item == selectedItem)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedFilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

