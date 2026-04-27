package com.example.secureshastrya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.data.AuditLog
import kotlinx.coroutines.launch

class AuditLogViewModel(application: Application) : AndroidViewModel(application) {

    private val auditLogDao = AppDatabase.getDatabase(application).auditLogDao()

    fun addLog(eventType: String, details: String) {
        viewModelScope.launch {
            val log = AuditLog(
                timestamp = System.currentTimeMillis(),
                eventType = eventType,
                details = details
            )
            auditLogDao.insert(log)
        }
    }
}