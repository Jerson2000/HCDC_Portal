package com.jerson.hcdc_portal.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityMainKtBinding
import com.jerson.hcdc_portal.util.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainKt : AppCompatActivity() {
    private lateinit var binding: ActivityMainKtBinding
    @Inject
    lateinit var appPref: AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment
        binding.navbar.setupWithNavController(navHostFragment.navController)

    }


}