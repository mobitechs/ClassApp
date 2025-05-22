package com.mobitechs.classapp.screens.VideoPlayer
// VideoPlayerViewModel.kt
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.PlaybackException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log

class VideoPlayerViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var exoPlayer: ExoPlayer? = null
        private set

    private var isPlayerInitialized = false
    private var currentVideoUrl: String? = null
    private var savedPosition: Long = 0L
    private var wasPlaying = false

    fun initializePlayer(context: Context) {
        if (exoPlayer == null && !isPlayerInitialized) {
            exoPlayer = ExoPlayer.Builder(context)
                .build()
                .apply {
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                            wasPlaying = isPlaying
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            _isLoading.value = playbackState == Player.STATE_BUFFERING
                            if (playbackState == Player.STATE_READY) {
                                _duration.value = duration
                                // Restore saved position if available
                                if (savedPosition > 0) {
                                    seekTo(savedPosition)
                                    savedPosition = 0L
                                }
                            }
                        }

                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            Log.d("VideoPlayer", "Video size: ${videoSize.width} x ${videoSize.height}")
                        }

                        override fun onRenderedFirstFrame() {
                            Log.d("VideoPlayer", "First frame rendered")
                            _isLoading.value = false
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("VideoPlayer", "Player error: ${error.message}")
                        }
                    })
                }
            isPlayerInitialized = true
            startPositionUpdates()
        }
    }

    fun setVideoUrl(url: String) {
        if (currentVideoUrl != url) {
            currentVideoUrl = url
            exoPlayer?.let { player ->
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .build()
                player.setMediaItem(mediaItem)
                player.prepare()
                // Don't auto-play, let user control
                player.playWhenReady = false
            }
        }
    }

    fun playPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    // Save state for orientation changes
    fun saveState() {
        exoPlayer?.let { player ->
            savedPosition = player.currentPosition
            wasPlaying = player.isPlaying
        }
    }

    // Restore state after orientation change
    fun restoreState() {
        exoPlayer?.let { player ->
            if (savedPosition > 0) {
                player.seekTo(savedPosition)
                savedPosition = 0L
            }
            if (wasPlaying) {
                player.play()
            }
        }
    }

    // Pause player (for lifecycle events)
    fun pausePlayer() {
        exoPlayer?.let { player ->
            wasPlaying = player.isPlaying
            if (player.isPlaying) {
                player.pause()
            }
        }
    }

    // Resume player (for lifecycle events)
    fun resumePlayer() {
        if (wasPlaying) {
            exoPlayer?.play()
        }
    }

    // Release player completely
    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        isPlayerInitialized = false
        currentVideoUrl = null
        savedPosition = 0L
        wasPlaying = false
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (isPlayerInitialized && exoPlayer != null) {
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                }
                delay(1000) // Update every second
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}