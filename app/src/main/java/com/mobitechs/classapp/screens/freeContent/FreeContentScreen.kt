package com.mobitechs.classapp.screens.freeContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.utils.openPDFReader
import com.mobitechs.classapp.utils.openVideoPlayer
import com.mobitechs.classapp.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeContentScreen(
    navController: NavController,
    viewModel: FreeContentViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFreeContent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Free Content",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }

                uiState.error.isNotEmpty() -> {
                    ErrorView(
                        message = uiState.error,
                        onRetry = { viewModel.loadFreeContent() }
                    )
                }

                uiState.groupedContent.isEmpty() -> {
                    EmptyContentView()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header with total count
                        item {
                            FreeContentHeader(
                                totalCourses = uiState.groupedContent.size,
                                totalContent = uiState.groupedContent.values.sumOf { it.size }
                            )
                        }

                        // Course items with expandable content
                        items(
                            items = uiState.groupedContent.toList(),
                            key = { it.first.id }
                        ) { (course, contentList) ->
                            CourseWithContentCard(
                                course = course,
                                contentList = contentList,
                                isExpanded = uiState.expandedCourseIds.contains(course.id),
                                onToggleExpanded = {
                                    viewModel.toggleCourseExpanded(course.id)
                                },
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeContentHeader(
    totalCourses: Int,
    totalContent: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$totalCourses",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Courses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$totalContent",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Free Content",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CourseWithContentCard(
    course: Course,
    contentList: List<Content>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    navController: NavController,
    viewModel: FreeContentViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Course Header (Always visible)
            CourseHeader(
                course = course,
                contentCount = contentList.size,
                isExpanded = isExpanded,
                onToggleExpanded = onToggleExpanded,
                navController = navController
            )

            // Expandable Content List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    // Group content by type
                    val groupedByType = contentList.groupBy { it.content_type }

                    groupedByType.forEach { (type, contents) ->
                        ContentTypeSection(
                            type = type,
                            contentList = contents,
                            course = course,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(
    course: Course,
    contentCount: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Course Thumbnail - only show if image exists
        if (course.image != null) {
            AsyncImage(
                model = course.image,
                contentDescription = course.course_name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            // Show placeholder icon instead of image
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Course Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = course.course_name ?: "Course ${course.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Course metadata - only show if data exists
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Likes - only show if greater than 0
                if (course.course_like > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${course.course_like}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Content count
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$contentCount Free Items",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Tags if available
            if (!course.course_tags.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    course.course_tags.split(",").take(2).forEach { tag ->
                        if (tag.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tag.trim(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expand/Collapse Icon
        IconButton(onClick = onToggleExpanded) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }

    // Show additional course info when expanded
    if (isExpanded) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Divider()

            // Only show course info that exists
            val hasAdditionalInfo = !course.course_description.isNullOrEmpty() ||
                    !course.instructor.isNullOrEmpty()

            if (hasAdditionalInfo) {
                // Course description
                if (!course.course_description.isNullOrEmpty()) {
                    Text(
                        text = course.course_description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Additional course metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Instructor
                    if (!course.instructor.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = course.instructor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Always show Course ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Course ID: ${course.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Only show View Full Course button if course has actual data
            if (course.course_description != null || course.instructor != null ||
                course.course_tags != null || course.image != null) {
                TextButton(
                    onClick = {
                        try {
                            // Navigate to course detail
                            val courseJson = com.google.gson.Gson().toJson(course)
                            navController.navigate("courseDetailScreen/$courseJson")
                        } catch (e: Exception) {
                            showToast(context, "Unable to view course details")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("View Full Course Details")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentTypeSection(
    type: String,
    contentList: List<Content>,
    course: Course,
    navController: NavController,
    viewModel: FreeContentViewModel
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = getContentTypeIcon(type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = getContentTypeTitle(type),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${contentList.size}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Items
        contentList.forEachIndexed { index, content ->
            FreeContentItem(
                content = content,
                course = course,
                index = index + 1,
                navController = navController,
                viewModel = viewModel
            )

            if (index < contentList.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
@Composable
private fun FreeContentItem(
    content: Content,
    course: Course,
    index: Int,
    navController: NavController,
    viewModel: FreeContentViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloaded by remember { mutableStateOf(false) }

    // Check if content is downloaded
    LaunchedEffect(content.id) {
        isDownloaded = viewModel.isContentDownloaded(content.id)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                handleContentClick(
                    content = content,
                    course = course,
                    navController = navController,
                    context = context
                )
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index circle
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Lesson $index",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "FREE",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Show downloaded indicator
                if (isDownloaded) {
                    Icon(
                        imageVector = Icons.Outlined.DownloadDone,
                        contentDescription = "Downloaded",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Download Button
        if (!isDownloaded) {
            IconButton(
                onClick = {
                    viewModel.downloadContent(content, course)
                    showToast(context, "Download started")
                    // Refresh download status after a delay
                    scope.launch {
                        kotlinx.coroutines.delay(3000)
                        isDownloaded = viewModel.isContentDownloaded(content.id)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // Play/View Icon for downloaded content
            Icon(
                imageVector = when (content.content_type?.uppercase()) {
                    "VIDEO" -> Icons.Default.PlayCircleOutline
                    "AUDIO" -> Icons.Default.VolumeUp
                    "PDF" -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.Description
                },
                contentDescription = "Play/View",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
//@Composable
//private fun FreeContentItem(
//    content: Content,
//    course: Course,
//    index: Int,
//    navController: NavController
//) {
//    val context = LocalContext.current
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                handleContentClick(
//                    content = content,
//                    course = course,
//                    navController = navController,
//                    context = context
//                )
//            }
//            .padding(vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Index circle
//        Surface(
//            color = MaterialTheme.colorScheme.tertiaryContainer,
//            shape = CircleShape,
//            modifier = Modifier.size(28.dp)
//        ) {
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                Text(
//                    text = index.toString(),
//                    style = MaterialTheme.typography.labelSmall,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        // Content info
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = "Lesson $index",
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.Medium
//            )
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Surface(
//                    color = MaterialTheme.colorScheme.tertiaryContainer,
//                    shape = RoundedCornerShape(4.dp)
//                ) {
//                    Text(
//                        text = "FREE",
//                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onTertiaryContainer,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                if (content.is_offline_available == "Yes") {
//                    Icon(
//                        imageVector = Icons.Outlined.DownloadDone,
//                        contentDescription = "Available Offline",
//                        modifier = Modifier.size(16.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//
//        // Play/View Icon
//        Icon(
//            imageVector = when (content.content_type?.uppercase()) {
//                "VIDEO" -> Icons.Default.PlayCircleOutline
//                "AUDIO" -> Icons.Default.VolumeUp
//                "PDF" -> Icons.Default.PictureAsPdf
//                else -> Icons.Default.Description
//            },
//            contentDescription = "Play/View",
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(24.dp)
//        )
//    }
//}

// Updated handleContentClick function
private fun handleContentClick(
    content: Content,
    course: Course,
    navController: NavController,
    context: android.content.Context
) {
    try {
        // Since all content here is free, we can directly handle the click
        when (content.content_type?.uppercase()) {
            "VIDEO" -> {
                content.content_url?.let { url ->
                    openVideoPlayer(navController, course, url)
                } ?: showToast(context, "Video URL not available")
            }

            "AUDIO" -> {
                content.content_url?.let { url ->
                    openVideoPlayer(navController, course, url)
                } ?: showToast(context, "Audio URL not available")
            }

            "PDF" -> {
                content.content_url?.let { url ->
                    openPDFReader(navController, course, url)
                } ?: showToast(context, "PDF URL not available")
            }

            else -> {
                // Open in browser or default viewer
                showToast(context, "Opening ${content.content_type ?: "content"}...")
            }
        }
    } catch (e: Exception) {
        showToast(context, "Error opening content: ${e.message}")
    }
}

@Composable
private fun EmptyContentView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.VideoLibrary,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "No Free Content Available",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Check back later for free content",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

// Helper functions
private fun getContentTypeIcon(type: String?): ImageVector {
    return when (type?.uppercase()) {
        "VIDEO" -> Icons.Outlined.VideoLibrary
        "AUDIO" -> Icons.Outlined.Audiotrack
        "PDF" -> Icons.Outlined.PictureAsPdf
        "DOCUMENT" -> Icons.Outlined.Description
        else -> Icons.Outlined.Article
    }
}

private fun getContentTypeTitle(type: String?): String {
    return when (type?.uppercase()) {
        "VIDEO" -> "Video Lectures"
        "AUDIO" -> "Audio Lessons"
        "PDF" -> "PDF Materials"
        "DOCUMENT" -> "Documents"
        else -> "Study Materials"
    }
}