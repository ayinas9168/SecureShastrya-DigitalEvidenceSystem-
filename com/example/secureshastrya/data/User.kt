package com.example.secureshastrya.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    USER, JUDGE
}

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true), Index(value = ["uid"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val uid: String = "", // Firebase User ID
    val username: String? = null,
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.USER,
    val publicKey: String? = null // For asymmetric key pairing with Judge
)
