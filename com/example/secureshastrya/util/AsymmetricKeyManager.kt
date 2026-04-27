package com.example.secureshastrya.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object AsymmetricKeyManager {

    private const val KEY_ALIAS = "secure_shastrya_asymmetric_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    /**
     * Generates a new RSA Key Pair in the Android Keystore.
     * The private key never leaves the hardware.
     */
    fun createKeyPair() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()
            kpg.initialize(parameterSpec)
            kpg.generateKeyPair()
        }
    }

    /**
     * Returns the Public Key in Base64 format for sharing.
     */
    fun getPublicKeyBase64(): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Encrypts data (e.g., an AES key) using a Judge's Public Key.
     */
    fun encryptWithPublicKey(data: ByteArray, publicKeyBase64: String): String {
        val publicBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
    }

    /**
     * Decrypts data using the device's Private Key (Judge's authorization).
     */
    fun decryptWithPrivateKey(encryptedDataBase64: String): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey

        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(encryptedDataBase64, Base64.NO_WRAP)
        return cipher.doFinal(encryptedData)
    }
}
