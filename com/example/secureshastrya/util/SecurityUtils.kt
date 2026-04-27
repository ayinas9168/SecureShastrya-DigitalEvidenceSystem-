package com.example.secureshastrya.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return factory.generateSecret(spec).encoded
    }

    fun sha256(input: String): String {
        return sha256(input.toByteArray())
    }

    fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    // Note: For real forensics, use Android Keystore to store this key. 
    // This is a simplified version for the structure.
    fun encryptData(data: ByteArray, key: SecretKey): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return Pair(iv, encryptedData)
    }

    fun decryptData(encryptedData: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        return cipher.doFinal(encryptedData)
    }
}