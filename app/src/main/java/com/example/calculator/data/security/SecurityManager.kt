package com.example.calculator.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(pin: String) {
        prefs.edit().putString("user_pin", pin).apply()
    }

    fun getPin(): String? = prefs.getString("user_pin", null)

    fun hasPin(): Boolean = getPin() != null

    fun verifyPin(input: String): Boolean = getPin() == input

    fun clearPin() {
        prefs.edit().remove("user_pin").apply()
    }
}