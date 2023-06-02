package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.FragmentAccountBinding;
import com.jerson.hcdc_portal.databinding.FragmentGradesBinding;
import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.ui.adapter.AccountAdapter;
import com.jerson.hcdc_portal.ui.adapter.GradeAdapter;
import com.jerson.hcdc_portal.viewmodel.AccountViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    private FragmentAccountBinding binding;
       private AccountViewModel viewModel;
    private AccountAdapter adapter;
    private List<AccountModel> accList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        binding.accountRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AccountAdapter(getActivity(), accList);
        binding.accountRecyclerView.setAdapter(adapter);


        getData();
        getResponse();


        return binding.getRoot();
    }


    void getData(){
        viewModel.getData().observe(requireActivity(),data->{
            if(data!=null){
                accList.clear();
                accList.addAll(data);
                adapter.notifyDataSetChanged();
                binding.dueText.setText(accList.get(0).getDueText());
                binding.dueAmount.setText(accList.get(0).getDue());
                binding.progressBar.setVisibility(View.GONE);
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.accountRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    void getResponse(){
        viewModel.getResponse().observe(requireActivity(),res->{
            Log.d(TAG, "getResponse: "+res);
        });

        viewModel.getResCode().observe(requireActivity(),code->{
            Log.d(TAG, "getResponse: "+code);
        });
    }


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}