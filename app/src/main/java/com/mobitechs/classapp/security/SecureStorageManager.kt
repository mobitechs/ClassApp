package com.mobitechs.classapp.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class SecureStorageManager(private val context: Context) {

    private val KEYSTORE_ALIAS = "ClassAppContentKey"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/CBC/PKCS7Padding"

    init {
        generateKey()
    }

    private fun generateKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val ivFile = File(outputFile.parent, "${outputFile.name}.iv")
        ivFile.writeBytes(iv)

        FileInputStream(inputFile).use { input ->
            CipherOutputStream(FileOutputStream(outputFile), cipher).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getDecryptedInputStream(encryptedFile: File): InputStream? {
        return try {
            val ivFile = File(encryptedFile.parent, "${encryptedFile.name}.iv")
            val iv = ivFile.readBytes()

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

            CipherInputStream(FileInputStream(encryptedFile), cipher)
        } catch (e: Exception) {
            null
        }
    }
}