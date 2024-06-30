package com.jerson.hcdc_portal.presentation.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.FragmentAccountDetailBinding
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.presentation.accounts.adapter.AccountAdapter
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.getParcelableArrayListCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountDetails: Fragment() {
    private lateinit var binding:FragmentAccountDetailBinding
    private val list = mutableListOf<Account>()
    private lateinit var adapter:AccountAdapter
    private var loadingDialog:LoadingDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentAccountDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = context?.let { LoadingDialog(it) }
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.header.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Accountabilities"
        }

        adapter = AccountAdapter(list)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            recyclerView.adapter = adapter
        }

        setFragmentResultListener("requestKey") { _, bundle ->
            bundle.getParcelableArrayListCompat<Account>("objectList")?.let { objectList ->
                list.addAll(objectList)
                adapter.notifyDataSetChanged()
                binding.header.collapsingToolbar.subtitle = list[0].term
            }
        }

        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }




    }


}