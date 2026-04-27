package com.example.secureshastrya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.loginUser(email, password)
                _loginResult.value = LoginResult.Success(user.userId, user.phone)
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error(e.message ?: "Login failed")
            }
        }
    }
}
