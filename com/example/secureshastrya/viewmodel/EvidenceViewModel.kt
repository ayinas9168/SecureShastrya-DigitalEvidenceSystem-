package com.example.secureshastrya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.data.BlockchainRepository
import com.example.secureshastrya.data.Evidence
import com.example.secureshastrya.util.SecurityUtils
import kotlinx.coroutines.launch

class EvidenceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val evidenceDao = db.evidenceDao()
    private val blockchainRepository = BlockchainRepository(db.blockchainDao())

    fun captureAndSecureEvidence(
        caseId: String,
        title: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        deviceId: String
    ) {
        viewModelScope.launch {
            // 1. Generate Hash of the "file" (for now using path/metadata for simulation)
            val fileHash = SecurityUtils.sha256(filePath + System.currentTimeMillis())

            // 2. Save Evidence Metadata
            val evidence = Evidence(
                caseId = caseId.toInt(),
                name = title,
                filename = title,
                mediaType = "", // Assuming mediaType is not available
                sha256Hash = fileHash,
                gpsCoordinates = "$latitude,$longitude",
                timestamp = System.currentTimeMillis(),
                encryptionKey = "", // Placeholder
                iv = "", // Placeholder
                keyIv = "" // Placeholder
            )
            val evidenceId = evidenceDao.insert(evidence)

            // 3. Add to Blockchain Ledger for Integrity
            blockchainRepository.createNewBlock(fileHash)
        }
    }
}
