package com.example.secureshastrya.data

import androidx.lifecycle.LiveData
import com.example.secureshastrya.util.AsymmetricKeyManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val user: LiveData<User?> = userDao.getAnyUser()

    fun getUserById(userId: Int): LiveData<User?> {
        return userDao.getUserById(userId)
    }

    suspend fun isEmailWhitelistedAsJudge(email: String): Boolean {
        return try {
            val document = firestore.collection("judges_whitelist")
                .document(email.lowercase())
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerUser(name: String, email: String, phone: String, password: String, role: UserRole = UserRole.USER): Long {
        // 1. Create User in Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Failed to get Firebase UID")

        // 2. Generate asymmetric key pair
        AsymmetricKeyManager.createKeyPair()
        val publicKey = AsymmetricKeyManager.getPublicKeyBase64()

        val user = User(
            uid = uid,
            username = name,
            email = email,
            phone = phone,
            role = role,
            publicKey = publicKey
        )

        // 3. Save to Firestore
        firestore.collection("users").document(uid).set(user).await()

        // 4. Save to local Room DB
        return userDao.insert(user)
    }

    suspend fun loginUser(email: String, password: String): User {
        // 1. Login with Firebase Auth
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Login failed")

        // 2. Fetch User from Firestore
        val document = firestore.collection("users").document(uid).get().await()
        val user = document.toObject(User::class.java) ?: throw Exception("User data not found in cloud")

        // 3. Update local Room DB (Sync cloud to local)
        val localUser = userDao.getUserByEmail(email)
        if (localUser == null) {
            userDao.insert(user)
        } else {
            // Update if necessary, or just return the cloud one
        }

        return user
    }

    suspend fun clearAllUsers() {
        userDao.clearAllUsers()
    }
}
