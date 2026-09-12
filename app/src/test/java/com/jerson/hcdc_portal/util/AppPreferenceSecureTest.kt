package com.jerson.hcdc_portal.util

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class AppPreferenceSecureTest {

    private class FakeSharedPreferences {
        val map = mutableMapOf<String, Any>()
        val removedKeys = mutableListOf<String>()
        var cleared = false

        val editorProxy: SharedPreferences.Editor

        init {
            val editorHandler = object : InvocationHandler {
                var proxyInstance: SharedPreferences.Editor? = null

                override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                    return when (method.name) {
                        "putString" -> {
                            val key = args?.get(0) as String
                            val value = args?.get(1) as String
                            map[key] = value
                            proxyInstance
                        }
                        "remove" -> {
                            val key = args?.get(0) as String
                            map.remove(key)
                            removedKeys.add(key)
                            proxyInstance
                        }
                        "clear" -> {
                            map.clear()
                            cleared = true
                            proxyInstance
                        }
                        "apply", "commit" -> {
                            if (method.returnType == Boolean::class.javaPrimitiveType) true else null
                        }
                        else -> proxyInstance
                    }
                }
            }

            editorProxy = Proxy.newProxyInstance(
                SharedPreferences.Editor::class.java.classLoader,
                arrayOf(SharedPreferences.Editor::class.java),
                editorHandler
            ) as SharedPreferences.Editor

            editorHandler.proxyInstance = editorProxy
        }

        val prefsProxy: SharedPreferences = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method: Method, args: Array<out Any>? ->
            when (method.name) {
                "edit" -> editorProxy
                "contains" -> {
                    val key = args?.get(0) as String
                    map.containsKey(key)
                }
                "getString" -> {
                    val key = args?.get(0) as String
                    val def = args?.getOrNull(1) as? String
                    map[key] as? String ?: def
                }
                else -> null
            }
        } as SharedPreferences
    }

    private class TestableSecurePreference(context: Context) : SecurePreference(context) {
        var savedPasswordValue: String = ""
        var clearCalled: Boolean = false

        override fun savePassword(password: String) {
            savedPasswordValue = password
        }

        override fun getPassword(): String {
            return savedPasswordValue
        }

        override fun clearSecureData() {
            savedPasswordValue = ""
            clearCalled = true
        }
    }

    private val dummyContext: Context = ContextWrapper(null)

    @Test
    fun legacyPasswordMigration_migratesCleartextAndDeletesFromPlainPrefs() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.map[Constants.KEY_PASSWORD] = "legacySecret123"
        val securePref = TestableSecurePreference(dummyContext)

        val appPreference = AppPreference(dummyContext, securePref, fakePrefs.prefsProxy)

        // Password should now be in secure storage
        assertEquals("legacySecret123", securePref.savedPasswordValue)
        // Plaintext password should be purged from unencrypted SharedPreferences
        assertFalse(fakePrefs.map.containsKey(Constants.KEY_PASSWORD))
        assertTrue(fakePrefs.removedKeys.contains(Constants.KEY_PASSWORD))
    }

    @Test
    fun setPassword_routesToSecurePreferenceNotPlainPrefs() {
        val fakePrefs = FakeSharedPreferences()
        val securePref = TestableSecurePreference(dummyContext)

        val appPreference = AppPreference(dummyContext, securePref, fakePrefs.prefsProxy)
        appPreference.setStringPreference(Constants.KEY_PASSWORD, "myNewSecurePass")

        // Written to secure preference
        assertEquals("myNewSecurePass", securePref.savedPasswordValue)
        // Never stored in plain prefs
        assertFalse(fakePrefs.map.containsKey(Constants.KEY_PASSWORD))
    }

    @Test
    fun getPassword_fetchesFromSecurePreference() {
        val fakePrefs = FakeSharedPreferences()
        val securePref = TestableSecurePreference(dummyContext).apply {
            savedPasswordValue = "secureDecryptedPass"
        }

        val appPreference = AppPreference(dummyContext, securePref, fakePrefs.prefsProxy)
        val pass = appPreference.getStringPreference(Constants.KEY_PASSWORD)

        assertEquals("secureDecryptedPass", pass)
    }

    @Test
    fun nonPasswordPreferences_continueToWorkInNormalStorage() {
        val fakePrefs = FakeSharedPreferences()
        val securePref = TestableSecurePreference(dummyContext)

        val appPreference = AppPreference(dummyContext, securePref, fakePrefs.prefsProxy)
        appPreference.setStringPreference("other_key", "other_value")

        assertEquals("other_value", fakePrefs.map["other_key"])
        assertEquals("other_value", appPreference.getStringPreference("other_key"))
    }

    @Test
    fun clearPreference_clearsBothPlainAndSecureStorage() {
        val fakePrefs = FakeSharedPreferences()
        val securePref = TestableSecurePreference(dummyContext).apply {
            savedPasswordValue = "to_be_cleared"
        }

        val appPreference = AppPreference(dummyContext, securePref, fakePrefs.prefsProxy)
        appPreference.clearPreference()

        assertTrue(fakePrefs.cleared)
        assertTrue(securePref.clearCalled)
        assertEquals("", securePref.savedPasswordValue)
    }
}
