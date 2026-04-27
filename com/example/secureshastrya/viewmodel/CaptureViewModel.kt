package com.example.secureshastrya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.data.EvidenceRepository
import kotlinx.coroutines.launch
import java.io.File

class CaptureViewModel(private val evidenceRepository: EvidenceRepository) : ViewModel() {

    fun addEvidence(evidence: Evidence, file: File) {
        viewModelScope.launch {
            evidenceRepository.addEvidence(evidence, file)
        }
    }
}
