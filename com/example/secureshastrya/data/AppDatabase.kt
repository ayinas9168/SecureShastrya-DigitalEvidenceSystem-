package com.example.secureshastrya.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.secureshastrya.util.Converters

@Database(entities = [User::class, Evidence::class, BlockchainBlock::class, AuditLog::class, Case::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun blockchainDao(): BlockchainDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun caseDao(): CaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "secure_shastrya_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}