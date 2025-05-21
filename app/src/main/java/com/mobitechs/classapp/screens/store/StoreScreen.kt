package com.mobitechs.classapp.screens.store


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.screens.common.PrimaryButton
import com.mobitechs.classapp.screens.common.SecondaryButton
import com.mobitechs.classapp.screens.common.SectionTitle
import kotlinx.coroutines.launch
import java.net.URLEncoder

/**
 * Main Store Screen - Redesigned for better user experience
 * with cleaner layout and improved spacing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    navController: NavController
) {
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // For bottom sheet state
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    // Track which filter is being edited
    var currentFilterType by remember { mutableStateOf<FilterType?>(null) }

    // Main layout with Scaffold
    Scaffold(
        // Top app bar with title, back button and search icon
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.selectedCategory?.name ?: "Store",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main content
        Row(
            modifier = Modifier
//                .background(Color.Red)
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Categories sidebar (left) with divider
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
            ) {
                if (uiState.categoriesLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else if (uiState.categoriesError.isNotEmpty()) {
                    RetrySection(
                        message = "Failed to load categories",
                        onRetry = { viewModel.retryLoadCategories() },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CategoriesSidebar(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }

                // Vertical divider to separate sidebar from content
                Divider(
                    color = MaterialTheme.colorScheme.surfaceDim,
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                )
            }

            // Main content (right) with padding for better spacing
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp)
            ) {
                // Filter section with better visual separation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    // Filter chips with improved layout
                    FilterChipsRow(
                        selectedSubCategoryId = uiState.selectedSubCategoryId,
                        selectedSubjectId = uiState.selectedSubjectId,
                        selectedPriceRange = uiState.selectedPriceRange,
                        onFilterClick = { filterType ->
                            when (filterType) {
                                FilterType.SUBCATEGORY -> {
                                    if (!uiState.subCategoriesLoading &&
                                        uiState.subCategoriesError.isEmpty() &&
                                        uiState.subCategories.isNotEmpty()
                                    ) {
                                        currentFilterType = filterType
                                        coroutineScope.launch { sheetState.show() }
                                    } else if (uiState.subCategoriesError.isNotEmpty()) {
                                        viewModel.retryLoadSubCategories()
                                    }
                                }

                                FilterType.SUBJECT -> {
                                    if (!uiState.subjectsLoading &&
                                        uiState.subjectsError.isEmpty() &&
                                        uiState.subjects.isNotEmpty()
                                    ) {
                                        currentFilterType = filterType
                                        coroutineScope.launch { sheetState.show() }
                                    } else if (uiState.subjectsError.isNotEmpty()) {
                                        viewModel.retryLoadSubjects()
                                    }
                                }

                                FilterType.PRICE -> {
                                    currentFilterType = filterType
                                    coroutineScope.launch { sheetState.show() }
                                }

                                else -> {}
                            }
                        }
                    )
                }

                // Divider after filters for better section separation
                Divider(
                    color = MaterialTheme.colorScheme.surfaceDim,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

                // Course grid section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (uiState.coursesLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.coursesError.isNotEmpty()) {
                        RetrySection(
                            message = uiState.coursesError,
                            onRetry = { viewModel.retryLoadCourses() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (uiState.filteredCourses.isEmpty()) {
                        EmptyCoursesList()
                    } else {
                        // Section title for current category or all courses
                        SectionTitle(
                            title = if (uiState.selectedCategory != null)
                                "${uiState.selectedCategory?.name} Courses"
                            else
                                "All Courses"
                        )

                        // Course grid with improved spacing
                        CoursesGrid(
                            courses = uiState.filteredCourses,
                            onCourseClick = { course ->
//                                navController.navigate("course_details/$course")
                                val courseJson = URLEncoder.encode(Gson().toJson(course), "UTF-8")
                                navController.navigate("course_detail?courseJson=$courseJson")
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom sheet for filters
    if (currentFilterType != null && sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { currentFilterType = null },
            sheetState = sheetState
        ) {
            when (currentFilterType) {
                FilterType.SUBCATEGORY -> {
                    FilterBottomSheetContent(
                        title = "Select Sub Category",
                        options = uiState.subCategories.map {
                            FilterOption(it.id.toString(), it.name)
                        },
                        selectedOptionId = uiState.selectedSubCategoryId,
                        onOptionSelected = {
                            viewModel.selectSubCategory(it)
                            coroutineScope.launch {
                                sheetState.hide()
                                currentFilterType = null
                            }
                        }
                    )
                }

                FilterType.SUBJECT -> {
                    FilterBottomSheetContent(
                        title = "Select Subject",
                        options = uiState.subjects.map {
                            FilterOption(it.id.toString(), it.name)
                        },
                        selectedOptionId = uiState.selectedSubjectId,
                        onOptionSelected = {
                            viewModel.selectSubject(it)
                            coroutineScope.launch {
                                sheetState.hide()
                                currentFilterType = null
                            }
                        }
                    )
                }

                FilterType.PRICE -> {
                    PriceFilterContent(
                        priceRanges = listOf(
                            PriceRange("all", "All Prices"),
                            PriceRange("free", "Free"),
                            PriceRange("0-500", "Under ₹500"),
                            PriceRange("500-1000", "₹500 - ₹1000"),
                            PriceRange("1000-2000", "₹1000 - ₹2000"),
                            PriceRange("2000+", "₹2000+")
                        ),
                        selectedRange = uiState.selectedPriceRange,
                        onRangeSelected = {
                            viewModel.selectPriceRange(it)
                            coroutineScope.launch {
                                sheetState.hide()
                                currentFilterType = null
                            }
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

/**
 * Component for showing a retry button when loading fails
 */
