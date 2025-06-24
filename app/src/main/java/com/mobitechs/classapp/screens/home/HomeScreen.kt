package com.mobitechs.classapp.screens.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mobitechs.classapp.R
import com.mobitechs.classapp.Screen
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.screens.common.BannerCarousel
import com.mobitechs.classapp.screens.common.CategoryCardWithBgColorNIcon
import com.mobitechs.classapp.screens.common.CourseCardPopularFeatured
import com.mobitechs.classapp.screens.common.Grid
import com.mobitechs.classapp.screens.common.ModernNoticeCard
import com.mobitechs.classapp.screens.common.PrimaryButton
import com.mobitechs.classapp.screens.common.SectionTitle
import com.mobitechs.classapp.screens.common.SideMenu
import com.mobitechs.classapp.ui.theme.AppTheme
import com.mobitechs.classapp.utils.Constants
import com.mobitechs.classapp.utils.ToastObserver
import com.mobitechs.classapp.utils.getFirstName
import com.mobitechs.classapp.utils.openCategoryWiseDetailsScreen
import com.mobitechs.classapp.utils.openChannelLinks
import com.mobitechs.classapp.utils.openCourseDetailsScreen
import com.mobitechs.classapp.utils.openSeeAllCategory
import com.mobitechs.classapp.utils.openSeeAllCourse
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
    ToastObserver(viewModel)

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
                TopAppBar(
                    title = {
                        Text(
                            text = "Hello ${getFirstName(user?.name ?: "")}",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = AppTheme.topAppBarColors,
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
                            onClick = { navController.navigate(Screen.SearchScreen.route) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate(Screen.NotificationScreen.route) }
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
                        context = context,
                        viewModel = viewModel,
                        navController = navController,
                        uiState = uiState,
                        onSeeAllClick = { section ->
                            when (section) {
                                "featured" -> {
                                    openSeeAllCourse(
                                        navController,
                                        uiState.featuredCourses,
                                        "featured"
                                    )
                                }

                                "popular" -> {
                                    openSeeAllCourse(
                                        navController,
                                        uiState.popularCourses,
                                        "popular"
                                    )
                                }

                                "offers" -> navController.navigate("courses?type=offers")
                                "categories" -> {
                                    openSeeAllCategory(navController, uiState.categories)
                                }
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
    context: Context,
    viewModel: HomeViewModel,
    navController: NavController,
    uiState: HomeUiState,
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
                        CourseCardPopularFeatured(
                            course = course,
                            onClick = {
                                openCourseDetailsScreen(navController, course)
                            },
                            onFavoriteClick = {
                                viewModel.handleFavoriteClick(course.id, course.is_favourited)
                            },
                            onWishlistClick = {
                                viewModel.handleWishlistClick(course.id, course.is_in_wishlist)
                            }
                        )
                    }
                }
            }
        }


        // Categories grid
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Categories",
            onSeeAllClick = { onSeeAllClick("categories") }
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

                Grid(
                    items = uiState.categories.take(4), // Show only first 4 categories,
                    columns = 2,
                    horizontalSpacing = 1.dp,
                    verticalSpacing = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) { category ->
                    CategoryCardWithBgColorNIcon(
                        category = category,
                        onClick = {
                            openCategoryWiseDetailsScreen(
                                navController,
                                category.id.toString(), category.name
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                }
            }
        }

        // section - Notice Board
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(
            title = "Notice Board",
//            onSeeAllClick = { onSeeAllClick("offers") }
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
                        ModernNoticeCard(
                            notice = board,
                            onClick = {
                                Log.d("Notice Board", "Details: ${board.notice_title}")
                            }
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
                        CourseCardPopularFeatured(
                            course = course,
                            onClick = { openCourseDetailsScreen(navController, course) },
                            onFavoriteClick = {
                                viewModel.handleFavoriteClick(course.id, course.is_favourited)
                            },
                            onWishlistClick = {
                                viewModel.handleWishlistClick(course.id, course.is_in_wishlist)
                            }
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


            AddSocialMediaICons(context, "YouTube", R.drawable.youtube, Constants.CONNECT_YOUTUBE)
            AddSocialMediaICons(
                context,
                "Telegram",
                R.drawable.telegram,
                Constants.CONNECT_TELEGRAM
            )
            AddSocialMediaICons(
                context,
                "Whatsapp",
                R.drawable.whatsapp,
                Constants.CONNECT_WHATSAPP
            )
            AddSocialMediaICons(
                context,
                "Facebook",
                R.drawable.facebook,
                Constants.CONNECT_FACEBOOK
            )

//            Image(
//                painter = painterResource(id = R.drawable.telegram),
//                contentDescription = "",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clickable {
//                        openChannelLinks(context, Constants.CONNECT_TELEGRAM)
//                    }
//            )
//            Image(
//                painter = painterResource(id = R.drawable.whatsapp),
//                contentDescription = "Whatsapp",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clickable {
//                        openChannelLinks(context, Constants.CONNECT_WHATSAPP)
//                    }
//            )
//            Image(
//                painter = painterResource(id = R.drawable.facebook),
//                contentDescription = "Facebook",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clickable {
//                        openChannelLinks(context, Constants.CONNECT_FACEBOOK)
//                    }
//            )


        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AddSocialMediaICons(context: Context, name: String, icon: Int, url: String) {
    Box(
        modifier = Modifier
            .size(48.dp)  // Fixed size for all

    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    openChannelLinks(context, url)
                }
        )
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