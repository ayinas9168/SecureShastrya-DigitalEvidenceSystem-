package com.example.secureshastrya

import android.app.Application
import com.example.secureshastrya.data.AppDatabase

class SecureShastryaApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}