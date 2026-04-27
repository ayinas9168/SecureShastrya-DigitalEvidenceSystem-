package com.example.secureshastrya

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secureshastrya.auth.AuthActivity
import com.example.secureshastrya.data.AppDatabase
import com.example.secureshastrya.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- ONE-TIME ACTION: RESET DATA ---
        // Run this once to clear all users and start fresh.
        // resetAppForFreshStart()
        // ------------------------------------

        val judgeEmails = listOf("neha3243yadav@gmail.com", "judge1@secureshastrya.com")
        setupJudgesWhitelist(judgeEmails)

        val sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && sessionManager.getUserId() != -1) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 2000)
    }

    private fun resetAppForFreshStart() {
        val sessionManager = SessionManager(this)
        FirebaseAuth.getInstance().signOut()
        sessionManager.fullReset()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@SplashActivity)
                db.clearAllTables()
                Log.d("Reset", "Local database cleared")
            } catch (e: Exception) {
                Log.e("Reset", "Error", e)
            }
        }
    }

    private fun setupJudgesWhitelist(emails: List<String>) {
        val db = FirebaseFirestore.getInstance()
        for (email in emails) {
            val judgeData = hashMapOf("isAuthorized" to true)
            db.collection("judges_whitelist")
                .document(email.lowercase())
                .set(judgeData)
        }
    }
}
