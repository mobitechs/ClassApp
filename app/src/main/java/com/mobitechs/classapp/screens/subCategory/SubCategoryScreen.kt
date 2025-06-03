package com.mobitechs.classapp.screens.subCategory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem
import com.mobitechs.classapp.screens.common.SectionTitle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: SubCategoryViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // State for selected subcategory
    var selectedSubcategory by remember { mutableStateOf<SubCategoryItem?>(null) }

    // Set first subcategory as selected by default when subcategories are loaded
    LaunchedEffect(uiState.subcategories) {
        if (uiState.subcategories.isNotEmpty() && selectedSubcategory == null) {
            selectedSubcategory = uiState.subcategories.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("category.name") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Subcategories section with horizontal scrolling chips
            SubcategoriesChipSection(
                subcategories = uiState.subcategories,
                selectedSubcategory = selectedSubcategory,
                onSubcategoryClick = { selectedSubcategory = it }
            )

            Spacer(modifier = Modifier.height(24.dp))


        }
    }
}

@Composable
fun SubcategoriesChipSection(
    subcategories: List<SubCategoryItem>,
    selectedSubcategory: SubCategoryItem?,
    onSubcategoryClick: (SubCategoryItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Use a single horizontally scrollable container
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                Column(
                    // Remove any default vertical spacing between rows
                    verticalArrangement = Arrangement.Top,
                    // Specify a tight vertical padding to keep rows close
                    modifier = Modifier.padding(vertical = 0.dp)
                ) {
                    // First row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Every even indexed item (0, 2, 4...)
                        subcategories.forEachIndexed { index, subcategory ->
                            if (index % 2 == 0) {
                                SubcategoryChip(
                                    subcategory = subcategory,
                                    isSelected = subcategory.id == selectedSubcategory?.id,
                                    onClick = { onSubcategoryClick(subcategory) }
                                )
                            }
                        }
                    }

                    // No spacer between rows

                    // Second row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Every odd indexed item (1, 3, 5...)
                        subcategories.forEachIndexed { index, subcategory ->
                            if (index % 2 == 1) {
                                SubcategoryChip(
                                    subcategory = subcategory,
                                    isSelected = subcategory.id == selectedSubcategory?.id,
                                    onClick = { onSubcategoryClick(subcategory) }
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
fun SubcategoryChip(
    subcategory: SubCategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedFilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = subcategory.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun SubjectsGridSection(
    subcategory: SubCategoryItem,
    subjects: List<SubjectItem>,
    subjectCounts: Map<Int, Int>,
    onSubjectClick: (SubjectItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = "${subcategory.name} Subjects",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (subjects.isEmpty()) {
            Text(
                text = "No subjects found for ${subcategory.name}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            // Split the subjects into rows (2 rows)
            val rows = subjects.chunked((subjects.size + 1) / 2)

            Column(modifier = Modifier.fillMaxWidth()) {
                // First row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rows.getOrElse(0) { emptyList() }) { subject ->
                        SubjectCard(
                            subject = subject,
                            courseCount = subjectCounts[subject.id] ?: 0,
                            onClick = { onSubjectClick(subject) }
                        )
                    }
                }

                // Second row (if we have enough items)
                if (rows.size > 1 && rows[1].isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rows[1]) { subject ->
                            SubjectCard(
                                subject = subject,
                                courseCount = subjectCounts[subject.id] ?: 0,
                                onClick = { onSubjectClick(subject) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: SubjectItem,
    courseCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subject Icon
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject name
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Course count
            Text(
                text = "$courseCount Courses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CoursesSection(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = "Popular Courses",
            onSeeAllClick = { /* Navigate to all courses */ },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEach { course ->
                CourseListItem(
                    course = course,
                    onClick = { onCourseClick(course.id.toString()) }
                )
            }
        }
    }
}

@Composable
fun CourseListItem(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course thumbnail
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
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Course details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.course_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "By ${course.instructor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating and price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = course.course_like.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Price
                    Text(
                        text = course.course_discounted_price,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
