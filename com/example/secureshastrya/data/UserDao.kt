package com.example.secureshastrya.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserById(userId: Int): LiveData<User?>

    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE role = 'JUDGE'")
    fun getAllJudges(): LiveData<List<User>>

    @Query("SELECT * FROM users LIMIT 1")
    fun getAnyUser(): LiveData<User?>

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}