package com.mobitechs.classapp.screens.freeContent

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
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
                course.course_tags != null || course.image != null
            ) {
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
//
//@Composable
//private fun FreeContentItem(
//    content: Content,
//    course: Course,
//    index: Int,
//    navController: NavController,
//    viewModel: FreeContentViewModel
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    var isDownloaded by remember { mutableStateOf(false) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    val isDownloading = viewModel.isDownloading(content.id)
//    val downloadProgress = viewModel.getDownloadProgress(content.id)
//
//    // Check if content is downloaded
//    LaunchedEffect(content.id) {
//        isDownloaded = viewModel.isContentDownloaded(content.id)
//    }
//
//    // Refresh download status periodically when downloading
//    LaunchedEffect(isDownloading) {
//        if (isDownloading) {
//            while (isDownloading) {
//                kotlinx.coroutines.delay(1000)
//                isDownloaded = viewModel.isContentDownloaded(content.id)
//            }
//        }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(enabled = !isDownloading) {
//                if (isDownloaded) {
//                    handleContentClick(
//                        content = content,
//                        course = course,
//                        navController = navController,
//                        context = context,
//                        viewModel = viewModel
//                    )
//                } else {
//                    showToast(context, "Please download the content first")
//                }
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
//                // Show downloaded indicator
//                if (isDownloaded && !isDownloading) {
//                    Icon(
//                        imageVector = Icons.Outlined.DownloadDone,
//                        contentDescription = "Downloaded",
//                        modifier = Modifier.size(16.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            // Show progress bar when downloading
//            if (isDownloading) {
//                Spacer(modifier = Modifier.height(4.dp))
//                LinearProgressIndicator(
//                    progress = downloadProgress / 100f,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(4.dp),
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//                Text(
//                    text = "$downloadProgress%",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//
//        // Action buttons
//        when {
//            isDownloading -> {
//                // Cancel button
//                IconButton(
//                    onClick = {
//                        viewModel.cancelDownload(content.id)
//                        showToast(context, "Download cancelled")
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Cancel,
//                        contentDescription = "Cancel Download",
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
//
//            isDownloaded -> {
//                // Delete button
//                IconButton(
//                    onClick = {
//                        showDeleteDialog = true
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Delete,
//                        contentDescription = "Delete Download",
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
//
//            else -> {
//                // Download button
//                IconButton(
//                    onClick = {
//                        viewModel.downloadContent(content, course)
//                        showToast(context, "Download started")
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Download,
//                        contentDescription = "Download",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//
//    // Delete confirmation dialog
//    if (showDeleteDialog) {
//        AlertDialog(
//            onDismissRequest = { showDeleteDialog = false },
//            title = { Text("Delete Download") },
//            text = { Text("Are you sure you want to delete this downloaded content?") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        scope.launch {
//                            viewModel.deleteDownload(content.id)
//                            isDownloaded = false
//                            showDeleteDialog = false
//                            showToast(context, "Download deleted")
//                        }
//                    }
//                ) {
//                    Text("Delete")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showDeleteDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//}

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
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDownloading = viewModel.isDownloading(content.id)
    val downloadProgress = viewModel.getDownloadProgress(content.id)

    // Check if content is downloaded initially
    LaunchedEffect(content.id) {
        isDownloaded = viewModel.isContentDownloaded(content.id)
    }

    // IMPORTANT: Monitor download completion
    LaunchedEffect(isDownloading, downloadProgress) {
        // When download completes (progress reaches 100% and isDownloading becomes false)
        if (!isDownloading && downloadProgress == 100) {
            // Small delay to ensure download is fully completed
            kotlinx.coroutines.delay(500)
            isDownloaded = viewModel.isContentDownloaded(content.id)
        }
    }

    // Also check download status when isDownloading changes from true to false
    LaunchedEffect(isDownloading) {
        if (!isDownloading) {
            // Check if download was completed (not cancelled)
            scope.launch {
                kotlinx.coroutines.delay(300) // Small delay for state to settle
                isDownloaded = viewModel.isContentDownloaded(content.id)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDownloading) {
                if (isDownloaded) {
                    handleContentClick(
                        content = content,
                        course = course,
                        navController = navController,
                        context = context,
                        viewModel = viewModel
                    )
                } else {
                    showToast(context, "Please download the content first")
                }
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

                // Show downloaded indicator
                if (isDownloaded && !isDownloading) {
                    Icon(
                        imageVector = Icons.Outlined.DownloadDone,
                        contentDescription = "Downloaded",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Show progress bar when downloading
            if (isDownloading) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = downloadProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$downloadProgress%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action buttons
        when {
            isDownloading -> {
                // Cancel button
                IconButton(
                    onClick = {
                        viewModel.cancelDownload(content.id)
                        showToast(context, "Download cancelled")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel Download",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            isDownloaded -> {
                // Delete button
                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Download",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                // Download button
                IconButton(
                    onClick = {
                        viewModel.downloadContent(content, course)
                        showToast(context, "Download started")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Download") },
            text = { Text("Are you sure you want to delete this downloaded content?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteDownload(content.id)
                            isDownloaded = false
                            showDeleteDialog = false
                            showToast(context, "Download deleted")
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun handleContentClick(
    content: Content,
    course: Course,
    navController: NavController,
    context: android.content.Context,
    viewModel: FreeContentViewModel
) {
    try {
        // Check if content is downloaded
        viewModel.viewModelScope.launch {
            val isDownloaded = viewModel.isContentDownloaded(content.id)

            if (isDownloaded) {
                // Content is downloaded, play from secure storage
                val downloadedContent = viewModel.getDownloadedContent(content.id)
                if (downloadedContent != null) {

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
                } else {
                    showToast(context, "Error loading downloaded content")
                }
            } else {
                // Content not downloaded, stream from URL (if allowed)
                // Or prompt user to download first
                showToast(context, "Please download the content first to play offline")
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