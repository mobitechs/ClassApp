package com.mobitechs.classapp.screens.store

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mobitechs.classapp.data.model.response.Content
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.screens.home.ErrorView
import com.mobitechs.classapp.screens.payment.PaymentActivity
import com.mobitechs.classapp.utils.Constants
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.openPDFReader
import com.mobitechs.classapp.utils.openVideoPlayer
import com.razorpay.Checkout
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseJson: String?,
    navController: NavController,
    viewModel: CourseDetailViewModel
) {

    ToastObserver(viewModel)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }

    // Use rememberSaveable to persist tab selection across navigation
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Details", "Content")

    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(context, gson) }
    val user = sharedPrefsManager.getUser()

    val courseObject = remember {
        courseJson?.let { Gson().fromJson(it, Course::class.java) }
    }

    LaunchedEffect(courseObject) {
        courseObject?.let {
            viewModel.setCourse(it)
            viewModel.loadCourseContent(it.id)
        }
    }

    // Handle payment initialization
    LaunchedEffect(uiState.paymentData) {
        uiState.paymentData?.let { paymentData ->
            try {
                val checkout = Checkout()
                checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

                val options = JSONObject()
                options.put("name", "Class Connect")
                options.put("description", "Payment for ${uiState.course?.course_name}")
                options.put("order_id", paymentData.orderId)
                options.put("currency", paymentData.currency)
                options.put("amount", (paymentData.amount.toInt() * 100).toInt())

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
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }

                    IconButton(onClick = {
                        viewModel.handleFavoriteClick(uiState.course?.id ?: 1,
                            uiState.course?.is_favourited ?: true
                        )
                    }) {
                        Icon(
                            imageVector = if (uiState.course?.is_favourited == true) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.course?.is_favourited == true) Color.Red
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = {
                        viewModel.handleWishlistClick(uiState.course?.id ?: 1,
                            uiState.course?.is_in_wishlist ?: true
                        )
                    }) {
                        Icon(
                            imageVector = if (uiState.course?.is_in_wishlist == true) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = "Wishlist",
                            tint = if (uiState.course?.is_in_wishlist == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            } else if (uiState.error.isNotEmpty()) {
                ErrorView(
                    message = uiState.error,
                    onRetry = { /* Reload course details */ }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            )
                        }
                    }

                    // Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (selectedTab) {
                            0 -> CourseDetailsTab(navController = navController,course = uiState.course)
                            1 -> CourseContentTab(
                                content = uiState.courseContent,
                                is_purchased = uiState.course?.is_purchased ?: false,
                                isLoading = uiState.isContentLoading,
                                onContentClick = { content ->
                                    handleContentClick(
                                        course = uiState.course!!,
                                        content = content,
                                        is_purchased = uiState.course?.is_purchased ?: false,
                                        navController = navController,
                                        context = context
                                    )
                                }
                            )
                        }
                    }
                }

                // Floating Buy Now button
                Button(
                    onClick = {
                        courseObject?.let { course ->
                            user?.let { userDetails ->
                                val intent = Intent(context, PaymentActivity::class.java).apply {
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
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
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
                        text = if (uiState.course?.is_purchased == true) "Go to Course" else "Buy Now",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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
                    videoUrl = uiState.course!!.image!!,
                    onDismiss = { showPreview = false }
                )
            }
        }
    }
}

@Composable
fun CourseDetailsTab(navController:NavController, course: Course?) {
    course?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                                /* Play video */
                                val videoUrl = Constants.video1 // or your actual video URL
                                openVideoPlayer(navController,course,videoUrl)
//                                navController.navigate("video_player?courseJson=${Gson().toJson(course)}/videoUrl=$videoUrl")
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Tags if available
                if (!course.course_tags.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SingleLineFlowRow(
                        horizontalGap = 8.dp,
                        verticalGap = 8.dp
                    ) {
                        course.course_tags.split(",").forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = tag.trim(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
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
                Text(
                    text = "By ${course.instructor}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

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

                // Price section
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Show discounted price if available
                    if (course.course_discounted_price != null) {
                        Text(
                            text = "₹${course.course_price}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    }
}

@Composable
fun CourseContentTab(
    content: List<Content>,
    is_purchased: Boolean,
    isLoading: Boolean,
    onContentClick: (Content) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (content.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "No content available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        val groupedContent = content.groupBy { it.content_type }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedContent.forEach { (type, contentList) ->
                item {
                    ContentTypeSection(
                        type = type,
                        contentList = contentList,
                        is_purchased = is_purchased,
                        onContentClick = onContentClick
                    )
                }
            }
        }
    }
}

@Composable
fun ContentTypeSection(
    type: String,
    contentList: List<Content>,
    is_purchased: Boolean,
    onContentClick: (Content) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = getContentTypeIcon(type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = getContentTypeTitle(type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${contentList.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            contentList.forEachIndexed { index, content ->
                ContentItem(
                    content = content,
                    is_purchased = is_purchased,
                    index = index + 1,
                    onContentClick = { onContentClick(content) }
                )

                if (index < contentList.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ContentItem(
    content: Content,
    is_purchased: Boolean,
    index: Int,
    onContentClick: () -> Unit
) {
    val canAccess = content.is_free == "Yes" || is_purchased

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (canAccess) 1f else 0.6f)
            .clickable(enabled = canAccess) { onContentClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index circle
        Surface(
            color = if (canAccess)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lesson $index",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (content.is_free == "Yes") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FREE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Text(
                text = "Duration: 10",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Action icons
        if (canAccess) {
            if (content.is_offline_available == "Yes") {
                IconButton(onClick = { /* Download */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Helper functions
fun getContentTypeIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "VIDEO" -> Icons.Outlined.VideoLibrary
        "AUDIO" -> Icons.Outlined.Audiotrack
        "PDF" -> Icons.Outlined.PictureAsPdf
        "DOCUMENT" -> Icons.Outlined.Description
        else -> Icons.Outlined.Article
    }
}

fun getContentTypeTitle(type: String): String {
    return when (type.uppercase()) {
        "VIDEO" -> "Video Lectures"
        "AUDIO" -> "Audio Lessons"
        "PDF" -> "PDF Materials"
        "DOCUMENT" -> "Documents"
        else -> "Study Materials"
    }
}



fun handleContentClick(
    course: Course,
    content: Content,
    is_purchased: Boolean,
    navController: NavController,
    context: Context
) {
    if (content.is_free != "Yes" && !is_purchased) {
        return
    }

    when (content.content_type.uppercase()) {
        "VIDEO" -> {
            openVideoPlayer(navController,course,content.content_url)
        }

        "AUDIO" -> {
            openVideoPlayer(navController,course,content.content_url)
        }

        "PDF" -> {
            openPDFReader(navController,course,content.content_url)
        }

        else -> {
            // Open in browser or default viewer
        }
    }
}

@Composable
fun SingleLineFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
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
                currentWidth = placeable.width
                totalHeight += currentHeight + verticalGap.roundToPx()
                currentHeight = placeable.height
                Offset(0f, totalHeight.toFloat())
            } else {
                val position = Offset(currentWidth.toFloat(), totalHeight.toFloat())
                currentWidth += placeable.width + horizontalGap.roundToPx()
                currentHeight = maxOf(currentHeight, placeable.height)
                position
            }
        }

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
    videoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
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
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Video Player",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}