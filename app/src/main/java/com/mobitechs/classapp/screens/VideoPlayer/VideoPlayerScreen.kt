package com.mobitechs.classapp.screens.VideoPlayer

// Core Media3 imports
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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
    val gson = Gson()

    // FIXED: Get course from SharedPreferences instead of JSON parameter
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

    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Initialize player
    LaunchedEffect(videoUrl) {
        viewModel.initializePlayer(context)
        videoUrl?.let {
            val decodedUrl = URLDecoder.decode(it, "UTF-8")
            Log.d("VideoPlayer", "Setting video URL: $decodedUrl")
            viewModel.setVideoUrl(decodedUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (isFullscreen) {
                    Modifier.systemBarsPadding()
                } else {
                    Modifier.statusBarsPadding()
                }
            )
    ) {
        // FIXED: Video Player with proper AndroidView setup
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false // Custom controls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    // Don't set player here - do it in update
                }
            },
            update = { playerView ->
                // IMPORTANT: Set player in update block, not factory
                playerView.player = viewModel.exoPlayer
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    showControls = !showControls
                }
        )

        // Top Controls
        AnimatedVisibility(
            visible = showControls && !isFullscreen,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Fullscreen toggle
                    IconButton(
                        onClick = { isFullscreen = !isFullscreen }
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
        }

        // Bottom Controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            VideoControlsOverlay(
                isPlaying = isPlaying,
                isLoading = isLoading,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPause = { viewModel.playPause() },
                onSeek = { position -> viewModel.seekTo(position) },
                course = course,
                isFullscreen = isFullscreen,
                onPurchase = {
                    // Navigate back and trigger purchase
                    navController.popBackStack()
                }
            )
        }

        // ENHANCED: Loading indicator with text
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading video...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Center play/pause button (when paused)
        if (!isPlaying && !isLoading) {
            IconButton(
                onClick = {
                    viewModel.playPause()
                    showControls = false
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // ADDED: Debug info (remove in production)
//        if (BuildConfig.DEBUG) {
//            Text(
//                text = "URL: ${videoUrl?.take(30)}...\nDuration: ${formatTime(duration)}\nPosition: ${
//                    formatTime(
//                        currentPosition
//                    )
//                }",
//                color = Color.Green,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(16.dp)
//                    .background(Color.Black.copy(alpha = 0.7f))
//                    .padding(8.dp)
//            )
//        }
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

// ADDED: Simple test version (use this to debug)
@Composable
fun SimpleVideoPlayerTest(videoUrl: String) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                val exoPlayer = ExoPlayer.Builder(context).build()
                val mediaItem = MediaItem.fromUri(videoUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}