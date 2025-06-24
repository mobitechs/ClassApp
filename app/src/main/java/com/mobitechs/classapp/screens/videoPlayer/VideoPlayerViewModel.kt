package com.mobitechs.classapp.screens.videoPlayer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.security.SecureStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

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
                                if (savedPosition > 0) {
                                    seekTo(savedPosition)
                                    savedPosition = 0L
                                }
                            }
                        }

                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            Log.d(
                                "VideoPlayer",
                                "Video size: ${videoSize.width} x ${videoSize.height}"
                            )
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
                player.playWhenReady = false
            }
        }
    }

    // Load secure encrypted content
    @OptIn(UnstableApi::class)
    fun loadSecureContent(context: Context, contentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(context)
                val download = database.contentDao().getDownloadByContentId(contentId)

                download?.downloadedFilePath?.let { filePath ->
                    val encryptedFile = File(filePath)
                    if (encryptedFile.exists()) {
                        val secureStorage = SecureStorageManager(context)
                        val inputStream = secureStorage.getDecryptedInputStream(encryptedFile)

                        if (inputStream != null) {
                            // Create custom data source for encrypted content
                            val dataSource = EncryptedDataSource(inputStream)
                            val mediaSource = ProgressiveMediaSource.Factory { dataSource }
                                .createMediaSource(MediaItem.fromUri(Uri.EMPTY))

                            exoPlayer?.apply {
                                setMediaSource(mediaSource)
                                prepare()
                                playWhenReady = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Error loading secure content: ${e.message}")
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

    fun pausePlayer() {
        exoPlayer?.let { player ->
            wasPlaying = player.isPlaying
            if (player.isPlaying) {
                player.pause()
            }
        }
    }

    fun resumePlayer() {
        if (wasPlaying) {
            exoPlayer?.play()
        }
    }

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
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

// Custom data source for encrypted content
@UnstableApi
class EncryptedDataSource(private val inputStream: InputStream) : DataSource {
    private var opened = false

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun open(dataSpec: DataSpec): Long {
        opened = true
        return inputStream.available().toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return inputStream.read(buffer, offset, length)
    }

    override fun getUri(): Uri? = null

    override fun close() {
        if (opened) {
            inputStream.close()
            opened = false
        }
    }
}