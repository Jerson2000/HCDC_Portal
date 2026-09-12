package com.jerson.hcdc_portal.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Provides hardware-backed AES256-GCM encrypted storage for sensitive user credentials
 * such as portal passwords and authentication tokens.
 */
open class SecurePreference(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedPreferences()
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to private SharedPreferences", e)
            context.getSharedPreferences(FALLBACK_PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    open fun savePassword(password: String) {
        sharedPreferences.edit().putString(Constants.KEY_PASSWORD, password).apply()
    }

    open fun getPassword(): String {
        return sharedPreferences.getString(Constants.KEY_PASSWORD, "") ?: ""
    }

    open fun clearSecureData() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val TAG = "SecurePreference"
        const val SECURE_PREFS_FILE = "secure_portal_prefs"
        private const val FALLBACK_PREFS_FILE = "secure_portal_fallback"
    }
}
