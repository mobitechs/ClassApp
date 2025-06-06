package com.mobitechs.classapp.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem
import com.mobitechs.classapp.screens.common.CourseCardForStore
import com.mobitechs.classapp.screens.common.PrimaryButton
import com.mobitechs.classapp.screens.common.StoreCategoryItem
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.openCourseDetailsScreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    ToastObserver(viewModel)

    // Main layout with Scaffold
    Scaffold(
        // Top app bar with title showing selected filters
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store",
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Filter chips with improved layout
                    FilterChipsRow(
                        selectedSubCategoryId = uiState.selectedSubCategoryId,
                        selectedSubjectId = uiState.selectedSubjectId,
                        selectedPriceRange = uiState.selectedPriceRange,
                        onFilterClick = { filterType ->
                            when (filterType) {
                                FilterType.SUBCATEGORY -> {
                                    if (uiState.subCategoriesLoading) {
                                        // Still loading, do nothing
                                        return@FilterChipsRow
                                    } else {
                                        // Always show bottom sheet, handle empty state inside
                                        currentFilterType = filterType
                                        coroutineScope.launch { sheetState.show() }
                                    }
                                }

                                FilterType.SUBJECT -> {
                                    if (uiState.subjectsLoading) {
                                        // Still loading, do nothing
                                        return@FilterChipsRow
                                    } else {
                                        // Always show bottom sheet, handle empty state inside
                                        currentFilterType = filterType
                                        coroutineScope.launch { sheetState.show() }
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Always show Applied Filters Section at the top
                        AppliedFiltersSection(
                            selectedCategory = uiState.selectedCategory,
                            selectedSubCategoryId = uiState.selectedSubCategoryId,
                            selectedSubjectId = uiState.selectedSubjectId,
                            selectedPriceRange = uiState.selectedPriceRange,
                            subCategories = uiState.subCategories,
                            subjects = uiState.subjects,
                            onClearFilter = { filterType ->
                                when (filterType) {
                                    FilterType.CATEGORY -> viewModel.clearCategoryFilter()
                                    FilterType.SUBCATEGORY -> viewModel.clearSubCategoryFilter()
                                    FilterType.SUBJECT -> viewModel.clearSubjectFilter()
                                    FilterType.PRICE -> viewModel.clearPriceFilter()
                                }
                            },
                            onResetAllFilters = { viewModel.resetAllFilters() }
                        )

                        // Course content area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when {
                                uiState.coursesLoading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                uiState.coursesError.isNotEmpty() -> {
                                    RetrySection(
                                        message = uiState.coursesError,
                                        onRetry = { viewModel.retryLoadCourses() },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                uiState.filteredCourses.isEmpty() -> {
                                    EmptyCoursesList()
                                }

                                else -> {
                                    // Course grid with improved spacing
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(uiState.filteredCourses) { course ->
                                            CourseCardForStore(
                                                course = course,
                                                onCourseClick = {
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
                            FilterOption(it.id, it.name)
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
                            FilterOption(it.id, it.name)
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
                    PriceSliderContent(
                        currentPriceRange = uiState.selectedPriceRange,
                        onPriceRangeChanged = { minPrice, maxPrice ->
                            viewModel.selectPriceRange(minPrice, maxPrice)
                        },
                        onApply = {
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
 * Component to show currently applied filters with Reset All button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppliedFiltersSection(
    selectedCategory: CategoryItem?,
    selectedSubCategoryId: Int?,
    selectedSubjectId: Int?,
    selectedPriceRange: PriceRange,
    subCategories: List<SubCategoryItem>,
    subjects: List<SubjectItem>,
    onClearFilter: (FilterType) -> Unit,
    onResetAllFilters: () -> Unit
) {
    // Get the names for selected filters
    val selectedSubCategoryName = if (selectedSubCategoryId != null && selectedSubCategoryId != 0) {
        subCategories.find { it.id == selectedSubCategoryId }?.name
    } else null

    val selectedSubjectName = if (selectedSubjectId != null && selectedSubjectId != 0) {
        subjects.find { it.id == selectedSubjectId }?.name
    } else null

    // Check if any filters are applied
    val hasFilters = selectedCategory != null ||
            selectedSubCategoryName != null ||
            selectedSubjectName != null ||
            selectedPriceRange.isActive

    if (hasFilters) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Header row with current selection display and "Reset All" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentSelection = "Applied Filter"

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSelection,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }

                // Reset All Filters Button
                OutlinedButton(
                    onClick = onResetAllFilters,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset All",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset All",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category filter chip
                selectedCategory?.let { category ->
                    FilterTag(
                        label = "Category: ${category.name}",
                        onClear = { onClearFilter(FilterType.CATEGORY) }
                    )
                }

                // Subcategory filter chip
                selectedSubCategoryName?.let { name ->
                    FilterTag(
                        label = "Type: $name",
                        onClear = { onClearFilter(FilterType.SUBCATEGORY) }
                    )
                }

                // Subject filter chip
                selectedSubjectName?.let { name ->
                    FilterTag(
                        label = "Subject: $name",
                        onClear = { onClearFilter(FilterType.SUBJECT) }
                    )
                }

                // Price filter chip
                if (selectedPriceRange.isActive) {
                    FilterTag(
                        label = "Price: ${selectedPriceRange.displayName}",
                        onClear = { onClearFilter(FilterType.PRICE) }
                    )
                }
            }
        }
    }
}

/**
 * Individual filter tag with clear button
 */
@Composable
fun FilterTag(
    label: String,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(
                    1f,
                    fill = false
                ) // This ensures text doesn't push out the icon
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear filter",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onClear() }
            )
        }
    }
}

/**
 * Price slider content for bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceSliderContent(
    currentPriceRange: PriceRange,
    onPriceRangeChanged: (Float, Float) -> Unit,
    onApply: () -> Unit
) {
    var sliderPosition by remember {
        mutableStateOf(currentPriceRange.minPrice..currentPriceRange.maxPrice)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Price Range",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Current range display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "₹${sliderPosition.start.roundToInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "₹${sliderPosition.endInclusive.roundToInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Range Slider
        RangeSlider(
            value = sliderPosition,
            onValueChange = { newRange ->
                sliderPosition = newRange
            },
            valueRange = 0f..10000f,
            steps = 19, // Creates steps of 500
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Min and Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "₹0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "₹10,000+",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    sliderPosition = 0f..10000f
                    onPriceRangeChanged(0f, 10000f)
                    onApply()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    onPriceRangeChanged(sliderPosition.start, sliderPosition.endInclusive)
                    onApply()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
            StoreCategoryItem(
                icon = Icons.Default.Star,
                name = "New Courses",
                isSelected = selectedCategoryId == null,
                onCategorySelected = { onCategorySelected(-1) }
            )
        }

        // Regular categories
        items(categories) { category ->
            StoreCategoryItem(
                icon = getCategoryIcon(category.name),
                name = category.name,
                isSelected = selectedCategoryId == category.id,
                onCategorySelected = { onCategorySelected(category.id) }
            )
        }
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
 * Filter chips row with improved layout and loading states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedSubCategoryId: Int?,
    selectedSubjectId: Int?,
    selectedPriceRange: PriceRange,
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
                selected = selectedSubCategoryId != null && selectedSubCategoryId != 0,
                onClick = { onFilterClick(FilterType.SUBCATEGORY) },
                label = {
                    Text(
                        text = if (selectedSubCategoryId != null && selectedSubCategoryId != 0) "Type ✓" else "Type"
                    )
                },
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
                selected = selectedSubjectId != null && selectedSubjectId != 0,
                onClick = { onFilterClick(FilterType.SUBJECT) },
                label = {
                    Text(
                        text = if (selectedSubjectId != null && selectedSubjectId != 0) "Subject ✓" else "Subject"
                    )
                },
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
                selected = selectedPriceRange.isActive,
                onClick = { onFilterClick(FilterType.PRICE) },
                label = {
                    Text(
                        text = if (selectedPriceRange.isActive) "Price ✓" else "Price"
                    )
                },
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
    val id: Int,
    val name: String
)

/**
 * Bottom sheet for filters with improved layout and empty state handling
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheetContent(
    title: String,
    options: List<FilterOption>,
    selectedOptionId: Int?,
    onOptionSelected: (Int) -> Unit
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

        if (options.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No options available",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Try selecting a category first or check your connection",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            // FlowRow automatically arranges items based on available space
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Card(
                        onClick = { onOptionSelected(option.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOptionId == option.id)
                                MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedOptionId == option.id)
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                    ) {
                        Text(
                            text = option.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (selectedOptionId == option.id) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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