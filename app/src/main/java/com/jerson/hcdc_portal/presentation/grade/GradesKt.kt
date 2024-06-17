package com.jerson.hcdc_portal.presentation.grade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.FragmentGradesKtBinding
import com.jerson.hcdc_portal.presentation.grade.viewmodel.GradeViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.launch

@AndroidEntryPoint
class GradesKt : Fragment() {
    private lateinit var binding: FragmentGradesKtBinding
    private val gradesViewModel: GradeViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
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
        gradesViewModel.fetchGrades()
        fetchGrades()
        reLogonResponse()

    }

    private fun fetchGrades() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchGrades.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            /* Log.e("Grades", "fetchGrades: ${it.data!!.size}\t${it.data[0].term} - ${it.data[0].teacher}")*/
                        }

                        is Resource.Error -> {
                            if (it.message!!.equals("session end", true))
                                loginViewModel.reLogon()
                            else
                                SnackBarKt.snackBarLong(binding.root,it.message)
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

                        }

                        is Resource.Success -> {
                            gradesViewModel.fetchGrades()
                        }

                        is Resource.Error -> {
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}