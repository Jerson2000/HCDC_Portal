package com.jerson.hcdc_portal.presentation.grade

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.FragmentGradesKtBinding
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.presentation.grade.adapter.GradeAdapter
import com.jerson.hcdc_portal.presentation.grade.viewmodel.GradeViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.TermSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GradesKt : Fragment() {
    private lateinit var binding: FragmentGradesKtBinding
    private val gradesViewModel: GradeViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val list = mutableListOf<Grade>()
    private var loadingDialog: LoadingDialog? = null
    private var termDialog: TermSelectionDialog? = null
    private lateinit var adapter:GradeAdapter
    private var selectedTerm:Term?=null

    @Inject
    lateinit var pref:AppPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGradesKtBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }

        adapter = GradeAdapter(list)
        binding.apply {
            recyclcerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            recyclcerView.adapter = adapter
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (isLoaded) gradesViewModel.getGrades(pref.getIntPreference(Constants.KEY_SELECT_GRADE_TERM))
        fetchGrades()
        reLogonResponse()
        gradesViewModel.getGradeTerm()
        getTerms()

        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog {term ->
                selectedTerm = term
                gradesViewModel.hasData(term) {
                    pref.setIntPreference(Constants.KEY_SELECT_GRADE_TERM,term.id)
                    if (!it) {
                        gradesViewModel.fetchGrades(term)
                    }else{
                        gradesViewModel.getGrades(term.id)
                    }
                }
            }
        }

    }

    private fun fetchGrades() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchGrades.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            loadingDialog?.dismiss()
                            if(it.data!!.isNotEmpty()){
                                binding.apply {
                                    tvTerm.text = it.data[0].term
                                    tvUnits.text = it.data[0].earnedUnits
                                    tvGWA.text = it.data[0].weightedAve
                                }
                                list.clear()
                                list.addAll(it.data)
                                adapter.notifyDataSetChanged()
                            }else{
                                binding.apply {
                                    tvTerm.text = "Select term"
                                    tvUnits.text = "0"
                                    tvGWA.text = "0.0"
                                }
                            }
                        }

                        is Resource.Error -> {
                            if (it.message!!.contains("session end", true))
                                loginViewModel.reLogon()
                            else{
                                loadingDialog?.dismiss()
                                SnackBarKt.snackBarLong(binding.root,it.message)
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
    
    private fun reLogonResponse(){
        lifecycleScope.launch { 
            repeatOnLifecycle(Lifecycle.State.STARTED){
                loginViewModel.login.collect{
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            selectedTerm?.let { term -> gradesViewModel.fetchGrades(term) }
                        }

                        is Resource.Error -> {
                            loadingDialog?.dismiss()
                            if(!it.message!!.contains("null"))
                                SnackBarKt.snackBarLong(binding.root, it.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
    private fun getTerms() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchTerms.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            loadingDialog?.dismiss()
                            it.data?.let { it1 -> termDialog?.setTerms(it1) }

                        }

                        is Resource.Error -> {
                            loadingDialog?.dismiss()
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}