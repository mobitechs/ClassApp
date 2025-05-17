package com.mobitechs.classapp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.BatchRepository
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.FreeContentRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import com.mobitechs.classapp.data.repository.OfflineDownloadRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import com.mobitechs.classapp.data.repository.PolicyTermConditionRepository
import com.mobitechs.classapp.data.repository.SearchRepository
import com.mobitechs.classapp.data.repository.UserRepository
import com.mobitechs.classapp.screens.auth.AuthViewModel
import com.mobitechs.classapp.screens.batches.BatchViewModel
import com.mobitechs.classapp.screens.freeContent.FreeContentViewModel
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.notification.NotificationViewModel
import com.mobitechs.classapp.screens.offlineDownload.OfflineDownloadViewModel
import com.mobitechs.classapp.screens.payment.PaymentHistoryViewModel
import com.mobitechs.classapp.screens.policyTermCondition.PolicyTermConditionViewModel
import com.mobitechs.classapp.screens.profile.ProfileViewModel
import com.mobitechs.classapp.screens.search.SearchViewModel
import com.mobitechs.classapp.screens.splash.SplashViewModel
import com.mobitechs.classapp.screens.store.CourseDetailViewModel
import com.mobitechs.classapp.screens.store.StoreViewModel

/**
 * Factory class for creating ViewModels without dependency injection
 */
class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val batchRepository: BatchRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationRepository: NotificationRepository,
    private val paymentRepository: PaymentRepository,
    private val freeContentRepository: FreeContentRepository,
    private val offlineDownloadRepository: OfflineDownloadRepository,
    private val searchRepository: SearchRepository,
    private val policyTermConditionRepository: PolicyTermConditionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(authRepository) as T
            }

            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(courseRepository, categoryRepository, notificationRepository,authRepository) as T
            }

            modelClass.isAssignableFrom(BatchViewModel::class.java) -> {
                BatchViewModel(batchRepository) as T
            }

            modelClass.isAssignableFrom(StoreViewModel::class.java) -> {
                StoreViewModel(courseRepository, categoryRepository) as T
            }

            modelClass.isAssignableFrom(CourseDetailViewModel::class.java) -> {
                CourseDetailViewModel(courseRepository, paymentRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository, authRepository, courseRepository) as T
            }


            modelClass.isAssignableFrom(FreeContentViewModel::class.java) -> {
                FreeContentViewModel(freeContentRepository) as T
            }

            modelClass.isAssignableFrom(PaymentHistoryViewModel::class.java) -> {
                PaymentHistoryViewModel(paymentRepository) as T
            }

            modelClass.isAssignableFrom(OfflineDownloadViewModel::class.java) -> {
                OfflineDownloadViewModel(offlineDownloadRepository) as T
            }

            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepository) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(searchRepository) as T
            }

            modelClass.isAssignableFrom(PolicyTermConditionViewModel::class.java) -> {
                PolicyTermConditionViewModel(policyTermConditionRepository) as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}