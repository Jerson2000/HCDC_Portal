package com.jerson.hcdc_portal

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
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
    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}