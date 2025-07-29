package com.jerson.hcdc_portal.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.presentation.login.LoginKt
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_IS_LOGIN
import com.jerson.hcdc_portal.util.DownloadWorkerKt
import com.jerson.hcdc_portal.util.isConnected
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SplashKt : AppCompatActivity() {
    @Inject
    lateinit var pref: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }

        val map = hashMapOf<String,Any>()
        map["url"] = "https://raw.githubusercontent.com/Jerson2000/HCDC_Portal/assets/assets/rooms.json"
        map["fileName"] = "rooms.json"
        val data = Data.Builder()
            .putAll(map)
            .build()
        if(isConnected(appContext)){
            val eye = OneTimeWorkRequestBuilder<DownloadWorkerKt>().setInputData(data).build()
            WorkManager.getInstance(this).enqueue(eye)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(500L)
                keepSplash = false
                val destination = if (pref.getBooleanPreference(KEY_IS_LOGIN))
                    MainKt::class.java else LoginKt::class.java
                startActivity(
                    Intent(this@SplashKt, destination)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )

            }
        }
    }
}
