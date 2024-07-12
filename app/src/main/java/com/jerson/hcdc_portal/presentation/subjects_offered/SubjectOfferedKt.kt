package com.jerson.hcdc_portal.presentation.subjects_offered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.ActivitySubjectOfferedKtBinding
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.presentation.subjects_offered.adapter.SubjectOfferedAdapter
import com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel.SubjectOfferedViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectOfferedKt:AppCompatActivity() {
    private lateinit var binding:ActivitySubjectOfferedKtBinding
    private val subjectOfferedViewModel:SubjectOfferedViewModel by viewModels()
    private lateinit var loadingDialog:LoadingDialog
    private lateinit var adapter:SubjectOfferedAdapter
    private val list = mutableListOf<SubjectOffered>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectOfferedKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        adapter = SubjectOfferedAdapter(list)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Subject Offered"
        }

        binding.apply{
            recyclerView.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
            recyclerView.adapter = adapter
        }

        subjectOfferedViewModel.fetchSubjectOffered()
        fetchSubjectOffered()
        binding.header.toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

    }


    private fun fetchSubjectOffered(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                subjectOfferedViewModel.fetchSubjectOffered.collect{
                    when(it){
                        is Resource.Loading -> {
                            loadingDialog.show()
                        }
                        is Resource.Success ->{
                            it.data?.let {x->
                                list.addAll(x)
                            }
                            adapter.notifyDataSetChanged()
                        }
                        is Resource.Error ->{
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding.root, it1) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }



}