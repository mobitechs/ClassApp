package com.mobitechs.classapp.viewModel.profile

import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.data.repository.ProfileRepository
import com.mobitechs.classapp.utils.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val nameError: String = "",
    val email: String = "",
    val emailError: String = "",
    val phone: String = "",
    val phoneError: String = "",
    val gender: String = "",
    val genderError: String = "",
    val bloodGroup: String = "",
    val bloodGroupError: String = "",
    val address: String = "",
    val addressError: String = "",
    val city: String = "",
    val cityError: String = "",
    val pincode: String = "",
    val pincodeError: String = "",
    val adharNo: String = "",
    val adharNoError: String = "",
    val panNo: String = "",
    val panNoError: String = "",
    val profileImageUrl: String = "",
    val profileImagePath: String = "",  // For storing actual file path
    val isProfileUpdated: Boolean = false,
    val currentUser: Student? = null
)

class EditProfileViewModel(
    private val profileRepository: ProfileRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserDataFromSession()
    }

    private fun loadUserDataFromSession() {
        // Get user data from repository (which gets it from SharedPrefsManager)
        val student = profileRepository.getUserFromSession()

        if (student != null) {
            _uiState.value = _uiState.value.copy(
                name = student.name ?: "",
                email = student.email ?: "",
                phone = student.phone ?: "",
                gender = student.gender ?: "",
                bloodGroup = student.bloodGroup ?: "",
                address = student.address ?: "",
                city = student.city ?: "",
                pincode = student.pincode ?: "",
                adharNo = student.adharNo ?: "",
                panNo = student.panNo ?: "",
                profileImageUrl = student.photo ?: "",
                currentUser = student,
                isLoading = false
            )
        } else {
            showToast("No user data found. Please login again.")
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = "")
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = "")
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, phoneError = "")
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender, genderError = "")
    }

    fun updateBloodGroup(bloodGroup: String) {
        _uiState.value = _uiState.value.copy(bloodGroup = bloodGroup, bloodGroupError = "")
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address, addressError = "")
    }

    fun updateCity(city: String) {
        _uiState.value = _uiState.value.copy(city = city, cityError = "")
    }

    fun updatePincode(pincode: String) {
        _uiState.value = _uiState.value.copy(pincode = pincode, pincodeError = "")
    }

    fun updateAdharNo(adharNo: String) {
        _uiState.value = _uiState.value.copy(adharNo = adharNo, adharNoError = "")
    }

    fun updatePanNo(panNo: String) {
        _uiState.value = _uiState.value.copy(panNo = panNo, panNoError = "")
    }

    fun updateProfileImage(imagePath: String) {
        _uiState.value = _uiState.value.copy(
            profileImageUrl = imagePath,
            profileImagePath = imagePath  // Store for API upload
        )
    }

    fun saveProfile() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = _uiState.value.currentUser
                val updatedStudent = Student(
                    id = currentUser?.id ?: 0,
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    phone = _uiState.value.phone,
                    gender = _uiState.value.gender,
                    bloodGroup = _uiState.value.bloodGroup,
                    address = _uiState.value.address,
                    city = _uiState.value.city,
                    pincode = _uiState.value.pincode,
                    adharNo = _uiState.value.adharNo,
                    panNo = _uiState.value.panNo,
                    photo = if (_uiState.value.profileImagePath.isNotEmpty()) {
                        _uiState.value.profileImagePath
                    } else {
                        currentUser?.photo ?: ""
                    },
                    password = currentUser?.password ?: "",
                    added_by = currentUser?.added_by ?: "",
                    adharImage = currentUser?.adharImage ?: "",
                    created_at = currentUser?.created_at ?: "",
                    deleted_at = currentUser?.deleted_at ?: "",
                    is_active = currentUser?.is_active ?: 1,
                    panImage = currentUser?.panImage ?: "",
                    signature = currentUser?.signature ?: "",
                    updated_at = currentUser?.updated_at ?: ""
                )

                // Make API call to update profile
                profileRepository.updateProfile(updatedStudent)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProfileUpdated = true
                )
                showToast("Profile updated successfully")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                showToast("Failed to update profile: ${e.message}")
            }
        }
    }

    // Optional: Add a refresh function if you want to fetch latest data from API
    fun refreshProfileFromApi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val student = profileRepository.getUserProfile()
                _uiState.value = _uiState.value.copy(
                    name = student.name ?: "",
                    email = student.email ?: "",
                    phone = student.phone ?: "",
                    gender = student.gender ?: "",
                    bloodGroup = student.bloodGroup ?: "",
                    address = student.address ?: "",
                    city = student.city ?: "",
                    pincode = student.pincode ?: "",
                    adharNo = student.adharNo ?: "",
                    panNo = student.panNo ?: "",
                    profileImageUrl = student.photo ?: "",
                    currentUser = student,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                showToast("Failed to refresh profile: ${e.message}")
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (_uiState.value.name.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            isValid = false
        } else if (_uiState.value.name.trim().length < 3) {
            _uiState.value = _uiState.value.copy(nameError = "Name must be at least 3 characters")
            isValid = false
        }

        if (_uiState.value.phone.trim().isNotEmpty()) {
            val phoneRegex = Regex("^[0-9]{10}$")
            if (!phoneRegex.matches(_uiState.value.phone.trim())) {
                _uiState.value = _uiState.value.copy(phoneError = "Enter a valid 10-digit phone number")
                isValid = false
            }
        }

        if (_uiState.value.email.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email.trim()).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Enter a valid email address")
            isValid = false
        }

        if (_uiState.value.pincode.trim().isNotEmpty()) {
            val pincodeRegex = Regex("^[0-9]{6}$")
            if (!pincodeRegex.matches(_uiState.value.pincode.trim())) {
                _uiState.value = _uiState.value.copy(pincodeError = "Enter a valid 6-digit pincode")
                isValid = false
            }
        }

        if (_uiState.value.adharNo.trim().isNotEmpty()) {
            val adharRegex = Regex("^[0-9]{12}$")
            if (!adharRegex.matches(_uiState.value.adharNo.trim())) {
                _uiState.value = _uiState.value.copy(adharNoError = "Enter a valid 12-digit Aadhar number")
                isValid = false
            }
        }

        if (_uiState.value.panNo.trim().isNotEmpty()) {
            val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
            if (!panRegex.matches(_uiState.value.panNo.trim())) {
                _uiState.value = _uiState.value.copy(panNoError = "Enter a valid PAN number")
                isValid = false
            }
        }

        return isValid
    }
}