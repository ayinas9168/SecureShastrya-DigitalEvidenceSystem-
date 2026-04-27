package com.example.secureshastrya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.data.User
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Holds the username of the user currently authenticating
    var username: String? = null

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun loadUser() {
        username?.let {
            viewModelScope.launch {
                _user.value = userDao.getUserByEmail(it)
            }
        }
    }
}