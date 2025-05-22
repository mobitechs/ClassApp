package com.mobitechs.classapp.screens.VideoPlayer

// 2. VideoPlayerViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun initializePlayer(context: android.content.Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isLoading.value = playbackState == Player.STATE_BUFFERING
                        if (playbackState == Player.STATE_READY) {
                            _duration.value = duration
                        }
                    }
                })
            }
            startPositionUpdates()
        }
    }

    fun setVideoUrl(url: String) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
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

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                }
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}
