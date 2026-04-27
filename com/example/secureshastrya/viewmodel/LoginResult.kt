package com.example.secureshastrya.viewmodel

sealed class LoginResult {
    data class Success(val userId: Int, val phone: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}