package com.example.secureshastrya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.UserRole
import com.example.secureshastrya.data.UserRepository
import kotlinx.coroutines.launch

sealed class RegistrationResult {
    data class Success(val userId: Int) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    fun register(name: String, email: String, phone: String, password: String, role: UserRole) {
        viewModelScope.launch {
            try {
                if (role == UserRole.JUDGE) {
                    val isWhitelisted = userRepository.isEmailWhitelistedAsJudge(email)
                    if (!isWhitelisted) {
                        _registrationResult.value = RegistrationResult.Error("Your email is not authorized as a Judge. Please contact the administrator.")
                        return@launch
                    }
                }

                val userId = userRepository.registerUser(name, email, phone, password, role)
                _registrationResult.value = RegistrationResult.Success(userId.toInt())
            } catch (e: Exception) {
                _registrationResult.value = RegistrationResult.Error(e.message ?: "An error occurred")
            }
        }
    }
}