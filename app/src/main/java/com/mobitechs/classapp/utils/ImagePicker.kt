package com.mobitechs.classapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Common Image Picker Composable
 * Returns a function that when called, opens the image picker
 */
@Composable
fun rememberImagePicker(
    onImageSelected: (String) -> Unit,
    onError: (String) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Modern photo picker (Android 13+)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val filePath = saveAndCompressImage(context, uri)
                if (filePath != null) {
                    onImageSelected(filePath)
                } else {
                    onError("Failed to save image")
                }
            }
        }
    }

    // Legacy image picker for older Android versions
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val filePath = saveAndCompressImage(context, uri)
                if (filePath != null) {
                    onImageSelected(filePath)
                } else {
                    onError("Failed to save image")
                }
            }
        }
    }

    return remember {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use modern photo picker
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                // Use legacy picker
                legacyPickerLauncher.launch("image/*")
            }
        }
    }
}

/**
 * Camera Capture with Permission Handling
 */
@Composable
fun rememberCameraCapture(
    onImageCaptured: (String) -> Unit,
    onError: (String) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoFile?.let { file ->
                scope.launch {
                    // Process and compress the captured image
                    val processedPath = processAndCompressCameraImage(context, file)
                    if (processedPath != null) {
                        onImageCaptured(processedPath)
                    } else {
                        onError("Failed to process image")
                    }
                }
            }
        } else {
            onError("Failed to capture image")
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            pendingPhotoFile?.let { file ->
                try {
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraLauncher.launch(photoUri)
                } catch (e: Exception) {
                    onError("Failed to launch camera: ${e.message}")
                }
            }
        } else {
            onError("Camera permission is required to take photos")
        }
    }

    return remember {
        {
            // Create photo file
            val photoFile = File(
                context.cacheDir, // Use cache dir for temporary file
                "camera_photo_temp_${System.currentTimeMillis()}.jpg"
            )
            pendingPhotoFile = photoFile

            // Check if camera permission is already granted
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, launch camera
                try {
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraLauncher.launch(photoUri)
                } catch (e: Exception) {
                    onError("Failed to launch camera: ${e.message}")
                }
            } else {
                // Request camera permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

/**
 * Process camera image to handle orientation correctly
 * This will NOT mirror front camera images
 */
private suspend fun processAndCompressCameraImage(context: Context, originalFile: File): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Load the bitmap with proper sampling to avoid memory issues
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(originalFile.absolutePath, options)

            // Calculate inSampleSize to reduce memory usage
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, options)
                ?: return@withContext null

            // Get EXIF data to check orientation
            val exif = ExifInterface(originalFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // Apply rotation based on EXIF orientation
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                    // Don't flip - this maintains the actual image as captured
                    // If you want the mirrored version, uncomment the next line:
                    // matrix.preScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.preScale(1f, -1f)
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.preScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(-90f)
                    matrix.preScale(-1f, 1f)
                }
            }

            // Create rotated bitmap only if needed
            val rotatedBitmap = if (!matrix.isIdentity) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            // Compress and save
            val outputFile = File(
                context.filesDir,
                "profile_image_${System.currentTimeMillis()}.jpg"
            )

            val saved = compressAndSaveBitmap(rotatedBitmap, outputFile)

            // Clean up
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
            bitmap.recycle()

            // Delete the original temp file
            originalFile.delete()

            if (saved) outputFile.absolutePath else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Calculate optimal inSampleSize
 */
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

/**
 * Save and compress image from URI
 */
private suspend fun saveAndCompressImage(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Use proper options to avoid OutOfMemoryError
            val options = BitmapFactory.Options()

            // First decode with inJustDecodeBounds=true to check dimensions
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            // Decode the bitmap with calculated sample size
            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return@withContext null

            val outputFile = File(
                context.filesDir,
                "profile_image_${System.currentTimeMillis()}.jpg"
            )

            val saved = compressAndSaveBitmap(bitmap, outputFile)

            bitmap.recycle()

            if (saved) outputFile.absolutePath else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Compress and save bitmap to file
 * Ensures file size is less than 1MB
 */
private fun compressAndSaveBitmap(bitmap: Bitmap, outputFile: File): Boolean {
    return try {
        // Calculate the scale factor to reduce image size if needed
        val maxDimension = 800 // Reduced for smaller file size
        val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else {
            1f
        }

        // Scale down the bitmap if needed
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Start with 85% quality
        var quality = 85
        val maxFileSize = 1024 * 1024 // 1MB in bytes

        // Compress with decreasing quality until file size is less than 1MB
        do {
            FileOutputStream(outputFile).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val fileSize = outputFile.length()

            if (fileSize > maxFileSize && quality > 10) {
                quality -= 10
            } else {
                break
            }
        } while (quality > 10)

        // Clean up
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        // Final check
        outputFile.exists() && outputFile.length() > 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Combined Image Picker with Options (Gallery or Camera)
 */
@Composable
fun rememberImagePickerWithOptions(
    onImageSelected: (String) -> Unit,
    onError: (String) -> Unit = {}
): ImagePickerLauncher {
    val galleryPicker = rememberImagePicker(onImageSelected, onError)
    val cameraPicker = rememberCameraCapture(onImageSelected, onError)

    return remember {
        ImagePickerLauncher(
            openGallery = galleryPicker,
            openCamera = cameraPicker
        )
    }
}

data class ImagePickerLauncher(
    val openGallery: () -> Unit,
    val openCamera: () -> Unit
)