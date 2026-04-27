package com.example.secureshastrya.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blockchain")
data class BlockchainBlock(
    @PrimaryKey(autoGenerate = true)
    val blockId: Int = 0,
    val blockIndex: Long,
    val timestamp: Long,
    val evidenceHash: String,
    val previousBlockHash: String,
    val integrityTag: String
)