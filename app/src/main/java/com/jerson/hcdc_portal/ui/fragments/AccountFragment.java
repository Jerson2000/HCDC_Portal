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
    private List<AccountLinksModel> semAccountList = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private AccountViewModel viewModel;
    private AccountAdapter adapter;
    private List<AccountModel> accList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);

        // Material TextField Autocomplete - simply known as dropdown
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);

        binding.accountRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AccountAdapter(getActivity(), accList);
        binding.accountRecyclerView.setAdapter(adapter);

        /*getLinks();*/
        getData();

        /*binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d(TAG, "onItemClick: " + semAccountList.get(i).getSemAccountLink() + " ()" + i);
            if (i != 0) {
                binding.progressBar.setVisibility(View.VISIBLE);
                getData(semAccountList.get(i).getSemAccountLink());
//                getResponse();
            }
        });*/

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
            getLinks();
            binding.retryLayout.retryBtn.setEnabled(false);
        });


        return binding.getRoot();
    }

    void getLinks() {
        try {
            viewModel.getLinks().observe(requireActivity(), data -> {
                if (data != null) {
                    list.clear();
                    semAccountList.clear();
                    semAccountList.addAll(data);
                    for (AccountLinksModel d : data) {
                        list.add(d.getSemAccountText());
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    binding.semSelectorLayout.setVisibility(View.VISIBLE);
                    binding.accountRecyclerView.setVisibility(View.VISIBLE);
                    arrayAdapter.notifyDataSetChanged();
                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLinks: " + e.getMessage());
        }

    }

    void getResponse() {
        try {
            viewModel.getResponse().observe(getActivity(), res -> {
                if (res.toLowerCase(Locale.ROOT).contains("timeout") || res.toLowerCase(Locale.ROOT).contains("fetch")) {
                    binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.retryLayout.retryBtn.setEnabled(true);
                    binding.semSelectorLayout.setVisibility(View.GONE);
                    binding.accountRecyclerView.setVisibility(View.GONE);
                }

            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getResponse: " + e.getMessage());
        }

    }

    void getData(String link) {
        try {
            viewModel.getData(link).observe(requireActivity(), data -> {
                if (data != null) {
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
        } catch (NullPointerException e) {
            Log.d(TAG, "getData: " + e.getMessage());
        }

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


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}