package com.example.secureshastrya

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.data.EvidenceRepository
import com.example.secureshastrya.data.User
import com.example.secureshastrya.data.UserRepository
import com.example.secureshastrya.util.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val evidenceRepository: EvidenceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val user: LiveData<User?> = userRepository.getUserById(sessionManager.getUserId())
    val evidenceList: LiveData<List<Evidence>> = evidenceRepository.getEvidenceForCurrentUser()

    fun uploadEvidence(uri: Uri, name: String) {
        viewModelScope.launch {
            evidenceRepository.uploadEvidenceFromUri(uri, name)
        }
    }

    fun isBiometricEnabled(): Boolean {
        val userId = sessionManager.getUserId()
        return sessionManager.isBiometricEnabled(userId)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val userId = sessionManager.getUserId()
        sessionManager.setBiometricEnabled(userId, enabled)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun clearDatabase() {
        viewModelScope.launch {
            userRepository.clearAllUsers()
            evidenceRepository.clearAllEvidence()
            sessionManager.clearSession()
        }
    }
}