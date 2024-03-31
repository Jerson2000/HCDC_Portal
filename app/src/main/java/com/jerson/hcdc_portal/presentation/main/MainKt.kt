package com.jerson.hcdc_portal.presentation.main

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityMainKtBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainKt : AppCompatActivity(R.layout.activity_main_kt) {
    private lateinit var binding: ActivityMainKtBinding
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment
        binding.navbar.setupWithNavController(navHostFragment.navController)

    }






}