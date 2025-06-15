//// 1. Common Download Button Component
//package com.mobitechs.classapp.screens.common
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import com.mobitechs.classapp.data.local.AppDatabase
//import com.mobitechs.classapp.data.model.response.Content
//import com.mobitechs.classapp.data.model.response.Course
//import com.mobitechs.classapp.utils.SimpleDownloadManager
//import com.mobitechs.classapp.utils.showToast
//import kotlinx.coroutines.launch
//
///**
// * Common Download Button that can be used in any screen
// *
// * @param content The content to download
// * @param course The course this content belongs to (optional)
// * @param modifier Modifier for customization
// * @param size Size of the button/icon (default 24.dp)
// * @param showText Whether to show text alongside icon
// * @param onDownloadStart Called when download starts
// * @param onDownloadComplete Called when download is complete
// * @param onError Called when error occurs
// */
//@Composable
//fun CommonDownloadButton(
//    content: Content,
//    course: Course? = null,
//    modifier: Modifier = Modifier,
//    size: Dp = 24.dp,
//    showText: Boolean = false,
//    onDownloadStart: (() -> Unit)? = null,
//    onDownloadComplete: (() -> Unit)? = null,
//    onError: ((String) -> Unit)? = null
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val downloadManager = remember { SimpleDownloadManager(context) }
//
//    var isDownloaded by remember { mutableStateOf(false) }
//    var isDownloading by remember { mutableStateOf(false) }
//
//    // Check download status on composition
//    LaunchedEffect(content.id) {
//        isDownloaded = downloadManager.isContentDownloaded(content.id)
//    }
//
//    when {
//        isDownloading -> {
//            // Downloading state
//            if (showText) {
//                Button(
//                    onClick = { },
//                    enabled = false,
//                    modifier = modifier
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(16.dp),
//                        strokeWidth = 2.dp
//                    )
//                    if (showText) {
//                        Text(" Downloading...", modifier = Modifier.padding(start = 8.dp))
//                    }
//                }
//            } else {
//                Box(modifier = modifier, contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(size),
//                        strokeWidth = 2.dp
//                    )
//                }
//            }
//        }
//
//        isDownloaded -> {
//            // Downloaded state
//            if (showText) {
//                OutlinedButton(
//                    onClick = { onDownloadComplete?.invoke() },
//                    modifier = modifier,
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Icon(
//                        Icons.Default.DownloadDone,
//                        contentDescription = "Downloaded",
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(" Downloaded", modifier = Modifier.padding(start = 8.dp))
//                }
//            } else {
//                IconButton(
//                    onClick = { onDownloadComplete?.invoke() },
//                    modifier = modifier
//                ) {
//                    Icon(
//                        Icons.Default.DownloadDone,
//                        contentDescription = "Downloaded",
//                        modifier = Modifier.size(size),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//
//        else -> {
//            // Not downloaded state
//            if (showText) {
//                Button(
//                    onClick = {
//                        scope.launch {
//                            try {
//                                isDownloading = true
//                                onDownloadStart?.invoke()
//
//                                // If course is not provided, create a minimal one
//                                val courseForDownload = course ?: Course(
//                                    id = content.course_id,
//                                    course_name = "Course ${content.course_id}",
//                                    course_like = 0,
//                                    course_price = "0",
//                                    course_discounted_price = "0",
//                                    is_active = "Active"
//                                )
//
//                                downloadManager.downloadContent(content, courseForDownload)
//                                showToast(context, "Download started")
//
//                                // Check download status after delay
//                                kotlinx.coroutines.delay(3000)
//                                isDownloaded = downloadManager.isContentDownloaded(content.id)
//                                isDownloading = false
//
//                                if (isDownloaded) {
//                                    onDownloadComplete?.invoke()
//                                }
//                            } catch (e: Exception) {
//                                isDownloading = false
//                                val errorMsg = e.message ?: "Download failed"
//                                onError?.invoke(errorMsg)
//                                showToast(context, errorMsg)
//                            }
//                        }
//                    },
//                    modifier = modifier
//                ) {
//                    Icon(
//                        Icons.Default.Download,
//                        contentDescription = "Download",
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(" Download", modifier = Modifier.padding(start = 8.dp))
//                }
//            } else {
//                IconButton(
//                    onClick = {
//                        scope.launch {
//                            try {
//                                isDownloading = true
//                                onDownloadStart?.invoke()
//
//                                val courseForDownload = course ?: Course(
//                                    id = content.course_id,
//                                    course_name = "Course ${content.course_id}",
//                                    course_like = 0,
//                                    course_price = "0",
//                                    course_discounted_price = "0",
//                                    is_active = "Active"
//                                )
//
//                                downloadManager.downloadContent(content, courseForDownload)
//                                showToast(context, "Download started")
//
//                                kotlinx.coroutines.delay(3000)
//                                isDownloaded = downloadManager.isContentDownloaded(content.id)
//                                isDownloading = false
//
//                                if (isDownloaded) {
//                                    onDownloadComplete?.invoke()
//                                }
//                            } catch (e: Exception) {
//                                isDownloading = false
//                                val errorMsg = e.message ?: "Download failed"
//                                onError?.invoke(errorMsg)
//                                showToast(context, errorMsg)
//                            }
//                        }
//                    },
//                    modifier = modifier
//                ) {
//                    Icon(
//                        Icons.Default.Download,
//                        contentDescription = "Download",
//                        modifier = Modifier.size(size),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}
//
//// 2. Variations of the download button for different use cases
//
///**
// * Small download icon button (default)
// */
//@Composable
//fun DownloadIconButton(
//    content: Content,
//    course: Course? = null,
//    onComplete: (() -> Unit)? = null
//) {
//    CommonDownloadButton(
//        content = content,
//        course = course,
//        showText = false,
//        size = 24.dp,
//        onDownloadComplete = onComplete
//    )
//}
//
///**
// * Download button with text
// */
//@Composable
//fun DownloadTextButton(
//    content: Content,
//    course: Course? = null,
//    onComplete: (() -> Unit)? = null
//) {
//    CommonDownloadButton(
//        content = content,
//        course = course,
//        showText = true,
//        onDownloadComplete = onComplete
//    )
//}
//
///**
// * Floating Action Button style download
// */
//@Composable
//fun DownloadFAB(
//    content: Content,
//    course: Course? = null,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val downloadManager = remember { SimpleDownloadManager(context) }
//
//    var isDownloaded by remember { mutableStateOf(false) }
//
//    LaunchedEffect(content.id) {
//        isDownloaded = downloadManager.isContentDownloaded(content.id)
//    }
//
//    if (!isDownloaded) {
//        FloatingActionButton(
//            onClick = {
//                scope.launch {
//                    val courseForDownload = course ?: Course(
//                        id = content.course_id,
//                        course_name = "Course ${content.course_id}",
//                        course_like = 0,
//                        course_price = "0",
//                        course_discounted_price = "0",
//                        is_active = "Active"
//                    )
//                    downloadManager.downloadContent(content, courseForDownload)
//                    showToast(context, "Download started")
//                }
//            },
//            modifier = modifier
//        ) {
//            Icon(Icons.Default.Download, contentDescription = "Download")
//        }
//    }
//}