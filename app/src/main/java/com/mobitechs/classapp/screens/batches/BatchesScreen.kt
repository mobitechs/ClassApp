package com.mobitechs.classapp.screens.batches


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.classapp.data.model.Batch
import com.mobitechs.classapp.data.model.StudyMaterial
import com.mobitechs.classapp.screens.home.ErrorView
import com.mobitechs.classapp.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchesScreen(
    navController: NavController,
    viewModel: BatchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showJoinBatchDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    // Filter options
    val filterOptions = listOf(
        "All" to "",
        "New" to Constants.FILTER_NEW,
        "Free" to Constants.FILTER_FREE,
        "Trending" to Constants.FILTER_TRENDING
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Batches") },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showJoinBatchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Join Batch",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter dropdown menu
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                filterOptions.forEach { (name, value) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            viewModel.applyFilter(value)
                            showFilterMenu = false
                        },
                        leadingIcon = {
                            if (uiState.selectedFilter == value) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }

            // Main content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (uiState.error.isNotEmpty()) {
                ErrorView(
                    message = uiState.error,
                    onRetry = { viewModel.loadBatches() }
                )
            } else if (uiState.batches.isEmpty()) {
                // Empty state - no batches
                NoBatchesView(
                    onJoinBatch = { showJoinBatchDialog = true }
                )
            } else {
                // Batches content
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Batch selection
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.batches) { batch ->
                            BatchItem(
                                batch = batch,
                                isSelected = batch.id == uiState.selectedBatchId,
                                onClick = { viewModel.selectBatch(batch.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected batch name and search
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = uiState.selectedBatchName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search study materials...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Material type tabs
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                viewModel.setMaterialType("")
                            },
                            text = { Text("All") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                viewModel.setMaterialType(Constants.MATERIAL_TYPE_VIDEO)
                            },
                            text = { Text("Videos") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                                viewModel.setMaterialType(Constants.MATERIAL_TYPE_PDF)
                            },
                            text = { Text("PDFs") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    // Study materials list
                    if (uiState.studyMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(64.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No study materials found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.studyMaterials) { material ->
                                StudyMaterialItem(
                                    material = material,
                                    onClick = {
                                        navController.navigate("study_material/${material.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Join Batch Dialog
            if (showJoinBatchDialog) {
                JoinBatchDialog(
                    batchCode = uiState.batchCode,
                    onBatchCodeChange = { viewModel.updateBatchCode(it) },
                    onJoinClick = {
                        viewModel.joinBatchByCode()
                        if (uiState.batchCodeError.isEmpty()) {
                            showJoinBatchDialog = false
                        }
                    },
                    onDismiss = { showJoinBatchDialog = false },
                    isLoading = uiState.isJoiningBatch,
                    error = uiState.batchCodeError
                )
            }
        }
    }
}

@Composable
fun NoBatchesView(
    onJoinBatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You haven't joined any batches yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Join a batch to access study materials",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onJoinBatch,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Join a Batch")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchItem(
    batch: Batch,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            // Batch image or icon
            if (batch.coverImage != null) {
                AsyncImage(
                    model = batch.coverImage,
                    contentDescription = batch.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = if (isSelected) 1f else 0.7f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = batch.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Batch name
            Text(
                text = batch.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            // Student count
            Text(
                text = "${batch.totalStudents} students",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialItem(
    material: StudyMaterial,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or Icon
            if (material.thumbnailUrl != null) {
                AsyncImage(
                    model = material.thumbnailUrl,
                    contentDescription = material.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (material.type) {
                                Constants.MATERIAL_TYPE_VIDEO -> MaterialTheme.colorScheme.primaryContainer
                                Constants.MATERIAL_TYPE_PDF -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (material.type) {
                            Constants.MATERIAL_TYPE_VIDEO -> Icons.Default.VideoLibrary
                            Constants.MATERIAL_TYPE_PDF -> Icons.Default.PictureAsPdf
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = when (material.type) {
                            Constants.MATERIAL_TYPE_VIDEO -> MaterialTheme.colorScheme.onPrimaryContainer
                            Constants.MATERIAL_TYPE_PDF -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = material.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description if available
                if (!material.description.isNullOrEmpty()) {
                    Text(
                        text = material.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Info row (file size, duration, etc.)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // File type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (material.type) {
                            Constants.MATERIAL_TYPE_VIDEO -> MaterialTheme.colorScheme.primaryContainer
                            Constants.MATERIAL_TYPE_PDF -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                    ) {
                        Text(
                            text = material.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (material.type) {
                                Constants.MATERIAL_TYPE_VIDEO -> MaterialTheme.colorScheme.onPrimaryContainer
                                Constants.MATERIAL_TYPE_PDF -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // File size if available
                    if (material.fileSize != null) {
                        Text(
                            text = formatFileSize(material.fileSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Duration if available
                    if (material.duration != null && material.type == Constants.MATERIAL_TYPE_VIDEO) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = formatDuration(material.duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Download indicator
                    if (material.isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloaded",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JoinBatchDialog(
    batchCode: String,
    onBatchCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Join a Batch",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the batch code provided by your instructor",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = batchCode,
                    onValueChange = onBatchCodeChange,
                    label = { Text("Batch Code") },
                    isError = error.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = error.isNotEmpty()) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onJoinClick,
                enabled = !isLoading && batchCode.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// Helper functions
private fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    return when {
        kb < 1024 -> "%.1f KB".format(kb)
        else -> "%.1f MB".format(kb / 1024.0)
    }
}

private fun formatDuration(durationSeconds: Long): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}