package com.example.secureshastrya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.secureshastrya.ProfileViewModel
import com.example.secureshastrya.SecureShastryaApplication
import com.example.secureshastrya.data.BlockchainRepository
import com.example.secureshastrya.data.EvidenceRepository
import com.example.secureshastrya.data.UserRepository
import com.example.secureshastrya.util.SessionManager

class ViewModelFactory(private val application: SecureShastryaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val userDao = application.database.userDao()
        val evidenceDao = application.database.evidenceDao()
        val blockchainDao = application.database.blockchainDao()
        val caseDao = application.database.caseDao()
        
        val userRepository = UserRepository(userDao)
        val blockchainRepository = BlockchainRepository(blockchainDao)
        val evidenceRepository = EvidenceRepository(application, evidenceDao, blockchainRepository)
        val sessionManager = SessionManager(application.applicationContext)

        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(userRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(userRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(userRepository, evidenceRepository, sessionManager) as T
            modelClass.isAssignableFrom(LockerViewModel::class.java) ->
                LockerViewModel(evidenceRepository, evidenceDao, caseDao) as T
            modelClass.isAssignableFrom(CaptureViewModel::class.java) ->
                CaptureViewModel(evidenceRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
