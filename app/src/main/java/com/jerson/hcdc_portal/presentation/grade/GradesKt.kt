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
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
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
    private lateinit var adapter: GradeAdapter
    private var selectedTerm: Term? = null
    private var isRefresh = false

    @Inject
    lateinit var pref: AppPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGradesKtBinding.inflate(inflater)
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GradeAdapter(list)
        binding.apply {
            binding.backgroundIV.load(R.drawable.studying){
                size(300,300)
            }
            recyclcerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclcerView.adapter = adapter
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (isLoaded) gradesViewModel.getGrades(pref.getIntPreference(Constants.KEY_SELECT_GRADE_TERM))
        fetchGrades {
            if (it)
                gradesViewModel.getGradeTerm()
        }
        reLogonResponse()
        getTerms()

        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog { term ->
                selectedTerm = term
                gradesViewModel.hasData(term) {
                    pref.setIntPreference(Constants.KEY_SELECT_GRADE_TERM, term.id)
                    if (!it) {
                        gradesViewModel.fetchGrades(term)
                    } else {
                        gradesViewModel.getGrades(term.id)
                    }
                }
            }
        }
        binding.btnRefresh.setOnClickListener{
            context?.let { cntxt ->
                MaterialAlertDialogBuilder(cntxt)
                    .setTitle("Refresh")
                    .setMessage("Are you sure you want to refresh?")
                    .setPositiveButton("Yes"){dialog,_->
                        isRefresh = true
                        gradesViewModel.fetchGrades()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No"){dialog,_->
                        dialog.dismiss()
                    }.show()

            }
        }

    }

    private fun fetchGrades(isDone:(Boolean)->Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchGrades.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            loadingDialog!!.dismiss()
                            if (it.data!!.isNotEmpty()) {
                                binding.apply {
                                    tvTerm.text = it.data[0].term
                                    tvUnits.text = it.data[0].earnedUnits
                                    tvGWA.text = it.data[0].weightedAve
                                }
                                list.clear()
                                list.addAll(it.data)
                                adapter.notifyDataSetChanged()
                            } else {
                                binding.apply {
                                    tvTerm.text = "Select term"
                                    tvUnits.text = "0"
                                    tvGWA.text = "0.0"
                                    list.clear()
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            isDone(true)
                        }

                        is Resource.Error -> {
                            isDone(true)
                            it.message?.let {msg->
                                if (msg.contains("session end", true))
                                    loginViewModel.reLogon()
                                else {
                                    loadingDialog?.dismiss()
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
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            selectedTerm?.let { term -> gradesViewModel.fetchGrades(term) }
                            if(isRefresh){
                                gradesViewModel.fetchGrades()
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let{msg->
                                if(!msg.contains("null")){
                                    loadingDialog?.dismiss()
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

    private fun getTerms() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchTerms.collect {
                    when (it) {
                        is Resource.Loading -> {
                            /*loadingDialog!!.show()*/
                        }

                        is Resource.Success -> {
                            /*loadingDialog!!.dismiss()*/
                            it.data?.let { it1 ->
                                termDialog?.setTerms(it1)
                            }

                        }

                        is Resource.Error -> {
                            /*loadingDialog!!.dismiss()*/
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}