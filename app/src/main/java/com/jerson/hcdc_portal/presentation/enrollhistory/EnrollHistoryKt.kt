package com.jerson.hcdc_portal.presentation.enrollhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryKtBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnrollHistoryKt: Fragment() {
    private lateinit var binding:FragmentEnrollmentHistoryKtBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentEnrollmentHistoryKtBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}