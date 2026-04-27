package com.example.secureshastrya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.Case
import com.example.secureshastrya.data.CaseDao
import com.example.secureshastrya.data.CaseStatus
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.data.EvidenceDao
import com.example.secureshastrya.util.SessionManager
import kotlinx.coroutines.launch

class JudgeDashboardViewModel(
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    val assignedCases: LiveData<List<Case>> = caseDao.getCasesForJudge(sessionManager.getUserId())

    fun getEvidenceForCase(caseId: Int): LiveData<List<Evidence>> {
        return evidenceDao.getEvidenceForCase(caseId)
    }

    fun updateCaseStatus(case: Case, newStatus: CaseStatus) {
        viewModelScope.launch {
            caseDao.update(case.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteEvidence(evidence: Evidence) {
        viewModelScope.launch {
            // In a real scenario, this would also trigger a request to the cloud to delete the file
            // verified by the Judge's private key via a secure backend function.
            evidenceDao.update(evidence.copy(isDeletedByJudge = true))
            // Note: In local DB, we might mark it as deleted, 
            // but the actual cloud deletion would require asymmetric signature verification.
        }
    }
}
