package com.mobitechs.classapp.screens.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.Course
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.data.repository.AuthRepository
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val user: Student? = null,
    val purchasedCourses: List<Course> = emptyList(),
    val favoriteCourses: List<Course> = emptyList(),
    val downloadedCourses: List<Course> = emptyList(),
    val wishlistCourses: List<Course> = emptyList(),
    val isLogoutDialogVisible: Boolean = false,
    val isEditProfileDialogVisible: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val userResponse = userRepository.getUserProfile()
                val userDetails = userResponse.student

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = userDetails
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun loadPurchasedCourses() {
        viewModelScope.launch {
            try {
                // In a real implementation, this would call a dedicated API
                val courses = emptyList<Course>() // Placeholder for purchased courses API

                _uiState.update {
                    it.copy(
                        purchasedCourses = courses
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load purchased courses"
                    )
                }
            }
        }
    }

    fun loadFavoriteCourses() {
        viewModelScope.launch {
            try {
                // In a real implementation, this would call a dedicated API
                val courses = emptyList<Course>() // Placeholder for favorite courses API

                _uiState.update {
                    it.copy(
                        favoriteCourses = courses
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load favorite courses"
                    )
                }
            }
        }
    }

    fun loadDownloadedCourses() {
        viewModelScope.launch {
            try {
                // In a real implementation, this would check local storage
                val courses = emptyList<Course>() // Placeholder for downloaded courses

                _uiState.update {
                    it.copy(
                        downloadedCourses = courses
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load downloaded courses"
                    )
                }
            }
        }
    }

    fun loadWishlistCourses() {
        viewModelScope.launch {
            try {
                // In a real implementation, this would call a dedicated API
                val courses = emptyList<Course>() // Placeholder for wishlist courses API

                _uiState.update {
                    it.copy(
                        wishlistCourses = courses
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load wishlist courses"
                    )
                }
            }
        }
    }

    fun updateUser(user: Student) {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val updatedUser = userRepository.updateUserProfile(user)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = updatedUser,
                        isEditProfileDialogVisible = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogVisible = true) }
    }

    fun hideLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
    }

    fun showEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogVisible = true) }
    }

    fun hideEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogVisible = false) }
    }

    fun logout() {
        authRepository.logout()
        // Navigation to login screen will be handled by the calling composable
    }
}