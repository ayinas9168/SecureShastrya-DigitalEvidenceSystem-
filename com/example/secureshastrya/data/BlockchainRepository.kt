package com.example.secureshastrya.data

class BlockchainRepository(private val blockchainDao: BlockchainDao) {

    suspend fun getLatestBlock(): BlockchainBlock? {
        return blockchainDao.getLatestBlock()
    }

    suspend fun createNewBlock(evidenceHash: String) {
        val latestBlock = getLatestBlock()
        val newBlock = if (latestBlock != null) {
            BlockchainBlock(
                blockIndex = latestBlock.blockIndex + 1,
                timestamp = System.currentTimeMillis(),
                evidenceHash = evidenceHash,
                previousBlockHash = latestBlock.integrityTag,
                integrityTag = ""
            )
        } else {
            BlockchainBlock(
                blockIndex = 0,
                timestamp = System.currentTimeMillis(),
                evidenceHash = evidenceHash,
                previousBlockHash = "0",
                integrityTag = ""
            )
        }

        val blockData = "${newBlock.blockIndex}${newBlock.timestamp}${newBlock.evidenceHash}${newBlock.previousBlockHash}"
        val integrityTag = java.security.MessageDigest.getInstance("SHA-256")
            .digest(blockData.toByteArray())
            .joinToString("") { "%02x".format(it) }

        blockchainDao.insertBlock(newBlock.copy(integrityTag = integrityTag))
    }

    suspend fun clearAll() {
        blockchainDao.clearAll()
    }
}