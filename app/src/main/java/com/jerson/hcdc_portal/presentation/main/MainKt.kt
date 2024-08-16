package com.jerson.hcdc_portal.presentation.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityMainKtBinding
import com.jerson.hcdc_portal.presentation.main.viewmodel.AppViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.downloadApk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainKt : AppCompatActivity() {
    private lateinit var binding: ActivityMainKtBinding
    private val appViewModel: AppViewModel by viewModels()
    @Inject
    lateinit var appPref: AppPreference
    private var title = ""
    private var msg = ""
    private var dialog:AlertDialog?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment
        binding.navbar.setupWithNavController(navHostFragment.navController)
        appViewModel.checkForUpdate()
        checkForUpdate{
            if(it.isNotEmpty()){
                println("LINK:-> $it")
                if(!appPref.getBooleanPreference(Constants.KEY_IS_SHOW_UPDATE_DIALOG)){
                    dialog = MaterialAlertDialogBuilder(this@MainKt)
                        .setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton("Update") { dialog, _ ->
//                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                            downloadApk(it)
                            Toast.makeText(this@MainKt,"Downloading...", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNeutralButton("Don't show again") { dialog, _ ->
                            appPref.setBooleanPreference(Constants.KEY_IS_SHOW_UPDATE_DIALOG,true)
                            dialog.dismiss()
                        }
                        .create()

                    if (dialog!!.isShowing) {
                        dialog!!.dismiss()
                        dialog!!.show()
                    } else {
                        dialog?.show()
                    }

                }
            }
        }


    }

    private fun checkForUpdate(callback:(String)->Unit){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.appUpdate.collect{
                    when(it){
                        is Resource.Success -> {
                            if(it.data.isNullOrEmpty()){
                                title = "App is up to date. No updates available."
                                msg = "Your current version is the latest, and you're running with the most recent security patches and features."
                                callback("")
                            }
                            else{
                                title = "New update available"
                                msg = "Download and install to get the latest features, security patches, and performance improvements."
                                callback(it.data)
                            }

                        }
                        else -> Unit
                    }
                }
            }
        }
    }


}