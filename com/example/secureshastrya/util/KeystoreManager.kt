package com.example.secureshastrya.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "master_key"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getMasterKey(): SecretKey {
        return (keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateMasterKey()
    }

    private fun generateMasterKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val parameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> {
        val masterKey = getMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return Pair(iv, encryptedData)
    }

    fun decrypt(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val masterKey = getMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec)
        return cipher.doFinal(encryptedData)
    }
}