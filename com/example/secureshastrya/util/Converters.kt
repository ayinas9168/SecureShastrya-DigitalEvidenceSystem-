package com.example.secureshastrya.util

import androidx.room.TypeConverter
import com.example.secureshastrya.data.CaseStatus
import com.example.secureshastrya.data.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }

    @TypeConverter
    fun fromCaseStatus(value: CaseStatus): String {
        return value.name
    }

    @TypeConverter
    fun toCaseStatus(value: String): CaseStatus {
        return CaseStatus.valueOf(value)
    }
}
