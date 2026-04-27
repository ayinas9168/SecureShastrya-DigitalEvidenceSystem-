package com.example.secureshastrya.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidence")
data class Evidence(
    @PrimaryKey(autoGenerate = true)
    val evidenceId: Int = 0,
    val caseId: Int,
    val name: String,
    val filename: String,
    val mediaType: String,
    val sha256Hash: String,
    val gpsCoordinates: String,
    val timestamp: Long,
    val encryptionKey: String, // Encrypted AES key (using Judge's public key)
    val iv: String,
    val keyIv: String,
    val isDeletedByJudge: Boolean = false,
    val cloudUrl: String? = null // For cloud storage integration
)