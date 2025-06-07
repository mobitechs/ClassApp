package com.mobitechs.classapp.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.utils.openCategoryWiseDetailsScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onBackClick = { navController.navigateUp() },
                onClearClick = { viewModel.clearSearch() },
                onFilterClick = { showFilters = true },
                hasActiveFilters = uiState.hasActiveFilters,
                focusRequester = focusRequester
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.searchQuery.isNotEmpty() -> {
                    SearchLoadingState()
                }
                uiState.searchQuery.isEmpty() -> {
                    SearchIdleState(
                        recentSearches = uiState.recentSearches,
                        popularSearches = uiState.popularSearches,
                        topCategories = uiState.topCategories,
                        onRecentSearchClick = { viewModel.updateSearchQuery(it) },
                        onPopularSearchClick = { viewModel.updateSearchQuery(it) },
                        onCategoryClick = { category ->
                            // Now you have the full category object
                            openCategoryWiseDetailsScreen(
                                navController,
                                category.id.toString(),
                                category.name
                            )
                        },
                        onClearRecentSearches = { viewModel.clearRecentSearches() }
                    )
                }
                uiState.searchCourses.isEmpty() && !uiState.isLoading -> {
                    SearchEmptyState(
                        searchQuery = uiState.searchQuery,
                        onSuggestionClick = { viewModel.updateSearchQuery(it) }
                    )
                }
                else -> {
                    searchCoursesContent(
                        searchCourses = uiState.searchCourses,
                        searchQuery = uiState.searchQuery,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }

            // Filter Bottom Sheet
            if (showFilters) {
                ModalBottomSheet(
                    onDismissRequest = { showFilters = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    FilterContent(
                        selectedFilters = uiState.selectedFilters,
                        onFilterChange = { filter, value -> viewModel.updateFilter(filter, value) },
                        onApplyFilters = {
                            viewModel.applyFilters()
                            showFilters = false
                        },
                        onResetFilters = { viewModel.resetFilters() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            SearchTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onClearClick = onClearClick,
                focusRequester = focusRequester
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filters"
                    )
                }
                if (hasActiveFilters) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    focusRequester: FocusRequester
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                "Search courses, subjects, topics...",
                style = MaterialTheme.typography.bodyLarge
            )
        },
//        leadingIcon = {
//            Icon(
//                imageVector = Icons.Default.Search,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
}

@Composable
fun SearchIdleState(
    recentSearches: List<String>,
    popularSearches: List<String>,
    topCategories: List<SearchCategory>,
    onRecentSearchClick: (String) -> Unit,
    onPopularSearchClick: (String) -> Unit,
    onCategoryClick: (SearchCategory) -> Unit,
    onClearRecentSearches: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Recent Searches Section
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClearRecentSearches) {
                        Text("Clear all")
                    }
                }
            }

            items(recentSearches) { search ->
                RecentSearchItem(
                    search = search,
                    onClick = { onRecentSearchClick(search) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Popular Searches Section
        item {
            Text(
                text = "Popular Searches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularSearches) { search ->
                    PopularSearchChip(
                        text = search,
                        onClick = { onPopularSearchClick(search) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Browse by Categories Section
        item {
            Text(
                text = "Browse by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(topCategories.chunked(2)) { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add empty space if odd number of categories
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecentSearchItem(
    search: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = search,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.NorthWest,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularSearchChip(
    text: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
fun CategoryCard(
    category: SearchCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = category.backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${category.courseCount} courses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun searchCoursesContent(
    searchCourses: List<Course>,
    searchQuery: String,
    navController: NavController,
    viewModel: SearchViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${searchCourses.size} results for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(searchCourses) { course ->
            SearchResultCard(
                course = course,
                searchQuery = searchQuery,
                onClick = {
                    // Navigate to course details with the course object
                    navController.currentBackStackEntry?.savedStateHandle?.set("course", course)
                    navController.navigate("course_details/${course.id}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(
    course: Course,
    searchQuery: String,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onWishlistClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Course thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Course title with highlighted search term
                Text(
                    text = course.course_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category breadcrumb
                Text(
                    text = "${course.category_name} > ${course.sub_category_name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Instructor and rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.instructor?:"No Instructor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${course.course_like}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price and action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Row {
                        if (course.course_discounted_price != null) {
                            Text(
                                text = "₹${course.course_discounted_price}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${course.course_price}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        } else {
                            Text(
                                text = "₹${course.course_price}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Action buttons
                    Row {
                        // Wishlist button
                        IconButton(
                            onClick = onWishlistClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (course.is_in_wishlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (course.is_in_wishlist) "Remove from wishlist" else "Add to wishlist",
                                tint = if (course.is_in_wishlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Favorite button
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (course.is_favourited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (course.is_favourited) "Remove from favorites" else "Add to favorites",
                                tint = if (course.is_favourited) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Searching...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchEmptyState(
    searchQuery: String,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We couldn't find any courses matching \"$searchQuery\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Suggestions
        Text(
            text = "Try searching for:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        val suggestions = listOf(
            "Mathematics",
            "Trigonometry Basics",
            "Calculus",
            "Physics",
            "Chemistry"
        )

        suggestions.forEach { suggestion ->
            TextButton(
                onClick = { onSuggestionClick(suggestion) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun FilterContent(
    selectedFilters: SearchFilters,
    onFilterChange: (String, Any) -> Unit,
    onApplyFilters: () -> Unit,
    onResetFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onResetFilters) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price Range
        Text(
            text = "Price Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilters.priceRange == PriceRange.FREE,
                onClick = { onFilterChange("priceRange", PriceRange.FREE) },
                label = { Text("Free") }
            )
            FilterChip(
                selected = selectedFilters.priceRange == PriceRange.UNDER_500,
                onClick = { onFilterChange("priceRange", PriceRange.UNDER_500) },
                label = { Text("Under ₹500") }
            )
            FilterChip(
                selected = selectedFilters.priceRange == PriceRange.UNDER_1000,
                onClick = { onFilterChange("priceRange", PriceRange.UNDER_1000) },
                label = { Text("Under ₹1000") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rating
        Text(
            text = "Minimum Rating",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(4, 3, 2).forEach { rating ->
                FilterChip(
                    selected = selectedFilters.minRating == rating,
                    onClick = { onFilterChange("minRating", rating) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$rating+")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFC107)
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Course Features
        Text(
            text = "Course Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Certificate Included")
                Switch(
                    checked = selectedFilters.hasCertificate,
                    onCheckedChange = { onFilterChange("hasCertificate", it) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Offline Available")
                Switch(
                    checked = selectedFilters.offlineAvailable,
                    onCheckedChange = { onFilterChange("offlineAvailable", it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Apply button
        Button(
            onClick = onApplyFilters,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply Filters")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Data classes for search functionality
data class SearchCategory(
    val id: String,
    val name: String,
    val courseCount: Int,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
)

data class SearchFilters(
    val priceRange: PriceRange? = null,
    val minRating: Int? = null,
    val hasCertificate: Boolean = false,
    val offlineAvailable: Boolean = false,
    val selectedCategories: List<String> = emptyList()
)

enum class PriceRange {
    FREE, UNDER_500, UNDER_1000, ABOVE_1000
}