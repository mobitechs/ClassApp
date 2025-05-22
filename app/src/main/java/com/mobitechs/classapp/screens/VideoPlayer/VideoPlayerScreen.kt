package com.mobitechs.classapp.screens.VideoPlayer

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Course
import kotlinx.coroutines.delay
import java.net.URLDecoder

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    courseJson: String?,
    videoUrl: String?,
    navController: NavController,
    viewModel: VideoPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val configuration = LocalConfiguration.current
    val gson = Gson()

    // Get course from SharedPreferences instead of JSON parameter
    val course = remember {
        val sharedPrefs = context.getSharedPreferences("course_temp", Context.MODE_PRIVATE)
        val courseJsonFromPrefs = sharedPrefs.getString("current_course", null)
        courseJsonFromPrefs?.let {
            try {
                gson.fromJson(it, Course::class.java)
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Error parsing course: ${e.message}")
                null
            }
        }
    }

    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showControls by rememberSaveable { mutableStateOf(true) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    // Check if we're in landscape mode
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Initialize player only once and preserve across orientation changes
    LaunchedEffect(Unit) {
        if (viewModel.exoPlayer == null) {
            viewModel.initializePlayer(context)
            videoUrl?.let {
                val decodedUrl = URLDecoder.decode(it, "UTF-8")
                Log.d("VideoPlayer", "Setting video URL: $decodedUrl")
                viewModel.setVideoUrl(decodedUrl)
            }
        }
    }

    // Handle fullscreen changes without reinitializing player
    LaunchedEffect(isFullscreen) {
        if (isFullscreen && !isLandscape) {
            // Only change orientation if not already in landscape
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else if (!isFullscreen && isLandscape) {
            // Only change orientation if in landscape
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Handle orientation changes
    LaunchedEffect(isLandscape) {
        if (isLandscape && !isFullscreen) {
            isFullscreen = true
        } else if (!isLandscape && isFullscreen) {
            isFullscreen = false
        }
    }

    // Cleanup when leaving screen completely
    DisposableEffect(Unit) {
        onDispose {
            // Only release when actually leaving the screen, not on orientation change
            if (activity.isFinishing) {
                viewModel.releasePlayer()
            }
        }
    }

    // Handle back button in fullscreen
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    if (isFullscreen || isLandscape) {
        // Fullscreen Layout (Landscape)
        FullscreenLayout(
            viewModel = viewModel,
            course = course,
            showControls = showControls,
            onControlsToggle = { showControls = !showControls },
            onExitFullscreen = {
                isFullscreen = false
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            onBackPressed = { navController.popBackStack() },
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            isLoading = isLoading,
            onPlayPause = { viewModel.playPause() },
            onSeek = { viewModel.seekTo(it) }
        )
    } else {
        // YouTube-style Portrait Layout
        YouTubeStyleLayout(
            viewModel = viewModel,
            course = course,
            showControls = showControls,
            onControlsToggle = { showControls = !showControls },
            onFullscreen = { isFullscreen = true },
            onBackPressed = { navController.popBackStack() },
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            isLoading = isLoading,
            onPlayPause = { viewModel.playPause() },
            onSeek = { viewModel.seekTo(it) }
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YouTubeStyleLayout(
    viewModel: VideoPlayerViewModel,
    course: Course?,
    showControls: Boolean,
    onControlsToggle: () -> Unit,
    onFullscreen: () -> Unit,
    onBackPressed: () -> Unit,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Video Player Section (Small, like YouTube)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Fixed height like YouTube
                .background(Color.Black)
        ) {
            // Video Player
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { playerView ->
                    playerView.player = viewModel.exoPlayer
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onControlsToggle() }
            )

            // Top Bar (Back button)
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Video Controls Overlay - FIXED
            if (showControls) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    SmallVideoControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onPlayPause = onPlayPause,
                        onSeek = onSeek,
                        onFullscreen = onFullscreen
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            }

            // Center play button
            if (!isPlaying && !isLoading) {
                IconButton(
                    onClick = {
                        onPlayPause()
                        onControlsToggle()
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Course Content Section (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Course Title
            Text(
                text = course?.course_name ?: "Course Title",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Course Stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${course?.course_like ?: 0}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "1.2K students",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Price Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Get Full Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${course?.course_discounted_price ?: course?.course_price ?: "0"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            // Purchase logic - navigate back to trigger purchase
                            onBackPressed()
                        }
                    ) {
                        Text("Buy Now")
                    }
                }
            }

            // Course Description
            Text(
                text = "About This Course",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = course?.course_description ?: "Course description goes here...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Instructor Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = course?.instructor ?: "Instructor Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Course Instructor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenLayout(
    viewModel: VideoPlayerViewModel,
    course: Course?,
    showControls: Boolean,
    onControlsToggle: () -> Unit,
    onExitFullscreen: () -> Unit,
    onBackPressed: () -> Unit,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        // Fullscreen Video Player
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { playerView ->
                playerView.player = viewModel.exoPlayer
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable { onControlsToggle() }
        )

        // Top Controls - FIXED
        if (showControls) {
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                TopAppBar(
                    title = {
                        Text(
                            text = course?.course_name ?: "Video Player",
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onExitFullscreen) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Bottom Controls - FIXED
        if (showControls) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                VideoControlsOverlay(
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    currentPosition = currentPosition,
                    duration = duration,
                    onPlayPause = onPlayPause,
                    onSeek = onSeek,
                    course = course,
                    isFullscreen = true,
                    onPurchase = onBackPressed
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = Color.White
            )
        }

        // Center play button
        if (!isPlaying && !isLoading) {
            IconButton(
                onClick = {
                    onPlayPause()
                    onControlsToggle()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallVideoControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullscreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(12.dp)
    ) {
        // Progress bar
        if (duration > 0) {
            val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newPosition = (newProgress * duration).toLong()
                    onSeek(newPosition)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (duration > 0) {
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onFullscreen) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun VideoControlsOverlay(
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    course: Course?,
    isFullscreen: Boolean,
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Progress Bar
        if (duration > 0) {
            val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newPosition = (newProgress * duration).toLong()
                    onSeek(newPosition)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Course info and purchase (only show if not fullscreen)
            if (!isFullscreen) {
                course?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Get full course access",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹${it.course_discounted_price ?: it.course_price}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onPurchase,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Buy Now")
                        }
                    }
                }
            }
        }
    }
}

// Utility function for time formatting
fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}