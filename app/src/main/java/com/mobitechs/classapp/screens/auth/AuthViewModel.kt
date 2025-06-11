package com.mobitechs.classapp.screens.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.request.LoginRequest
import com.mobitechs.classapp.data.model.request.RegisterRequest
import com.mobitechs.classapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI State for authentication screens
data class AuthUiState(
    // Login fields
//    val phone: String = "8655883062",
    val email: String = "pratik@gmail.com",
//
//    // Registration fields
//    val name: String = "Pratik Sonawane",
//    val gender: String = "male",
    val password: String = "Pratik@10",
//    val password_confirmation: String = "",
//    val city: String = "Mumbai",
//    val pincode: String = "421201",

    val phone: String = "",
//    val email: String = "",

    // Registration fields
    val name: String = "",
    val gender: String = "",
//    val password: String = "",
    val password_confirmation: String = "",
    val city: String = "",
    val pincode: String = "",

    val nameError: String = "",
    val emailError: String = "",
    val phoneError: String = "",
    val passwordError: String = "",
    val password_confirmationError: String = "",
    val genderError: String = "",
    val cityError: String = "",
    val pincodeError: String = "",


    // Common states
    val isLoading: Boolean = false,
    val error: String = "",
    val onNavigateToHome: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Add this method to the AuthViewModel class
    fun isUserLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Update functions for form fields

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = validateName(name)) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = validatePhone(phone)) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = validateEmail(email)) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = validatePassword(password)) }
    }

    fun updateConfirmPassword(password_confirmation: String) {
        _uiState.update {
            it.copy(
                password_confirmation = password_confirmation,
                password_confirmationError = validateConfirmPassword(
                    password_confirmation,
                    it.password
                )
            )
        }
    }

    fun updateGender(gender: String) {
        _uiState.update { it.copy(gender = gender, genderError = validateGender(gender)) }
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city, cityError = validateCity(city)) }
    }

    fun updatePinCode(pincode: String) {
        _uiState.update { it.copy(pincode = pincode, pincodeError = validatePinCode(pincode)) }
    }


    // Login with email
    fun loginWithEmail() {
        val email = uiState.value.email
        val password = uiState.value.password
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError.isNotEmpty()) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }
        if (passwordError.isNotEmpty()) {
            _uiState.update { it.copy(passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = authRepository.login(request)

                if (response.status_code == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            onNavigateToHome = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
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

    // Register new user
    fun register() {
        val name = uiState.value.name
        val email = uiState.value.email
        val phone = uiState.value.phone
        val gender = uiState.value.gender
        val password = uiState.value.password
        val password_confirmation = uiState.value.password_confirmation
        val city = uiState.value.city
        val pincode = uiState.value.pincode

        val nameError = validateName(name)
        val emailError = validateEmail(email)
        val phoneError = validatePhone(phone)
        val passwordError = validatePassword(password)
        val password_confirmationError = validateConfirmPassword(password_confirmation, password)
        val genderError = validateGender(gender)
        val cityError = validateCity(city)
        val pincodeError = validatePinCode(pincode)

        if (nameError.isNotEmpty() || emailError.isNotEmpty() || phoneError.isNotEmpty() ||
            passwordError.isNotEmpty() || password_confirmationError.isNotEmpty() ||
            genderError.isNotEmpty() || cityError.isNotEmpty() || pincodeError.isNotEmpty()
        ) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    password_confirmationError = password_confirmationError,
                    genderError = genderError,
                    cityError = cityError,
                    pincodeError = pincodeError,
                    error = if (gender.isEmpty()) "Please select a gender" else ""
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    password_confirmation = password_confirmation,
                    gender = gender,
                    city = city,
                    pincode = pincode,
                )
                val response = authRepository.register(request)

                if (response.status_code == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            onNavigateToHome = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
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


    // Reset navigation flag
    fun resetNavigation() {
        _uiState.update { it.copy(onNavigateToHome = false) }
    }

    // Validation functions
    private fun validatePhone(phone: String): String {
        return when {
            phone.isEmpty() -> "Phone number is required"
            phone.length < 10 -> "Phone number must be at least 10 digits"
            else -> ""
        }
    }

    private fun validateEmail(email: String): String {
        return when {
            email.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> "Invalid email format"

            else -> ""
        }
    }

    private fun validateName(name: String): String {
        return when {
            name.isEmpty() -> "Name is required"
            name.length < 3 -> "Name must be at least 3 characters"
            else -> ""
        }
    }

    private fun validatePassword(password: String): String {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 5 -> "Password must be at least 6 characters"
            else -> ""
        }
    }

    private fun validateConfirmPassword(
        password_confirmation: String,
        password: String
    ): String {
        return when {
            password_confirmation.isEmpty() -> "Password is required"
//                password_confirmation.length < 2 -> "Password must be at least 6 characters"
            password_confirmation != password -> "Password & Confirm Password must be same"
            else -> ""
        }
    }

    private fun validateGender(gender: String): String {
        return when {
            gender.isEmpty() -> "Name is required"
            else -> ""
        }
    }

    private fun validateCity(city: String): String {
        return when {
            city.isEmpty() -> "City is required"
            city.length < 3 -> "City must be at least 4 characters"
            else -> ""
        }
    }

    private fun validatePinCode(pincode: String): String {
        return when {
            pincode.isEmpty() -> "Pin Code is required"
            pincode.length == 5 -> "Pin Code must be at least 6 characters"
            else -> ""
        }
    }


    fun logout() {
        authRepository.logout()
        // Navigation to login screen will be handled by the calling composable
    }
}

