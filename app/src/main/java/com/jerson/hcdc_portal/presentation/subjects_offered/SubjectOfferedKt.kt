package com.jerson.hcdc_portal.presentation.subjects_offered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.ActivitySubjectOfferedKtBinding
import com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel.SubjectOfferedViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectOfferedKt:AppCompatActivity() {
    private lateinit var binding:ActivitySubjectOfferedKtBinding
    private val subjectOfferedViewModel:SubjectOfferedViewModel by viewModels()
    private lateinit var loadingDialog:LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectOfferedKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Subject Offered"
        }

    }
    private fun fetchSubjectOffered(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

            }
        }
    }
}