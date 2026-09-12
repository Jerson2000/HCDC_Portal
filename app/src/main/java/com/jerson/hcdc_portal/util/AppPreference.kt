package com.jerson.hcdc_portal.util

import android.content.Context
import android.content.SharedPreferences

class AppPreference(
    context: Context,
    private val securePreference: SecurePreference = SecurePreference(context),
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PortalApp", Context.MODE_PRIVATE)
) {

    init {
        migrateLegacyPassword()
    }

    private fun migrateLegacyPassword() {
        // If an unencrypted password exists from an earlier app version, migrate it to secure storage
        if (sharedPreferences.contains(Constants.KEY_PASSWORD)) {
            val legacyPassword = sharedPreferences.getString(Constants.KEY_PASSWORD, null)
            if (!legacyPassword.isNullOrEmpty()) {
                securePreference.savePassword(legacyPassword)
            }
            // Remove cleartext password from plain XML storage
            sharedPreferences.edit().remove(Constants.KEY_PASSWORD).apply()
        }
    }

    fun setStringPreference(key: String, value: String) {
        if (key == Constants.KEY_PASSWORD) {
            securePreference.savePassword(value)
            // Ensure no cleartext duplicate exists in unencrypted preferences
            if (sharedPreferences.contains(Constants.KEY_PASSWORD)) {
                sharedPreferences.edit().remove(Constants.KEY_PASSWORD).apply()
            }
        } else {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }

    fun getStringPreference(key: String, def: String = ""): String {
        return if (key == Constants.KEY_PASSWORD) {
            val securePass = securePreference.getPassword()
            if (securePass.isNotEmpty()) securePass else def
        } else {
            sharedPreferences.getString(key, def) ?: def
        }
    }

    fun setIntPreference(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getIntPreference(key: String, def: Int = 0): Int {
        return sharedPreferences.getInt(key, def)
    }

    fun setLongPreference(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getLongPreference(key: String, def: Long = 0): Long {
        return sharedPreferences.getLong(key, def)
    }

    fun setBooleanPreference(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBooleanPreference(key: String, def: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, def)
    }

    fun clearPreference() {
        sharedPreferences.edit().clear().apply()
        securePreference.clearSecureData()
    }
}