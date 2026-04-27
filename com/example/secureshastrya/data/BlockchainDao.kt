package com.example.secureshastrya.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BlockchainDao {
    @Insert
    suspend fun insertBlock(block: BlockchainBlock)

    @Query("SELECT * FROM blockchain ORDER BY blockIndex DESC LIMIT 1")
    suspend fun getLatestBlock(): BlockchainBlock?

    @Query("SELECT * FROM blockchain ORDER BY blockIndex ASC")
    suspend fun getAllBlocks(): List<BlockchainBlock>

    @Query("DELETE FROM blockchain")
    suspend fun clearAll()
}