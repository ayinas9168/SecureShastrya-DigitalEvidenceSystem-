package com.example.secureshastrya.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("secure_shastrya_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LAST_USER_ID = "last_user_id"
        private const val KEY_BIOMETRIC_ENABLED_PREFIX = "biometric_enabled_"
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
        if (userId != -1) {
            prefs.edit().putInt(KEY_LAST_USER_ID, userId).apply()
        }
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getLastUserId(): Int {
        return prefs.getInt(KEY_LAST_USER_ID, -1)
    }

    fun setBiometricEnabled(userId: Int, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED_PREFIX + userId, enabled).apply()
    }

    fun isBiometricEnabled(userId: Int): Boolean {
        return if (userId == -1) false else prefs.getBoolean(KEY_BIOMETRIC_ENABLED_PREFIX + userId, false)
    }

    fun clearSession() {
        prefs.edit().putInt(KEY_USER_ID, -1).apply()
    }

    fun fullReset() {
        prefs.edit().clear().apply()
    }
}
