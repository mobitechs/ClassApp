package com.mobitechs.classapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobitechs.classapp.screens.auth.AuthViewModel
import com.mobitechs.classapp.screens.auth.LoginScreen
import com.mobitechs.classapp.screens.auth.RegisterScreen
import com.mobitechs.classapp.screens.batches.BatchViewModel
import com.mobitechs.classapp.screens.batches.BatchesScreen
import com.mobitechs.classapp.screens.freeContent.FreeContentScreen
import com.mobitechs.classapp.screens.freeContent.FreeContentViewModel
import com.mobitechs.classapp.screens.home.AppBottomNavigation
import com.mobitechs.classapp.screens.home.HomeScreen
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.notification.NotificationScreen
import com.mobitechs.classapp.screens.notification.NotificationViewModel
import com.mobitechs.classapp.screens.offlineDownload.OfflineDownloadScreen
import com.mobitechs.classapp.screens.offlineDownload.OfflineDownloadViewModel
import com.mobitechs.classapp.screens.payment.PaymentHistoryScreen
import com.mobitechs.classapp.screens.payment.PaymentHistoryViewModel
import com.mobitechs.classapp.screens.policyTermCondition.FeedbackScreen
import com.mobitechs.classapp.screens.policyTermCondition.PolicyTermConditionViewModel
import com.mobitechs.classapp.screens.policyTermCondition.PrivacyPolicyScreen
import com.mobitechs.classapp.screens.policyTermCondition.TermConditionScreen
import com.mobitechs.classapp.screens.profile.ProfileScreen
import com.mobitechs.classapp.screens.profile.ProfileViewModel
import com.mobitechs.classapp.screens.search.SearchScreen
import com.mobitechs.classapp.screens.search.SearchViewModel
import com.mobitechs.classapp.screens.splash.SplashScreen
import com.mobitechs.classapp.screens.splash.SplashViewModel
import com.mobitechs.classapp.screens.store.CourseDetailScreen
import com.mobitechs.classapp.screens.store.CourseDetailViewModel
import com.mobitechs.classapp.screens.store.StoreScreen
import com.mobitechs.classapp.screens.store.StoreViewModel
import com.mobitechs.classapp.screens.subCategory.CategoryScreen
import com.mobitechs.classapp.screens.subCategory.SubCategoryViewModel
import com.mobitechs.classapp.ui.theme.ClassConnectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get repositories from application class
        val app = application as ClassConnectApp

        // Create ViewModel factory
        val viewModelFactory = ViewModelFactory(
            app.authRepository,
            app.userRepository,
            app.courseRepository,
            app.batchRepository,
            app.categoryRepository,
            app.notificationRepository,
            app.paymentRepository,
            app.freeContentRepository,
            app.offlineDownloadRepository,
            app.searchRepository,
            app.policyTermConditionRepository,
        )

        setContent {
            ClassConnectTheme {
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
        Screen.Home.route,
        Screen.Batches.route,
        Screen.Store.route,
        Screen.Profile.route
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                val splashViewModel: SplashViewModel = viewModel(factory = viewModelFactory)
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth screens
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.Register.route) {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) }
                )
            }



            // Main screens
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    viewModel = homeViewModel,
                    navController = navController
                )
            }

            composable(Screen.Batches.route) {
                val batchViewModel: BatchViewModel = viewModel(factory = viewModelFactory)
                BatchesScreen(
                    viewModel = batchViewModel,
                    navController = navController
                )
            }

            composable(Screen.Store.route) {
                val storeViewModel: StoreViewModel = viewModel(factory = viewModelFactory)
                StoreScreen(
                    viewModel = storeViewModel,
                    navController = navController
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    navController = navController
                )
            }

            // Detail screens
            composable(
                route = "course_details/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) {
                val courseDetailViewModel: CourseDetailViewModel =
                    viewModel(factory = viewModelFactory)
                CourseDetailScreen(
                    viewModel = courseDetailViewModel,
                    navController = navController
                )
            }

            composable(
                route = "study_material/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.StringType })
            ) {
                // Study material screen implementation
            }

            composable(Screen.FreeContent.route) {
                val viewModel: FreeContentViewModel = viewModel(factory = viewModelFactory)
                FreeContentScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.PaymentHistory.route) {
                val viewModel: PaymentHistoryViewModel = viewModel(factory = viewModelFactory)
                PaymentHistoryScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.OfflineDownload.route) {
                val viewModel: OfflineDownloadViewModel = viewModel(factory = viewModelFactory)
                OfflineDownloadScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.Notification.route) {
                val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
                NotificationScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.Search.route) {
                val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                SearchScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                PrivacyPolicyScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.TermCondition.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                TermConditionScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.Feedback.route) {
                val viewModel: PolicyTermConditionViewModel = viewModel(factory = viewModelFactory)
                FeedbackScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(Screen.CategoryScreen.route) {
                val viewModel: SubCategoryViewModel = viewModel(factory = viewModelFactory)
                CategoryScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }


        }
    }
}


// Define navigation destinations
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Batches : Screen("batches")
    object Store : Screen("store")
    object Profile : Screen("profile")
    object FreeContent : Screen("freeContent")
    object PaymentHistory : Screen("paymentHistory")
    object OfflineDownload : Screen("offlineDownload")
    object Search : Screen("search")
    object Notification : Screen("notification")
    object PrivacyPolicy : Screen("privacyPolicy")
    object TermCondition : Screen("termCondition")
    object Feedback : Screen("feedback")
    object CategoryScreen : Screen("categoryScreen")
    // Add other screens as needed
}