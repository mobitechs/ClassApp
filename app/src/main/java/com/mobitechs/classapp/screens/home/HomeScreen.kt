package com.mobitechs.classapp.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.screens.common.BannerCarousel
import com.mobitechs.classapp.screens.common.CourseCard
import com.mobitechs.classapp.screens.common.Grid
import com.mobitechs.classapp.screens.common.HomeCategoryItem
import com.mobitechs.classapp.screens.common.PrimaryButton
import com.mobitechs.classapp.screens.common.SectionTitle
import com.mobitechs.classapp.screens.common.SideMenu
import com.mobitechs.classapp.screens.common.StoreCategoryItem
import com.mobitechs.classapp.screens.store.getCategoryIcon
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(context, gson) }
    val user = sharedPrefsManager.getUser()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SideMenu(
                    onMenuItemClick = { route ->
                        navController.navigate(route)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onClose = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    user = user
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MediumTopAppBar(
                    title = {
//                        Text(
//                            text = "Class Connect22",
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("search") }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate("notification") }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uiState.hasNotifications) {
                                        Badge {
                                            Text(text = uiState.notificationCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Only show the loading indicator during initial load
                if (uiState.isInitialLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Always show HomeContent, each section handles its own loading/error state
                    HomeContent(
                        navController = navController,
                        uiState = uiState,
                        onCourseClick = { courseId ->
                            navController.navigate("course_details/$courseId")
                        },
                        onCategoryClick = { categoryId ->
//                            navController.navigate("category/$categoryId")
                            navController.navigate("categoryScreen")
                        },
                        onFavoriteClick = { courseId ->
//                            viewModel.toggleFavorite(courseId)
                        },
                        onSeeAllClick = { section ->
                            when (section) {
                                "featured" -> navController.navigate("courses?type=featured")
                                "popular" -> navController.navigate("courses?type=popular")
                                "offers" -> navController.navigate("courses?type=offers")
                                "categories" -> navController.navigate("categoryScreen")
                            }
                        },
                        onReferralClick = {
                            navController.navigate("referral")
                        },
                        onRetrySection = { section ->
                            viewModel.retryLoadSection(section)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    navController: NavController,
    uiState: HomeUiState,
    onCourseClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onReferralClick: () -> Unit,
    onRetrySection: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        //section - Banner carousel
        SectionTitle(
            title = "Special Offers",

        )
        //
        Spacer(modifier = Modifier.height(16.dp))
        when {
            uiState.offersBannersLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            uiState.offersBannersError.isNotEmpty() -> {
                SectionErrorView(
                    message = uiState.offersBannersError,
                    onRetry = { onRetrySection("offersBanners") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.offersBanners.isNotEmpty() -> {
                BannerCarousel(
                    banners = uiState.offersBanners,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        // Popular courses section
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Popular Courses",
            onSeeAllClick = { onSeeAllClick("popular") }
        )

        when {
            uiState.popularCoursesLoading -> {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            uiState.popularCoursesError.isNotEmpty() -> {
                SectionErrorView(
                    message = uiState.popularCoursesError,
                    onRetry = { onRetrySection("popularCourses") },
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.popularCourses.isNotEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.popularCourses) { course ->
                        CourseCard(
                            title = course.course_name,
                            description = course.course_description,
                            imageUrl = course.image,
                            rating = 5f,
                            likes = course.course_like,
                            price = "₹${course.course_discounted_price ?: course.course_price}",
                            discountedPrice = if (course.course_discounted_price != null) "₹${course.course_price}" else null,
                            isFavorite = false,
                            onClick = { navController.navigate("course_detail?courseJson=${Gson().toJson(course)}")},
                            onFavoriteClick = { onFavoriteClick(course.id.toString()) }
                        )
                    }
                }
            }
        }


        // Categories grid
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Categories",
            //onSeeAllClick = { onSeeAllClick("categories") }
        )

        when {
            uiState.categoriesLoading -> {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            uiState.categoriesError.isNotEmpty() -> {
                SectionErrorView(
                    message = uiState.categoriesError,
                    onRetry = { onRetrySection("categories") },
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.categories.isNotEmpty() -> {

//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(2),
//                    contentPadding = PaddingValues(horizontal = 16.dp),
//                    modifier = Modifier
//                        .height(250.dp) // Increased from 200.dp
//                        .fillMaxWidth(),
//                    userScrollEnabled = false
//                ) {
//                    items(uiState.categories) { category ->
//                        HomeCategoryItem(
//                            icon = getCategoryIcon(category.name),
//                            name = category.name,
//                            isSelected = false,
//                            onCategorySelected = { onCategoryClick(category.id.toString()) }
//                        )
//                    }
//                }

                Grid(
                    items = uiState.categories,
                    columns = 2,
                    horizontalSpacing = 1.dp,
                    verticalSpacing = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) { category ->
                    HomeCategoryItem(
                        icon = getCategoryIcon(category.name),
                        name = category.name,
                        isSelected = false,
                        onCategorySelected = { onCategoryClick(category.id.toString()) }
                    )
                }



            }
        }

        // section - Notice Board
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Notice Board",
            onSeeAllClick = { onSeeAllClick("offers") }
        )

        when {
            uiState.noticeBoardLoading -> {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            uiState.noticeBoardError.isNotEmpty() -> {
                SectionErrorView(
                    message = uiState.noticeBoardError,
                    onRetry = { onRetrySection("noticeBoard") },
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.noticeBoard.isNotEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.noticeBoard) { board ->
                        CourseCard(
                            title = board.notice_title,
                            description = "",
                            imageUrl = board.url,
                            rating = 5f,
                            likes = board.course.course_like,
                            price = "₹${board.course.course_discounted_price ?: board.course.course_price}",
                            discountedPrice = if (board.course.course_discounted_price != null) "₹${board.course.course_price}" else null,
                            isFavorite = false,
                            onClick = { onCourseClick(board.course.id.toString()) },
                            onFavoriteClick = { onFavoriteClick(board.course.id.toString()) }
                        )
                    }
                }
            }
        }

        // Featured courses section
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Featured Courses",
            onSeeAllClick = { onSeeAllClick("featured") }
        )

        when {
            uiState.featuredCoursesLoading -> {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            uiState.featuredCoursesError.isNotEmpty() -> {
                SectionErrorView(
                    message = uiState.featuredCoursesError,
                    onRetry = { onRetrySection("featuredCourses") },
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.featuredCourses.isNotEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.featuredCourses) { course ->
                        CourseCard(
                            title = course.course_name,
                            description = course.course_description,
                            imageUrl = course.image,
                            rating = 5f,
                            likes = course.course_like,
                            price = "₹${course.course_discounted_price ?: course.course_price}",
                            discountedPrice = if (course.course_discounted_price != null) "₹${course.course_price}" else null,
                            isFavorite = false,
                            onClick = { navController.navigate("course_detail?courseJson=${Gson().toJson(course)}") },
                            onFavoriteClick = { onFavoriteClick(course.id.toString()) }
                        )
                    }
                }
            }
        }


        // Referral banner
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onReferralClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Refer and Earn",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Invite friends & get ₹500 for each successful referral",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Social media links
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Connect With Us",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SocialMediaIcon(
                icon = Icons.Default.Facebook,
                contentDescription = "Facebook",
                backgroundColor = Color(0xFF1877F2),
                onClick = { /* Open Facebook page */ }
            )

            SocialMediaIcon(
                icon = Icons.Filled.Insights,
                contentDescription = "Instagram",
                backgroundColor = Color(0xFFE1306C),
                onClick = { /* Open Instagram page */ }
            )

            SocialMediaIcon(
                icon = Icons.Filled.YoutubeSearchedFor,
                contentDescription = "YouTube",
                backgroundColor = Color(0xFFFF0000),
                onClick = { /* Open YouTube channel */ }
            )

            SocialMediaIcon(
                icon = Icons.Filled.Hotel,
                contentDescription = "Telegram",
                backgroundColor = Color(0xFF0088CC),
                onClick = { /* Open Telegram channel */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}
@Composable
fun SocialMediaIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.width(200.dp)
        )
    }
}