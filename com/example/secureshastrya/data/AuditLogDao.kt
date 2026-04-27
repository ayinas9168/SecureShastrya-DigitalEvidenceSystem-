package com.example.secureshastrya.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insert(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll()
}