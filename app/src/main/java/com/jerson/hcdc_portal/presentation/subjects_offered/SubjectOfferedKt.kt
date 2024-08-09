package com.jerson.hcdc_portal.presentation.subjects_offered

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivitySubjectOfferedKtBinding
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.subjects_offered.adapter.SubjectOfferedAdapter
import com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel.SubjectOfferedViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectOfferedKt:AppCompatActivity() {
    private lateinit var binding:ActivitySubjectOfferedKtBinding
    private val subjectOfferedViewModel:SubjectOfferedViewModel by viewModels()
    private val loginViewModel:LoginViewModel by viewModels()
    private lateinit var loadingDialog:LoadingDialog
    private lateinit var adapter:SubjectOfferedAdapter
    private val list = mutableListOf<SubjectOffered>()
    private var subjectOfferedPage = 1
    private var totalPages = 148
    private var isSearch = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectOfferedKtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        adapter = SubjectOfferedAdapter(list){
            showOfferDetail(it)
        }


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

        subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
        fetchSubjectOffered()
        binding.header.toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int
                val totalItemCount: Int
                val firstVisibleItemPosition: Int
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                checkNotNull(layoutManager)
                visibleItemCount = layoutManager.childCount
                totalItemCount = layoutManager.itemCount
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!recyclerView.canScrollVertically(1)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        if (subjectOfferedPage <= totalPages && !isSearch) {
                            subjectOfferedPage++
                            subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
                        }
                    }
                }
            }
        })

        reLogonResponse()

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
                            loadingDialog.dismiss()
                            it.data?.let {x->
                                if(isSearch) list.clear()
                                list.addAll(x)
                            }
                            adapter.notifyDataSetChanged()
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

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog.show()
                        }

                        is Resource.Success -> {
                            subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
                        }

                        is Resource.Error -> {
                            it.message?.let{msg->
                                if(msg.contains("null")){
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

    private fun showOfferDetail(it:SubjectOffered){
        val details =
            "Offer No. : ${it.so_no}\nDescription: ${it.description}\nUnits: ${it.units}\nMaximum Students: ${it.maximum}\nEnrolled: ${it.enrolled}" +
                    "\nRemaining Slot: ${it.remaining_slot}\nS.Y Semester: ${it.sysem}\nCourse: ${it.course}"

        MaterialAlertDialogBuilder(this)
            .setTitle(it.subject_code)
            .setMessage(details).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.isIconified = false
        searchView.queryHint = "Search subjects.."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                hideKeyboard()
                subjectOfferedViewModel.searchSubjectOffered(query)
                isSearch = true
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }



}