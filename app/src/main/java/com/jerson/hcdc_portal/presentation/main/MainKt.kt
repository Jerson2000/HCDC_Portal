package com.jerson.hcdc_portal.presentation.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityMainKtBinding
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_IS_SESSION
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainKt : AppCompatActivity() {
    private lateinit var binding: ActivityMainKtBinding
    private val loginViewModel: LoginViewModel by viewModels()
    @Inject
    lateinit var appPref: AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (appPref.getBooleanPreference(KEY_IS_SESSION))
            loginViewModel.reLogin()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment
        binding.navbar.setupWithNavController(navHostFragment.navController)

    }


}