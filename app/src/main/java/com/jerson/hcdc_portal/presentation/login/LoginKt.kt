package com.jerson.hcdc_portal.presentation.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityLoginKtBinding
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_EMAIL
import com.jerson.hcdc_portal.util.Constants.KEY_IS_LOGIN
import com.jerson.hcdc_portal.util.Constants.KEY_PASSWORD
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.SnackBarKt.snackBarLong
import com.jerson.hcdc_portal.util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
import com.jerson.hcdc_portal.util.checkAndRequestPermissions
import com.jerson.hcdc_portal.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginKt : AppCompatActivity(R.layout.activity_login_kt) {
    private lateinit var binding: ActivityLoginKtBinding
    private val viewModel: LoginViewModel by viewModels()
    private var loadToken: Int = 0
    private var loadingDialog: LoadingDialog? = null

    @Inject
    lateinit var pref: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logo.load(R.drawable.logo)
        loadingDialog = LoadingDialog(this)


        binding.loginBtn.setOnClickListener {
            if (checkFields()) viewModel.login(
                binding.emailET.text.toString().trim(),
                binding.passET.text.toString().trim()
            )
            loadToken+=2
            binding.passET.clearFocus()
            binding.emailET.clearFocus()
            hideKeyboard()
        }

        viewModel.checkSession()
        login()
        checkSession()
        checkAndRequestPermissions()
    }

    private fun checkFields(): Boolean {
        return !(binding.emailET.text.toString().isEmpty() || binding.passET.text.toString()
            .isEmpty())
    }

    private fun login() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.loginBtn.isEnabled = false
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            viewModel.checkSession()
                        }

                        is Resource.Error -> {
                            if (!it.message.equals("null")) {
                                loadingDialog!!.dismiss()
                                binding.loginBtn.isEnabled = true
                                snackBarLong(binding.root, it.message.toString())
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun checkSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.session.collect {
                    when (it) {
                        is Resource.Loading -> {
                        }

                        is Resource.Success -> {
                            if (loadToken > 1) {
                                if (it.data!!) {
                                    snackBarLong(binding.root, "Incorrect Credentials")
                                    binding.loginBtn.isEnabled = true
                                    loadingDialog!!.dismiss()
                                } else {
                                    loadingDialog!!.dismiss()
                                    pref.setStringPreference(
                                        KEY_EMAIL,
                                        binding.emailET.text.toString()
                                    )
                                    pref.setStringPreference(
                                        KEY_PASSWORD,
                                        binding.passET.text.toString()
                                    )
                                    pref.setBooleanPreference(KEY_IS_LOGIN, true)
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            MainKt::class.java
                                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }

                        }

                        is Resource.Error -> {
                            if (!it.message.equals("null")) {
                                loadingDialog!!.dismiss()
                                snackBarLong(binding.root, it.message.toString())
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                snackBarLong(binding.root,"Some features might not be able to work.")
            }
        }
    }


}