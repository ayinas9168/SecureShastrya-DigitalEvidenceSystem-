package com.example.secureshastrya.util

import android.content.Context
import android.os.Environment
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * Manages a persistent secure vault file that survives app uninstallation.
 * Uses AES-256 encryption via Android Security Crypto library (v1.0.0).
 */
class VaultManager(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val vaultFileName = ".secure_shastrya_vault.dat"

    /**
     * Returns the file object for the vault in a public directory to ensure 
     * persistence across uninstalls.
     */
    private fun getVaultFile(): File {
        // Storing in the public Documents directory to survive app uninstallation
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!publicDir.exists()) {
            publicDir.mkdirs()
        }
        return File(publicDir, vaultFileName)
    }

    /**
     * Saves sensitive user data to the encrypted vault.
     */
    fun saveToVault(data: String) {
        val file = getVaultFile()
        if (file.exists()) {
            file.delete()
        }

        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val outputStream: OutputStream = encryptedFile.openFileOutput()
        outputStream.write(data.toByteArray(StandardCharsets.UTF_8))
        outputStream.flush()
        outputStream.close()
    }

    /**
     * Reads and decrypts data from the vault.
     */
    fun readFromVault(): String? {
        val file = getVaultFile()
        if (!file.exists()) return null

        return try {
            val encryptedFile = EncryptedFile.Builder(
                file,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            val inputStream: InputStream = encryptedFile.openFileInput()
            val byteArray = inputStream.readBytes()
            inputStream.close()
            String(byteArray, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks if the persistent vault exists on the device.
     */
    fun isVaultPresent(): Boolean {
        return getVaultFile().exists()
    }
}