package com.mobitechs.classapp


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.BatchRepository
import com.mobitechs.classapp.data.repository.CategoryRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.FreeContentRepository
import com.mobitechs.classapp.data.repository.NotificationRepository
import com.mobitechs.classapp.data.repository.MyDownloadsRepository
import com.mobitechs.classapp.data.repository.PaymentRepository
import com.mobitechs.classapp.data.repository.PolicyTermConditionRepository
import com.mobitechs.classapp.data.repository.SearchRepository
import com.mobitechs.classapp.data.repository.ThemeRepository
import com.mobitechs.classapp.data.repository.UserRepository
import com.mobitechs.classapp.data.repository.chat.ChatRepository
import com.mobitechs.classapp.data.repository.chat.ChatUserRepository
import com.mobitechs.classapp.data.repository.chat.MessageRepository
import com.mobitechs.classapp.screens.videoPlayer.VideoPlayerViewModel
import com.mobitechs.classapp.screens.auth.AuthViewModel
import com.mobitechs.classapp.screens.batches.BatchViewModel
import com.mobitechs.classapp.screens.categoryDetails.SubCategoryViewModel
import com.mobitechs.classapp.screens.freeContent.FreeContentViewModel
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.notification.NotificationViewModel
import com.mobitechs.classapp.screens.offlineDownload.MyDownloadsViewModel
import com.mobitechs.classapp.screens.payment.PaymentHistoryViewModel
import com.mobitechs.classapp.screens.policyTermCondition.PolicyTermConditionViewModel
import com.mobitechs.classapp.screens.profile.FavouriteViewModel
import com.mobitechs.classapp.screens.profile.MyWishListViewModel
import com.mobitechs.classapp.screens.profile.ProfileViewModel
import com.mobitechs.classapp.screens.search.SearchViewModel
import com.mobitechs.classapp.screens.settings.ThemeSelectionViewModel
import com.mobitechs.classapp.screens.splash.SplashViewModel
import com.mobitechs.classapp.screens.store.CourseDetailViewModel
import com.mobitechs.classapp.screens.store.StoreViewModel
import com.mobitechs.classapp.viewModel.chat.ChatListViewModel
import com.mobitechs.classapp.viewModel.chat.ChatViewModel
import com.mobitechs.classapp.viewModel.chat.NewChatViewModel

/**
 * Factory class for creating ViewModels without dependency injection
 */
class ViewModelFactory(

    private val context: Context,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val batchRepository: BatchRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationRepository: NotificationRepository,
    private val paymentRepository: PaymentRepository,
    private val freeContentRepository: FreeContentRepository,
    private val myDownloadsRepository: MyDownloadsRepository,
    private val searchRepository: SearchRepository,
    private val policyTermConditionRepository: PolicyTermConditionRepository,
    private val chatUserRepository: ChatUserRepository,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val themeRepository: ThemeRepository
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
                HomeViewModel(
                    courseRepository,
                    categoryRepository,
                    notificationRepository,
                    authRepository
                ) as T
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

            modelClass.isAssignableFrom(VideoPlayerViewModel::class.java) -> {
                VideoPlayerViewModel() as T
            }

            modelClass.isAssignableFrom(SubCategoryViewModel::class.java) -> {
                SubCategoryViewModel(courseRepository, categoryRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository, authRepository, courseRepository) as T
            }


            modelClass.isAssignableFrom(FreeContentViewModel::class.java) -> {
                FreeContentViewModel(freeContentRepository,courseRepository,myDownloadsRepository) as T
            }

            modelClass.isAssignableFrom(PaymentHistoryViewModel::class.java) -> {
                PaymentHistoryViewModel(paymentRepository) as T
            }

            modelClass.isAssignableFrom(MyDownloadsViewModel::class.java) -> {
                MyDownloadsViewModel(myDownloadsRepository) as T
            }

            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepository) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(searchRepository,courseRepository,categoryRepository) as T
            }

            modelClass.isAssignableFrom(PolicyTermConditionViewModel::class.java) -> {
                PolicyTermConditionViewModel(policyTermConditionRepository) as T
            }

            modelClass.isAssignableFrom(FavouriteViewModel::class.java) -> {
                FavouriteViewModel(courseRepository) as T
            }

            modelClass.isAssignableFrom(MyWishListViewModel::class.java) -> {
                MyWishListViewModel(courseRepository) as T
            }

            modelClass.isAssignableFrom(ChatListViewModel::class.java) -> {
                ChatListViewModel(chatRepository,chatUserRepository,authRepository) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(chatRepository, messageRepository,authRepository,context) as T
            }
            modelClass.isAssignableFrom(NewChatViewModel::class.java) -> {
                NewChatViewModel(chatRepository, chatUserRepository,authRepository) as T
            }
            modelClass.isAssignableFrom(ThemeSelectionViewModel::class.java) -> {
                ThemeSelectionViewModel(themeRepository) as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}