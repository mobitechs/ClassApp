package com.mobitechs.classapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobitechs.classapp.Screen.ChatListScreen
import com.mobitechs.classapp.data.local.AppDatabase
import com.mobitechs.classapp.data.repository.UserRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
import com.mobitechs.classapp.data.repository.chat.MessageRepository
import com.mobitechs.classapp.screens.auth.AuthViewModel
import com.mobitechs.classapp.screens.auth.LoginScreen
import com.mobitechs.classapp.screens.auth.RegisterScreen
import com.mobitechs.classapp.screens.batches.BatchViewModel
import com.mobitechs.classapp.screens.batches.BatchesScreen
import com.mobitechs.classapp.screens.categoryDetails.CategoryWiseDetailsScreen
import com.mobitechs.classapp.screens.categoryDetails.SeeAllCategoriesScreen
import com.mobitechs.classapp.screens.categoryDetails.SubCategoryViewModel
import com.mobitechs.classapp.screens.chat.ChatListScreen
import com.mobitechs.classapp.screens.chat.ChatScreen
import com.mobitechs.classapp.screens.chat.NewChatScreen
import com.mobitechs.classapp.screens.freeContent.FreeContentScreen
import com.mobitechs.classapp.screens.freeContent.FreeContentViewModel
import com.mobitechs.classapp.screens.home.AppBottomNavigation
import com.mobitechs.classapp.screens.home.HomeScreen
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.notification.NotificationScreen
import com.mobitechs.classapp.screens.notification.NotificationViewModel
import com.mobitechs.classapp.screens.offlineDownload.MyDownloadsScreen
import com.mobitechs.classapp.screens.offlineDownload.MyDownloadsViewModel
import com.mobitechs.classapp.screens.payment.PaymentHistoryScreen
import com.mobitechs.classapp.screens.payment.PaymentHistoryViewModel
import com.mobitechs.classapp.screens.policyTermCondition.FeedbackScreen
import com.mobitechs.classapp.screens.policyTermCondition.PolicyTermConditionViewModel
import com.mobitechs.classapp.screens.policyTermCondition.PrivacyPolicyScreen
import com.mobitechs.classapp.screens.policyTermCondition.TermConditionScreen
import com.mobitechs.classapp.screens.profile.FavouriteViewModel
import com.mobitechs.classapp.screens.profile.MyFavouriteScreen
import com.mobitechs.classapp.screens.profile.MyWishListViewModel
import com.mobitechs.classapp.screens.profile.MyWishlistScreen
import com.mobitechs.classapp.screens.profile.ProfileScreen
import com.mobitechs.classapp.screens.profile.ProfileViewModel
import com.mobitechs.classapp.screens.search.SearchScreen
import com.mobitechs.classapp.screens.search.SearchViewModel
import com.mobitechs.classapp.screens.settings.ThemeSelectionScreen
import com.mobitechs.classapp.screens.settings.ThemeSelectionViewModel
import com.mobitechs.classapp.screens.splash.SplashScreen
import com.mobitechs.classapp.screens.splash.SplashViewModel
import com.mobitechs.classapp.screens.store.CourseDetailScreen
import com.mobitechs.classapp.screens.store.CourseDetailViewModel
import com.mobitechs.classapp.screens.store.PdfViewerScreen
import com.mobitechs.classapp.screens.store.SeeAllCoursesScreen
import com.mobitechs.classapp.screens.store.StoreScreen
import com.mobitechs.classapp.screens.store.StoreViewModel
import com.mobitechs.classapp.screens.videoPlayer.VideoPlayerScreen
import com.mobitechs.classapp.screens.videoPlayer.VideoPlayerViewModel
import com.mobitechs.classapp.ui.theme.ClassConnectTheme
import com.mobitechs.classapp.ui.theme.ClassConnectThemeWrapper
import com.mobitechs.classapp.viewModel.chat.ChatListViewModel
import com.mobitechs.classapp.viewModel.chat.ChatViewModel
import com.mobitechs.classapp.viewModel.chat.NewChatViewModel
import com.mobitechs.classapp.ui.theme.ThemeManager
import com.mobitechs.classapp.ui.theme.ThemeState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get repositories from application class
        val app = application as ClassConnectApp

        // Initialize theme manager early
        val themeManager = ThemeManager(applicationContext)

        // Load saved theme on app start
        lifecycleScope.launch {
            themeManager.selectedTheme.collect { theme ->
                ThemeState.currentTheme.value = theme
            }
        }

        lifecycleScope.launch {
            themeManager.isDarkMode.collect { isDark ->
                ThemeState.isDarkMode.value = isDark
            }
        }



        // Create ViewModel factory
        val viewModelFactory = ViewModelFactory(
            applicationContext,
            app.authRepository,
            app.userRepository,
            app.courseRepository,
            app.batchRepository,
            app.categoryRepository,
            app.notificationRepository,
            app.paymentRepository,
            app.freeContentRepository,
            app.myDownloadsRepository,
            app.searchRepository,
            app.policyTermConditionRepository,
            app.chatUserRepository,
            app.chatRepository,
            app.messageRepository,
            app.themeRepository
        )

        setContent {
            ClassConnectThemeWrapper  {
                AppNavigation(viewModelFactory)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom navigation visible only on main screens
    val showBottomBar = currentRoute in listOf(
        Screen.HomeScreen.route,
        Screen.BatchesScreen.route,
        Screen.StoreScreen.route,
        Screen.ProfileScreen.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    )  { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.SplashScreen.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.SplashScreen.route) {
                val splashViewModel: SplashViewModel = viewModel(factory = viewModelFactory)
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth screens
            composable(Screen.LoginScreen.route) {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.RegisterScreen.route) },
                    onNavigateToHome = { navController.navigate(Screen.HomeScreen.route) }
                )
            }

            composable(Screen.RegisterScreen.route) {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) },
                    onNavigateToHome = { navController.navigate(Screen.HomeScreen.route) }
                )
            }


            // Main screens
            composable(Screen.HomeScreen.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    viewModel = homeViewModel,
                    navController = navController
                )
            }

            composable(Screen.BatchesScreen.route) {
                val batchViewModel: BatchViewModel = viewModel(factory = viewModelFactory)
                BatchesScreen(
                    viewModel = batchViewModel,
                    navController = navController
                )
            }

            composable(Screen.StoreScreen.route) {
                val storeViewModel: StoreViewModel = viewModel(factory = viewModelFactory)
                StoreScreen(
                    viewModel = storeViewModel,
                    navController = navController
                )
            }

            composable(Screen.ProfileScreen.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    navController = navController
                )
            }


