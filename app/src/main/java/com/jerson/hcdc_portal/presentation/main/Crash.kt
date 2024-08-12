package com.jerson.hcdc_portal.presentation.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.databinding.ActivityCrashBinding
import com.jerson.hcdc_portal.presentation.main.viewmodel.AppViewModel
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Crash : AppCompatActivity() {
    private lateinit var binding: ActivityCrashBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val err = intent.extras?.getString("err")
        err?.let {
            binding.crash.text = err
            val report = "${getDevice()}\nError:-> ${err.take(650)}"
            appViewModel.postReport(report)
        }

        report {
            restartApp()
        }


    }

    private fun restartApp() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val packageManager: PackageManager = appContext.packageManager
                val intent = packageManager.getLaunchIntentForPackage(appContext.packageName)
                val componentName = intent!!.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                mainIntent.setPackage(appContext.packageName)
                appContext.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        }

    }

    private fun getDevice(): String = """
        OS Version: ${System.getProperty("os.version")}
        Device: ${Build.DEVICE}
        Model: ${Build.MODEL}
        Product & Brand: ${Build.PRODUCT} - ${Build.BRAND}
        Android Version: ${Build.VERSION.RELEASE}
        SDK Int: ${Build.VERSION.SDK_INT}
        Manufacturer: ${Build.MANUFACTURER}
        Display: ${Build.DISPLAY}     
    """.trimIndent()

    private fun report(isDone: (Boolean) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.postReport.collect {
                    when (it) {
                        is Resource.Success -> {
                            delay(15000)
                            isDone(true)
                        }

                        else -> isDone(false)
                    }
                }
            }
        }
    }
}