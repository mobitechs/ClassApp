package com.mobitechs.classapp.utils

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.NavController
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import java.io.File

object SecureContentPlayer {

    /**
     * Play video/audio content from secure storage
     */
    fun playSecureContent(
        context: Context,
        downloadedContent: DownloadContent,
        secureDownloadManager: SecureDownloadManager
    ): ExoPlayer? {
        return try {
            // Get decrypted file
            val decryptedFile = secureDownloadManager.getDecryptedFile(downloadedContent)
                ?: return null

            // Create ExoPlayer instance
            val exoPlayer = ExoPlayer.Builder(context).build()

            // Create media item from decrypted file
            val mediaItem = MediaItem.fromUri(Uri.fromFile(decryptedFile))

            // Prepare player
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            // Add listener to clean up temp file when playback ends
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        // Delete temp file
                        decryptedFile.delete()
                    }
                }
            })

            exoPlayer

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Open PDF content from secure storage
     */
    fun openSecurePDF(
        context: Context,
        downloadedContent: DownloadContent,
        secureDownloadManager: SecureDownloadManager,
        onError: (String) -> Unit
    ) {
        try {
            val decryptedFile = secureDownloadManager.getDecryptedFile(downloadedContent)
            if (decryptedFile == null) {
                onError("Could not decrypt PDF file")
                return
            }

            // For PDFs, you'll need to use a PDF viewer that can load from file
            // or implement an in-app PDF viewer
            // Here's a basic approach using a custom PDF viewer

            // Option 1: Use AndroidPdfViewer library
            // Option 2: Use WebView to display PDF
            // Option 3: Create custom activity with PDF renderer

            // Clean up temp file after some delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                decryptedFile.delete()
            }, 300000) // Delete after 5 minutes

        } catch (e: Exception) {
            onError("Error opening PDF: ${e.message}")
        }
    }
}

/**
 * Composable for secure video player
 */
@Composable
fun SecureVideoPlayer(
    downloadedContent: DownloadContent,
    secureDownloadManager: SecureDownloadManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        SecureContentPlayer.playSecureContent(context, downloadedContent, secureDownloadManager)
    }

    DisposableEffect(Unit) {
        exoPlayer?.play()

        onDispose {
            exoPlayer?.release()
            // Clear any temp files
            secureDownloadManager.clearTempFiles()
        }
    }

    // Your existing video player UI here
    // Use exoPlayer for playback controls
}

/**
 * Extension functions for navigation with secure content
 */
fun openSecureVideoPlayer(
    navController: NavController,
    downloadedContent: DownloadContent
) {
    // Navigate to secure video player screen
    // Pass downloadedContent ID as parameter
    navController.navigate("secureVideoPlayer/${downloadedContent.id}")
}

fun openSecurePDFReader(
    navController: NavController,
    downloadedContent: DownloadContent
) {
    // Navigate to secure PDF reader screen
    navController.navigate("securePdfReader/${downloadedContent.id}")
}