package com.jerson.hcdc_portal.presentation.lacking

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.ActivityLackingBinding
import com.jerson.hcdc_portal.presentation.evaluation.viewmodel.EvaluationViewModel
import com.jerson.hcdc_portal.presentation.lacking.adapter.LackingAdapter
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Lacking : AppCompatActivity() {
    private lateinit var binding: ActivityLackingBinding
    private val lackingViewModel: EvaluationViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: LackingAdapter
    private val list = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Lacking Credentials"
        }



        adapter = LackingAdapter(list)

        binding.recyclcerView.adapter = adapter
        binding.recyclcerView.layoutManager = LinearLayoutManager(this)

        lackingViewModel.fetchLacking()

        fetchLacking()
        reLogonResponse()

        binding.header.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun fetchLacking() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                lackingViewModel.fetchLacking.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog.show()
                        }

                        is Resource.Success -> {
                            loadingDialog.dismiss()
                            if (it.data!!.lowercase().contains("complied")) {
                                val tv = TextView(this@Lacking)
                                tv.text = it.data
                                tv.textSize = 20F
                                binding.layout.addView(tv)
                                binding.recyclcerView.visibility = View.GONE
                            } else {
                                list.clear()
                                list.addAll(it.data.split(":"))
                                adapter.notifyDataSetChanged()
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let { msg ->
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

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog.show()
                        }

                        is Resource.Success -> {
                            lackingViewModel.fetchLacking()
                        }

                        is Resource.Error -> {
                            it.message?.let { msg ->
                                if (!msg.contains("null")) {
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