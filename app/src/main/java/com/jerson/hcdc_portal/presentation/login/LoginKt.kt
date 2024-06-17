package com.jerson.hcdc_portal.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityLoginKtBinding
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_EMAIL
import com.jerson.hcdc_portal.util.Constants.KEY_IS_LOGIN
import com.jerson.hcdc_portal.util.Constants.KEY_PASSWORD
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt.snackBarLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginKt:AppCompatActivity(R.layout.activity_login_kt) {
    private lateinit var binding:ActivityLoginKtBinding
    private val viewModel: LoginViewModel by viewModels()
    private var loadToken:Int = 0

    @Inject
    lateinit var pref:AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginKtBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginBtn.setOnClickListener {
            if(checkFields()) viewModel.login(binding.emailET.text.toString().trim(),binding.passET.text.toString().trim())
        }

        viewModel.checkSession()
        login()
        checkSession()

    }

    private fun checkFields():Boolean{
        return !(binding.emailET.text.toString().isEmpty() || binding.passET.text.toString().isEmpty())
    }
    private fun login(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.login.collect{
                    when(it){
                        is Resource.Loading->{
                            binding.loginBtn.isEnabled = false
                        }
                        is Resource.Success ->{
                            viewModel.checkSession()
                        }

                        is Resource.Error->{
                            binding.loginBtn.isEnabled = true
                            snackBarLong(binding.root,it.message.toString())
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun checkSession(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.session.collect{
                    when(it){
                        is Resource.Loading->{
                            Log.e("HUHU", "checkSession: Loading..." )
                        }
                        is Resource.Success ->{
                            loadToken++
                            if(loadToken > 1){
                                if(it.data!!){
                                    snackBarLong(binding.root,"Incorrect Credentials")
                                    binding.loginBtn.isEnabled = true
                                }else{
                                    pref.setStringPreference(KEY_EMAIL,binding.emailET.text.toString())
                                    pref.setStringPreference(KEY_PASSWORD,binding.passET.text.toString())
                                    pref.setBooleanPreference(KEY_IS_LOGIN,true)
                                    startActivity(Intent(applicationContext, MainKt::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }

                        }
                        is Resource.Error->{
                            snackBarLong(binding.root,it.message.toString())
                        }
                        else -> Unit
                    }
                }
            }
        }
    }



}