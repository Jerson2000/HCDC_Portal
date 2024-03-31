package com.jerson.hcdc_portal.util

import android.content.Context
import android.content.SharedPreferences

class AppPreference(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PortalApp", Context.MODE_PRIVATE)

    fun setStringPreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getStringPreference(key: String, def: String = ""): String {
        return sharedPreferences.getString(key, def)!!
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
}