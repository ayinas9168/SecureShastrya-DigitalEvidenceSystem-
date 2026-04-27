package com.example.secureshastrya.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CaseStatus {
    OPEN, CLOSED, DISMISSED
}

@Entity(tableName = "cases")
data class Case(
    @PrimaryKey(autoGenerate = true)
    val caseId: Int = 0,
    val creatorId: Int,
    val judgeId: Int?,
    val title: String,
    val description: String,
    val status: CaseStatus = CaseStatus.OPEN,
    val nextHearingDate: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
