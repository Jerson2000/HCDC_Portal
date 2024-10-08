package com.jerson.hcdc_portal.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.databinding.ActivitySplashBinding
import com.jerson.hcdc_portal.presentation.login.LoginKt
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_IS_LOGIN
import com.jerson.hcdc_portal.util.DownloadWorkerKt
import com.jerson.hcdc_portal.util.isConnected
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashKt:AppCompatActivity() {
    private lateinit var binding:ActivitySplashBinding
    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var pref: AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        Handler(Looper.getMainLooper())
            .postDelayed({
                if(pref.getBooleanPreference(KEY_IS_LOGIN)){
                    loginViewModel.checkSession()
                    startActivity(Intent(this, MainKt::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }else{
                    startActivity(Intent(this, LoginKt::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }, 1500)

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



    }


}