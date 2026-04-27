package com.example.secureshastrya.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secureshastrya.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailLinkHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val emailLink = intent.data.toString()

        if (Firebase.auth.isSignInWithEmailLink(emailLink)) {
            val sharedPref = getSharedPreferences("SignInPreferences", Context.MODE_PRIVATE)
            val email = sharedPref.getString("emailForSignIn", null)

            if (email == null) {
                Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            Firebase.auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        with(sharedPref.edit()) {
                            remove("emailForSignIn")
                            apply()
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to sign in: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
        } else {
            finish()
        }
    }
}