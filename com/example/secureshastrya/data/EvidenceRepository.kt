package com.example.secureshastrya.data

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import com.example.secureshastrya.util.KeystoreManager
import com.example.secureshastrya.util.SecurityUtils
import com.example.secureshastrya.util.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class EvidenceRepository(
    private val application: Application,
    private val evidenceDao: EvidenceDao,
    private val blockchainRepository: BlockchainRepository
) {

    private val sessionManager = SessionManager(application.applicationContext)
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getEvidenceForCurrentUser(): LiveData<List<Evidence>> {
        val currentUserId = sessionManager.getUserId()
        return evidenceDao.getEvidenceForCase(currentUserId)
    }

    suspend fun addEvidence(evidence: Evidence, file: File?) {
        // 1. Save to local Room DB
        evidenceDao.insert(evidence)
        
        // 2. Add to Blockchain
        blockchainRepository.createNewBlock(evidence.sha256Hash)

        // 3. Upload to Firebase (Metadata & File)
        try {
            var cloudUrl: String? = null
            if (file != null && file.exists()) {
                val currentUserId = sessionManager.getUserId()
                val storageRef = storage.reference.child("evidence/$currentUserId/${file.name}")
                storageRef.putBytes(file.readBytes()).await()
                cloudUrl = storageRef.downloadUrl.await().toString()
            }
            
            val finalEvidence = if (cloudUrl != null) evidence.copy(cloudUrl = cloudUrl) else evidence
            firestore.collection("evidence").document(evidence.filename).set(finalEvidence).await()
            
            if (cloudUrl != null) {
                evidenceDao.update(finalEvidence)
            }
        } catch (e: Exception) {
            // Log cloud sync failure
        }
    }

    suspend fun uploadEvidenceFromUri(uri: Uri, name: String) {
        val context = application.applicationContext
        val currentUserId = sessionManager.getUserId()

        val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val sha256Hash = SecurityUtils.sha256(fileBytes)
        val dataKey = SecurityUtils.generateAESKey()
        val (iv, encryptedData) = SecurityUtils.encryptData(fileBytes, dataKey)
        val (keyIv, encryptedKey) = KeystoreManager.encrypt(dataKey.encoded)

        val fileName = "evidence_${System.currentTimeMillis()}.enc"
        val internalFile = File(context.filesDir, fileName)
        FileOutputStream(internalFile).use { it.write(encryptedData) }

        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        
        val evidence = Evidence(
            caseId = currentUserId,
            name = name,
            filename = fileName,
            mediaType = mimeType,
            sha256Hash = sha256Hash,
            gpsCoordinates = "Uploaded",
            timestamp = System.currentTimeMillis(),
            encryptionKey = Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            keyIv = Base64.encodeToString(keyIv, Base64.NO_WRAP)
        )

        addEvidence(evidence, internalFile)
    }

    suspend fun clearAllEvidence() {
        evidenceDao.clearAll()
        blockchainRepository.clearAll()
        // Also delete physical files
        application.filesDir.listFiles()?.forEach { it.delete() }
    }
}
