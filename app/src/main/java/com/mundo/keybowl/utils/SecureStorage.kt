package com.mundo.keybowl.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, token: String) {
        sharedPreferences.edit().apply {
            putString("KEY_EMAIL", email)
            putString("KEY_PASS", token)
            apply()
        }
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("KEY_EMAIL", null)
    }

    fun getPassword(): String? {
        return sharedPreferences.getString("KEY_PASS", null)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}