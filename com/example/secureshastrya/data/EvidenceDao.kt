package com.example.secureshastrya.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EvidenceDao {
    @Insert
    suspend fun insert(evidence: Evidence): Long

    @Update
    suspend fun update(evidence: Evidence)

    @Query("SELECT * FROM evidence WHERE caseId = :caseId")
    fun getEvidenceForCase(caseId: Int): LiveData<List<Evidence>>

    @Query("SELECT * FROM evidence WHERE caseId = :userId OR caseId IN (SELECT caseId FROM cases WHERE creatorId = :userId)")
    fun getAllEvidenceForUser(userId: Int): LiveData<List<Evidence>>

    @Query("SELECT * FROM evidence")
    suspend fun getAllEvidenceSync(): List<Evidence>

    @Query("SELECT * FROM evidence WHERE evidenceId = :evidenceId")
    suspend fun getEvidenceById(evidenceId: Int): Evidence?

    @Query("DELETE FROM evidence")
    suspend fun clearAll()
}