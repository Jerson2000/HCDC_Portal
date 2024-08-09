package com.jerson.hcdc_portal.presentation.evaluation

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.ActivityEvaluationKtBinding
import com.jerson.hcdc_portal.presentation.evaluation.viewmodel.EvaluationViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EvaluationKt:AppCompatActivity() {
    private lateinit var binding:ActivityEvaluationKtBinding
    private val evalViewModel:EvaluationViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var webView: WebView

    @Inject
    lateinit var pref:AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=  ActivityEvaluationKtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Evaluation"
        }

        binding.header.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        webView = binding.myWebView
        val isLoaded = pref.getBooleanPreference(Constants.KEY_HTML_EVALUATION_LOADED)
        if(!isLoaded) evalViewModel.fetchEvaluation()
        display2WebView(pref.getStringPreference(Constants.KEY_HTML_EVALUATION))
        fetchEvaluation()
        reLogonResponse()
    }

    private fun fetchEvaluation(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                evalViewModel.fetchEvaluation.collect{
                    when(it){
                        is Resource.Loading->{
                            loadingDialog.show()
                        }
                        is Resource.Success->{
                            loadingDialog.dismiss()
                            val evalHtml = "<html><body>${it.data}</body></html>"
//                            println("EvalHTML: $evalHtml")
                            webView.loadDataWithBaseURL(null,evalHtml, "text/html", "UTF-8", null)
                        }
                        is Resource.Error ->{
                            it.message?.let {msg->
                                if (msg.contains("session end", true))
                                    loginViewModel.reLogon()
                                else {
                                    loadingDialog.dismiss()
                                    SnackBarKt.snackBarLong(binding.root, it.message)
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun display2WebView(htmlData:String){
        val html = "<html><body>$htmlData</body></html>"
        webView.loadDataWithBaseURL(null,html, "text/html", "UTF-8", null)
    }

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog.show()
                        }

                        is Resource.Success -> {
                            evalViewModel.fetchEvaluation()
                        }

                        is Resource.Error -> {
                            it.message?.let{msg->
                                if(!msg.contains("null")){
                                    loadingDialog.dismiss()
                                    SnackBarKt.snackBarLong(binding.root, msg)
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}