@Composable
fun RetrySection(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.width(100.dp)
        )
    }
}

/**
 * Categories sidebar with improved visual design
 */
@Composable
fun CategoriesSidebar(
    categories: List<CategoryItem>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Special "Top Picks" category
        item {
            CategoryItem(
                icon = Icons.Default.Star,
                name = "Top Picks",
                isSelected = selectedCategoryId == null,
                onCategorySelected = { onCategorySelected(-1) }
            )
        }

        // Regular categories
        items(categories) { category ->
            CategoryItem(
                icon = getCategoryIcon(category.name),
                name = category.name,
                isSelected = selectedCategoryId == category.id,
                onCategorySelected = { onCategorySelected(category.id) }
            )
        }
    }
}

/**
 * Single category item with improved styling
 */
@Composable
fun CategoryItem(
    icon: ImageVector,
    name: String,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(75.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onCategorySelected),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with circle background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category name with better text handling
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

/**
 * Helper function to get an icon for a category
 */
@Composable
fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "programming" -> Icons.Default.Code
        "business" -> Icons.Default.Business
        "design" -> Icons.Default.Brush
        "marketing" -> Icons.Default.TrendingUp
        "language" -> Icons.Default.Language
        "mathematics" -> Icons.Default.Calculate
        "science" -> Icons.Default.Science
        else -> Icons.Default.School
    }
}

/**
 * Filter types enumeration
 */
enum class FilterType {
    CATEGORY, SUBCATEGORY, SUBJECT, PRICE
}

/**
 * Filter chips row with improved layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedSubCategoryId: String?,
    selectedSubjectId: String?,
    selectedPriceRange: PriceRange?,
    onFilterClick: (FilterType) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedSubCategoryId != null,
                onClick = { onFilterClick(FilterType.SUBCATEGORY) },
                label = { Text("Type") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            FilterChip(
                selected = selectedSubjectId != null,
                onClick = { onFilterClick(FilterType.SUBJECT) },
                label = { Text("Subject") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            FilterChip(
                selected = selectedPriceRange != null && selectedPriceRange.id != "all",
                onClick = { onFilterClick(FilterType.PRICE) },
                label = { Text("Price") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

/**
 * Data class for filter options
 */
data class FilterOption(
    val id: String,
    val name: String
)

/**
 * Bottom sheet for filters with improved layout
 */
@Composable
fun FilterBottomSheetContent(
    title: String,
    options: List<FilterOption>,
    selectedOptionId: String?,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(options) { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option.id) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOptionId == option.id,
                        onClick = { onOptionSelected(option.id) }
                    )

                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Price filter content
 */
@Composable
fun PriceFilterContent(
    priceRanges: List<PriceRange>,
    selectedRange: PriceRange?,
    onRangeSelected: (PriceRange) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Price Range",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(priceRanges) { range ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRangeSelected(range) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRange?.id == range.id,
                        onClick = { onRangeSelected(range) }
                    )

                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Grid of courses with improved card layout
 */
@Composable
fun CoursesGrid(
    courses: List<Course>,
//    onCourseClick: (String) -> Unit
    onCourseClick: (Course) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(courses) { course ->
            ImprovedCourseCard2(
                course = course,
                onCourseClick = { onCourseClick(course) }
//                onCourseClick = { onCourseClick(course.id.toString()) }
            )
        }
    }
}

/**
 * Improved course card with rating and likes in a solid dark rectangle overlay on the image
 */
@Composable
fun ImprovedCourseCard2(
    course: Course,
    onCourseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCourseClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Course image with overlay for rating and likes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                // Course image
                AsyncImage(
                    model = course.image,
                    contentDescription = course.course_name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Solid dark rectangle overlay at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    // Row for rating and likes inside the overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating on left
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF388E3C)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "4.${course.course_like % 10}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        // Likes on right
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Likes",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${course.course_like}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Course details with better spacing
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Course name
                Text(
                    text = course.course_name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = course.course_description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Price row with better alignment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Discounted price
                    if (course.course_discounted_price != null) {

                        // Original price with strikethrough
                        Text(
                            text = "₹${course.course_price}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${course.course_discounted_price}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )


                    } else {
                        // Regular price (no discount)
                        Text(
                            text = if (course.course_price == "0") "FREE" else "₹${course.course_price}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = if (course.course_price == "0")
                                Color(0xFF388E3C)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buy now button with fixed size text
                SecondaryButton(
                    text = "Buy Now",
                    onClick = onCourseClick,
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}


/**
 * Empty state when no courses match filters
 */
@Composable
fun EmptyCoursesList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No courses found",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Try adjusting your filters or browse a different category",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}