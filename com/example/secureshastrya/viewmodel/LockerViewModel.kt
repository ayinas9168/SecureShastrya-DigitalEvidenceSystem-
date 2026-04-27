package com.example.secureshastrya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.Case
import com.example.secureshastrya.data.CaseDao
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.data.EvidenceDao
import com.example.secureshastrya.data.EvidenceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LockerViewModel(
    private val evidenceRepository: EvidenceRepository,
    private val evidenceDao: EvidenceDao,
    private val caseDao: CaseDao
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    fun getEvidenceForCase(caseId: Int): LiveData<List<Evidence>> {
        return evidenceDao.getAllEvidenceForUser(caseId)
    }

    fun addEvidence(evidence: Evidence) {
        viewModelScope.launch {
            evidenceRepository.addEvidence(evidence, null) // Already encrypted file exists in internal storage
        }
    }

    fun fileComplaint(title: String, description: String, creatorId: Int, selectedEvidence: List<Evidence>) {
        viewModelScope.launch {
            try {
                val newCase = Case(
                    creatorId = creatorId,
                    judgeId = null,
                    title = title,
                    description = description,
                    nextHearingDate = null
                )
                val caseId = caseDao.insert(newCase).toInt()

                for (evidence in selectedEvidence) {
                    val updatedEvidence = evidence.copy(caseId = caseId)
                    evidenceDao.update(updatedEvidence)
                    firestore.collection("evidence").document(evidence.filename).set(updatedEvidence).await()
                }

                firestore.collection("cases").document(caseId.toString()).set(newCase.copy(caseId = caseId)).await()

            } catch (e: Exception) {
                // Log error
            }
        }
    }
}
