package com.example.secureshastrya.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CaseDao {
    @Insert
    suspend fun insert(caseEntity: Case): Long

    @Update
    suspend fun update(caseEntity: Case)

    @Query("SELECT * FROM cases WHERE caseId = :caseId")
    fun getCaseById(caseId: Int): LiveData<Case?>

    @Query("SELECT * FROM cases WHERE creatorId = :userId")
    fun getCasesForUser(userId: Int): LiveData<List<Case>>

    @Query("SELECT * FROM cases WHERE judgeId = :judgeId")
    fun getCasesForJudge(judgeId: Int): LiveData<List<Case>>

    @Query("SELECT * FROM cases")
    fun getAllCases(): LiveData<List<Case>>
}
