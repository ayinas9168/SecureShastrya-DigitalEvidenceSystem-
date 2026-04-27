package com.example.secureshastrya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.data.BlockchainBlock
import com.example.secureshastrya.util.SecurityUtils
import kotlinx.coroutines.launch

class BlockchainViewModel(application: Application) : AndroidViewModel(application) {

    private val blockchainDao = AppDatabase.getDatabase(application).blockchainDao()

    fun addBlock(evidenceHash: String) {
        viewModelScope.launch {
            val latestBlock = blockchainDao.getLatestBlock()
            val newIndex = (latestBlock?.blockIndex ?: -1) + 1
            val previousBlockHash = latestBlock?.integrityTag ?: "0"

            val timestamp = System.currentTimeMillis()
            val blockContent = "$newIndex$timestamp$evidenceHash$previousBlockHash"
            val integrityTag = SecurityUtils.sha256(blockContent)

            val newBlock = BlockchainBlock(
                blockIndex = newIndex,
                timestamp = timestamp,
                evidenceHash = evidenceHash,
                previousBlockHash = previousBlockHash,
                integrityTag = integrityTag
            )

            blockchainDao.insertBlock(newBlock)
        }
    }
}