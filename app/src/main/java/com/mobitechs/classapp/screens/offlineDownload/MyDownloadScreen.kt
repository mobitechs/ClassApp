package com.mobitechs.classapp.screens.offlineDownload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.utils.formatFileSize
import com.mobitechs.classapp.utils.openPDFReader
import com.mobitechs.classapp.utils.openVideoPlayer
import com.mobitechs.classapp.utils.showToast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDownloadsScreen(
    navController: NavController,
    viewModel: MyDownloadsViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var contentToDelete by remember { mutableStateOf<DownloadContent?>(null) }

    // Show error if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            showToast(context, error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Downloads",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.downloads.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                showDeleteDialog = true
                                contentToDelete = null // null means delete all
                            }
                        ) {
                            Icon(Icons.Default.DeleteSweep, "Clear All")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.downloads.isEmpty()) {
            // Empty state
            EmptyDownloadsView(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Downloads list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stats Header
                item {
                    DownloadStatsCard(
                        totalDownloads = uiState.downloads.size,
                        totalSize = uiState.totalSize,
                        totalCourses = uiState.totalCourses
                    )
                }

                // Downloads grouped by course
                uiState.groupedDownloads.forEach { (courseId, courseDownloads) ->
                    item {
                        CourseDownloadSection(
                            courseId = courseId,
                            courseName = courseDownloads.firstOrNull()?.course_name
                                ?: "Course $courseId",
                            downloads = courseDownloads,
                            isExpanded = uiState.expandedCourseIds.contains(courseId),
                            onToggleExpanded = { viewModel.toggleCourseExpanded(courseId) },
                            onItemClick = { downloadedContent ->
                                if (viewModel.checkFileExists(downloadedContent)) {
                                    handleDownloadClick(downloadedContent, navController, context)
                                } else {
                                    showToast(context, "File not found. It may have been deleted.")
                                    viewModel.deleteDownload(downloadedContent)
                                }
                            },
                            onDeleteClick = { downloadedContent ->
                                contentToDelete = downloadedContent
                                showDeleteDialog = true
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            title = {
                Text(if (contentToDelete == null) "Clear All Downloads" else "Delete Download")
            },
            text = {
                Text(
                    if (contentToDelete == null)
                        "Are you sure you want to delete all downloaded content? This action cannot be undone."
                    else
                        "Are you sure you want to delete this downloaded content? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (contentToDelete == null) {
                            viewModel.deleteAllDownloads()
                            showToast(context, "All downloads cleared")
                        } else {
                            viewModel.deleteDownload(contentToDelete!!)
                            showToast(context, "Download deleted")
                        }
                        showDeleteDialog = false
                        contentToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun DownloadStatsCard(
    totalDownloads: Int,
    totalSize: Long,
    totalCourses: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
            StatItem(
                icon = Icons.Default.Download,
                value = "$totalDownloads",
                label = "Downloads"
            )

            VerticalDivider()

            StatItem(
                icon = Icons.Default.Storage,
                value = formatFileSize(totalSize),
                label = "Total Size"
            )

            VerticalDivider()

            StatItem(
                icon = Icons.Default.School,
                value = "$totalCourses",
                label = "Courses"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VerticalDivider() {
    Divider(
        modifier = Modifier
            .height(60.dp)
            .width(1.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
    )
}

@Composable
private fun CourseDownloadSection(
    courseId: Int,
    courseName: String,
    downloads: List<DownloadContent>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onItemClick: (DownloadContent) -> Unit,
    onDeleteClick: (DownloadContent) -> Unit,
    viewModel: MyDownloadsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Course Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val totalSize = downloads.sumOf { viewModel.getFileSize(it) }
                    Text(
                        text = "${downloads.size} items • ${formatFileSize(totalSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Content List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    downloads.forEachIndexed { index, download ->
                        DownloadedContentItem(
                            download = download,
                            index = index + 1,
                            fileSize = viewModel.getFileSize(download),
                            onItemClick = { onItemClick(download) },
                            onDeleteClick = { onDeleteClick(download) }
                        )

                        if (index < downloads.size - 1) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadedContentItem(
    download: DownloadContent,
    index: Int,
    fileSize: Long,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Content Type Icon
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getContentTypeIcon(download.content_type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Lesson $index - ${download.content_type ?: "Content"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // File size
                if (fileSize > 0) {
                    Text(
                        text = formatFileSize(fileSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Download date
                Text(
                    text = "• ${formatDate(download.downloadedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Delete button
        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyDownloadsView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DownloadDone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "No Downloads Yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Download content to access it offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("freeContentScreen")
                }
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Free Content")
            }
        }
    }
}

// Helper functions
private fun handleDownloadClick(
    download: DownloadContent,
    navController: NavController,
    context: android.content.Context
) {
    val filePath = download.downloadedFilePath ?: return

    // Create minimal Course object for navigation

    val course = Course(
        id = download.course_id,
        course_name = "${download.course_name}",
        course_description = null, // Keep blank
        course_price = "0",
        course_discounted_price = "0",
        course_like = 0, // No likes to show
        course_tags = null, // No tags
        category_id = null,
        category_name = null,
        sub_category_id = null,
        sub_category_name = null,
        subject_id = null,
        subject_name = null,
        image = null, // No image
        instructor = null, // No instructor
        course_duration = null,
        course_expiry_date = null,
        offer_code = null,
        is_favourited = false,
        isPurchased = false,
        is_in_wishlist = false,
        is_liked = false,
        is_active = "Active",
        created_at = null,
        updated_at = null,
        deleted_at = null,
        added_by = null,
        lastSyncedAt = System.currentTimeMillis()
    )

    try {
        when (download.content_type?.uppercase()) {
            "VIDEO", "AUDIO" -> {
                openVideoPlayer(navController, course, filePath)
            }

            "PDF" -> {
                openPDFReader(navController, course, filePath)
            }

            else -> {
                showToast(context, "Opening ${download.content_type ?: "content"}...")
            }
        }
    } catch (e: Exception) {
        showToast(context, "Error opening content: ${e.message}")
    }
}

private fun getContentTypeIcon(type: String?): ImageVector {
    return when (type?.uppercase()) {
        "VIDEO" -> Icons.Outlined.VideoLibrary
        "AUDIO" -> Icons.Outlined.Audiotrack
        "PDF" -> Icons.Outlined.PictureAsPdf
        else -> Icons.Outlined.Article
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}