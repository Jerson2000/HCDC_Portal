package com.jerson.hcdc_portal

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.jerson.hcdc_portal.presentation.main.Crash
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import dagger.hilt.android.HiltAndroidApp
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application(), Configuration.Provider,LifecycleObserver {
    companion object {
        lateinit var appContext: Context
            private set
    }
    @Inject
    lateinit var pref:AppPreference

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        val themeMode = if(pref.getIntPreference(Constants.KEY_SETTINGS_THEME_MODE) == 0) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else pref.getIntPreference(Constants.KEY_SETTINGS_THEME_MODE)
        AppCompatDelegate.setDefaultNightMode(themeMode)


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val err = sw.toString()
            Log.e(this.toString(), "uncaughtException: ", e);
            startActivity(Intent(this,Crash::class.java).putExtra("err",err).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION))
            exitProcess(1)

        }

    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}