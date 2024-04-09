package com.jerson.hcdc_portal.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.jerson.hcdc_portal.databinding.ActivitySplashBinding
import com.jerson.hcdc_portal.presentation.login.LoginKt
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_IS_LOGIN
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashKt:AppCompatActivity() {
    private lateinit var binding:ActivitySplashBinding

    @Inject
    lateinit var pref: AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper())
            .postDelayed({
                if(pref.getBooleanPreference(KEY_IS_LOGIN)){
                    startActivity(Intent(this, MainKt::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }else{
                    startActivity(Intent(this, LoginKt::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }, 1500)



    }


}