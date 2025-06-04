package com.mobitechs.classapp.screens.store


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.screens.home.ErrorView
import com.mobitechs.classapp.screens.payment.PaymentActivity
import com.mobitechs.classapp.utils.Constants
import com.mobitechs.classapp.utils.showToast
import com.razorpay.Checkout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseJson: String?, navController: NavController, viewModel: CourseDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }

    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(context, gson) }
    val user = sharedPrefsManager.getUser()

    val courseObject = remember { Gson().fromJson(courseJson, Course::class.java) }



    LaunchedEffect(courseJson) {
        if (uiState.course == null && !courseJson.isNullOrEmpty()) {
            viewModel.setCourse(courseObject)
        }
    }

    // Handle payment initialization
    LaunchedEffect(uiState.paymentData) {
        uiState.paymentData?.let { paymentData ->
            try {
                // Initialize Razorpay checkout
                val checkout = Checkout()
                checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

                val options = JSONObject()
                options.put("name", "Class Connect")
                options.put("description", "Payment for ${uiState.course?.course_name}")
                options.put("order_id", paymentData.orderId)
                options.put("currency", paymentData.currency)
                options.put(
                    "amount", (paymentData.amount.toInt() * 100).toInt()
                ) // Amount in smallest currency unit

                val prefill = JSONObject()
                prefill.put("email", "")
                prefill.put("contact", "")
                options.put("prefill", prefill)

                checkout.open(context as Activity, options)
            } catch (e: Exception) {
                // Handle payment initialization error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Course Details") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                    )
                }
            }, actions = {
                // Share button
                IconButton(onClick = { /* Share functionality */ }) {
                    Icon(
                        imageVector = Icons.Default.Share, contentDescription = "Share"
                    )
                }

                // Favorite button
                IconButton(onClick = {
//                        viewModel.toggleFavorite()
                }) {
                    Icon(
                        imageVector = if (uiState.course?.isFavorite == true) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.course?.isFavorite == true) Color.Red
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Wishlist button
                IconButton(onClick = {
//                        viewModel.toggleWishlist()
                }) {
                    Icon(
                        imageVector = if (uiState.course?.isWishlisted == true) Icons.Default.Bookmark
                        else Icons.Default.BookmarkBorder,
                        contentDescription = "Wishlist",
                        tint = if (uiState.course?.isWishlisted == true) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading state
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            // Error state
            else if (uiState.error.isNotEmpty()) {
                ErrorView(
                    message = uiState.error, onRetry = { /* Reload course details */ })
            }
            // Course details content
            else {
                uiState.course?.let { course ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp) // Add padding for the floating button
                    ) {
                        // Course thumbnail with preview button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            AsyncImage(
                                model = course.image,
                                contentDescription = course.course_name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Preview button if preview URL exists
                            if (course.image != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            //play video
                                            val videoUrl =
                                                Constants.video1 // or your actual video URL
                                            navController.navigate(
                                                "video_player?courseJson=${
                                                    Gson().toJson(
                                                        course
                                                    )
                                                }/videoUrl=$videoUrl"
                                            )
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Preview",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Course information
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Move tags to appear above course name
                            // Categories and tags
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Category chip
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = course.course_name,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    )
                                }

                                // Subcategory chip if available
                                if (course.sub_category_name != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = course.sub_category_name,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp, vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                            }

                            // Tags if available
                            if (!course.course_tags.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                SingleLineFlowRow(
                                    horizontalGap = 8.dp, verticalGap = 8.dp
                                ) {
                                    course.course_tags.forEach { tag ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
//                                            Text(
//                                                text = tag,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                                style = MaterialTheme.typography.bodySmall,
//                                                modifier = Modifier.padding(
//                                                    horizontal = 12.dp,
//                                                    vertical = 6.dp
//                                                )
//                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Title
                            Text(
                                text = course.course_name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Instructor
//                            Text(
//                                text = "By ${course.instructor}",
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Rating row
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Star rating
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = course.course_like.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Students count
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "10 students",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Price section directly below rating without card
                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Show discounted price if available
                                if (course.course_discounted_price != null) {
                                    Text(
                                        text = "₹${course.course_price}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.6f
                                        ),
                                        textDecoration = TextDecoration.LineThrough
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "₹${course.course_discounted_price}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Discount percentage
                                    val discountPercentage =
                                        ((course.course_price.toDouble() - course.course_discounted_price.toDouble()) / course.course_price.toDouble() * 100).toInt()

                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "$discountPercentage% OFF",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp, vertical = 2.dp
                                            )
                                        )
                                    }
                                } else {
                                    // Regular price if no discount
                                    Text(
                                        text = "₹${course.course_price}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description section
                            Text(
                                text = "About This Course",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = course.course_description.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )


                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Floating Buy Now button positioned at the bottom
                    Button(
                        onClick = {
//                                  Launch the PaymentActivity
                            courseObject?.let { course ->
                                user?.let { userDetails ->
                                    val intent =
                                        Intent(context, PaymentActivity::class.java).apply {
                                            putExtra("COURSE_DATA", gson.toJson(course))
                                            putExtra("USER_DATA", gson.toJson(userDetails))
                                        }
                                    context.startActivity(intent)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp, pressedElevation = 8.dp
                        ),
                        enabled = !uiState.isProcessingPayment
                    ) {
                        if (uiState.isProcessingPayment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (course.isPurchased == true) "Go to Course" else "Buy Now",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Error snackbar
            AnimatedVisibility(
                visible = uiState.error.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(uiState.error)
                }
            }

            // Video preview dialog
            if (showPreview && uiState.course?.image != null) {
                VideoPreviewDialog(
                    videoUrl = uiState.course!!.image!!, onDismiss = { showPreview = false })
            }
        }
    }
}


fun createOrderId(): String {
    val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    val currentDate = sdf.format(Date())
    return "ORDER_$currentDate"
}

fun openPaymentGateway(context: Context, userDetails: Student, courseDetails: Course) {
    val currency = "INR"
    val order_id = createOrderId()
    try {
//        Initialize Razorpay checkout
        val checkout = Checkout()
        checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

        val options = JSONObject()
        options.put("name", "Class Connect")
        options.put("description", "Payment for ${courseDetails.course_name}")
        options.put("order_id", order_id)
        options.put("currency", currency)
        options.put(
            "amount", (courseDetails.course_discounted_price.toDouble() * 100).toInt()
        ) // Amount in smallest currency unit

        val prefill = JSONObject()
        prefill.put("email", userDetails!!.email)
        prefill.put("contact", userDetails!!.phone)
        options.put("prefill", prefill)

        checkout.open(context as Activity, options)
    } catch (e: Exception) {
        // Handle payment initialization error
        showToast(context, e.message.toString())
    }
}

// Helper component for wrapping tags in multiple lines
@Composable
fun SingleLineFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content, modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val maxWidth = constraints.maxWidth
        var currentWidth = 0
        var currentHeight = 0
        var totalHeight = 0

        val positions = placeables.map { placeable ->
            if (currentWidth + placeable.width > maxWidth) {
                // Move to next row
                currentWidth = placeable.width
                totalHeight += currentHeight + verticalGap.roundToPx()
                currentHeight = placeable.height

                Offset(0f, totalHeight.toFloat())
            } else {
                // Stay on current row
                val position = Offset(currentWidth.toFloat(), totalHeight.toFloat())
                currentWidth += placeable.width + horizontalGap.roundToPx()
                currentHeight = maxOf(currentHeight, placeable.height)

                position
            }
        }

        // Calculate total height including the last row
        totalHeight += currentHeight

        layout(maxWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = positions[index]
                placeable.placeRelative(x.toInt(), y.toInt())
            }
        }
    }
}

@Composable
fun VideoPreviewDialog(
    videoUrl: String, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Course Preview",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video player would go here
                // For demonstration, showing a placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Video Player", color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss, modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}