//
//
//            composable(
//                "course_detail?courseJson={courseJson}",
//                arguments = listOf(
//                    navArgument("courseJson") {
//                        type = NavType.StringType
//                        defaultValue = ""
//                    }
//                )
//            ) {
//                val courseDetailViewModel: CourseDetailViewModel = viewModel(factory = viewModelFactory)
//                val courseJson = it.arguments?.getString("courseJson")
//                CourseDetailScreen(
//                    courseJson = courseJson,
//                    navController = navController,
//                    viewModel = courseDetailViewModel
//                )
//            }

            composable(
                "course_detail?courseJson={courseJson}",
                arguments = listOf(
                    navArgument("courseJson") {
                        type = NavType.StringType
                        defaultValue = "null"
                    }
                )
            ) { backStackEntry ->
                val courseJson = backStackEntry.arguments?.getString("courseJson")
                val viewModel: CourseDetailViewModel = viewModel(factory = viewModelFactory)
                CourseDetailScreen(courseJson, navController, viewModel)
            }

            composable(
                "video_player?courseJson={courseJson}/videoUrl={videoUrl}",
                arguments = listOf(
                    navArgument("courseJson") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    },
                    navArgument("videoUrl") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    }
                )
            ) { backStackEntry ->
                val courseJson = backStackEntry.arguments?.getString("courseJson")
                val videoUrl = backStackEntry.arguments?.getString("videoUrl")
                val viewModel: VideoPlayerViewModel = viewModel(factory = viewModelFactory)
                VideoPlayerScreen(courseJson, videoUrl, navController, viewModel)
            }



            composable(
                "PDFReader?courseJson={courseJson}/url={url}",
                arguments = listOf(
                    navArgument("courseJson") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    },
                    navArgument("url") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    }
                )
            ) { backStackEntry ->
                val courseJson = backStackEntry.arguments?.getString("courseJson")
                val url = backStackEntry.arguments?.getString("url")
                val viewModel: VideoPlayerViewModel = viewModel(factory = viewModelFactory)
                PdfViewerScreen(courseJson, url, navController, viewModel)
            }


            composable(Screen.FreeContentScreen.route) {
                val viewModel: FreeContentViewModel = viewModel(factory = viewModelFactory)
                FreeContentScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(Screen.ThemeSelectionScreen.route) {
                val themeViewModel: ThemeSelectionViewModel = viewModel(factory = viewModelFactory)
                ThemeSelectionScreen(
                    viewModel = themeViewModel,
                    navController = navController
                )
            }

            composable(Screen.PaymentHistoryScreen.route) {
                val viewModel: PaymentHistoryViewModel = viewModel(factory = viewModelFactory)
                PaymentHistoryScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.MyDownloadsScreen.route) {
                val viewModel: MyDownloadsViewModel = viewModel(factory = viewModelFactory)
                MyDownloadsScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.NotificationScreen.route) {
                val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
                NotificationScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.SearchScreen.route) {
                val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                SearchScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.PrivacyPolicyScreen.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                PrivacyPolicyScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.TermConditionScreen.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                TermConditionScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.FeedbackScreen.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                FeedbackScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }


            composable(
                "categoryWiseDetailsScreen?categoryId={categoryId}/categoryName={categoryName}",
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    },
                    navArgument("categoryName") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId")
                val categoryName = backStackEntry.arguments?.getString("categoryName")
                val viewModel: SubCategoryViewModel = viewModel(factory = viewModelFactory)
                CategoryWiseDetailsScreen(
                    viewModel = viewModel,
                    navController = navController,
                    categoryId = categoryId?.toInt() ?: 1,
                    categoryName = categoryName.toString()
                )
            }




            composable(
                "seeAllCoursesScreen?courseListJson={courseListJson}/courseType={courseType}",
                arguments = listOf(
                    navArgument("courseListJson") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    },
                    navArgument("courseType") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    }
                )
            ) { backStackEntry ->
                val coursesJson = backStackEntry.arguments?.getString("courseListJson") ?: "[]"
                val courseType = backStackEntry.arguments?.getString("courseType") ?: "popular"
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)

                SeeAllCoursesScreen(
                    navController = navController,
                    coursesJson = coursesJson,
                    courseType = courseType,
                    homeViewModel = homeViewModel
                )
            }
            composable(
                "SeeAllCategoriesScreen?categoryListJson={categoryListJson}",
                arguments = listOf(
                    navArgument("categoryListJson") {
                        type = NavType.StringType
                        defaultValue = "null" // Handle null case
                    }
                )
            ) { backStackEntry ->
                val categoryListJson =
                    backStackEntry.arguments?.getString("categoryListJson") ?: "[]"

                SeeAllCategoriesScreen(
                    navController = navController,
                    categoryJson = categoryListJson
                )
            }

            composable(Screen.MyFavouriteScreen.route) {
                val viewModel: FavouriteViewModel = viewModel(factory = viewModelFactory)
                MyFavouriteScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(Screen.MyWishlistScreen.route) {
                val viewModel: MyWishListViewModel = viewModel(factory = viewModelFactory)
                MyWishlistScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }


//            chat screen
            composable(Screen.ChatListScreen.route) {
                val viewModel: ChatListViewModel = viewModel(factory = viewModelFactory)
                ChatListScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(
                "chat/{chatId}",
                arguments = listOf(
                    navArgument("chatId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val viewModel: ChatViewModel = viewModel(factory = viewModelFactory)

                ChatScreen(
                    viewModel = viewModel,
                    navController = navController,
                    chatId = chatId
                )
            }

            composable(Screen.NewChatScreen.route) {
                val viewModel: NewChatViewModel = viewModel(factory = viewModelFactory)
                NewChatScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }


        }
    }
}


// Define navigation destinations
sealed class Screen(val route: String) {
    object SplashScreen : Screen("splashScreen")
    object LoginScreen : Screen("loginScreen")
    object RegisterScreen : Screen("registerScreen")
    object HomeScreen : Screen("homeScreen")
    object BatchesScreen : Screen("batchesScreen")

    object StoreScreen : Screen("storeScreen")
    object CategoryScreen : Screen("categoryScreen")
    object SeeAllCoursesScreen : Screen("seeAllCoursesScreen")
    object SeeAllCategoriesScreen : Screen("seeAllCategoriesScreen")
    object SearchScreen : Screen("searchScreen")
    object FreeContentScreen : Screen("freeContentScreen")
    object ThemeSelectionScreen : Screen("themeSelectionScreen")

    object ProfileScreen : Screen("profileScreen")
    object MyDownloadsScreen : Screen("myDownloadScreen")
    object MyFavouriteScreen : Screen("MyFavouriteScreen")
    object MyWishlistScreen : Screen("myWishlistScreen")

    object PaymentHistoryScreen : Screen("paymentHistoryScreen")

    object NotificationScreen : Screen("notificationScreen")
    object PrivacyPolicyScreen : Screen("privacyPolicyScreen")
    object TermConditionScreen : Screen("termConditionScreen")
    object FeedbackScreen : Screen("feedbackScreen")

    object VideoPlayerScreen : Screen("videoPlayerScreen")
    object ChatListScreen : Screen("chatListScreen")
    object NewChatScreen : Screen("newChatScreen")

}