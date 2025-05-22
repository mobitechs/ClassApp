package com.mobitechs.classapp.screens.common
// 3. VideoPlayerComposable.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.mobitechs.classapp.screens.VideoPlayer.VideoPlayerViewModel

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Initialize player when composable is first created
    LaunchedEffect(Unit) {
        viewModel.initializePlayer(context)
        viewModel.setVideoUrl(videoUrl)
    }

    Box(modifier = modifier) {
        // ExoPlayer View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.exoPlayer
                    useController = false // We'll create custom controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Controls Overlay
        VideoControls(
            isPlaying = isPlaying,
            isLoading = isLoading,
            currentPosition = currentPosition,
            duration = duration,
            onPlayPause = { viewModel.playPause() },
            onSeek = { position -> viewModel.seekTo(position) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun VideoControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
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

        Spacer(modifier = Modifier.height(8.dp))

        // Play/Pause Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            } else {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Utility function to format time
fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}