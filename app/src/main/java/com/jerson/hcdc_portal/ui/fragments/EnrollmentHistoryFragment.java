package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryBinding;
import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.model.EnrollLinksModel;
import com.jerson.hcdc_portal.ui.adapter.EnrollHistoryAdapter;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnrollmentHistoryFragment extends Fragment {
    private static final String TAG = "EnrollmentHistoryFragme";
    FragmentEnrollmentHistoryBinding binding;
    private EnrollHistoryViewModel viewModel;
    private List<EnrollLinksModel> periodLinks = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private List<EnrollHistModel> enrollData = new ArrayList<>();
    private EnrollHistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEnrollmentHistoryBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EnrollHistoryViewModel.class);

        // Material TextField Autocomplete - simply known as dropdown
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);

        binding.enrHistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EnrollHistoryAdapter(getActivity(), enrollData);
        binding.enrHistRecyclerView.setAdapter(adapter);


        getLinks();

        binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d(TAG, "onItemClick: " + periodLinks.get(i).getPeriodText() + " ()" + i);
            binding.progressBar.setVisibility(View.VISIBLE);
            getData(periodLinks.get(i).getPeriodLink());
            getResponse();
        });

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
            getLinks();
            binding.retryLayout.retryBtn.setEnabled(false);
        });

        return binding.getRoot();
    }

    void getResponse() {
        try {
            viewModel.getResponse().observe(getActivity(), res -> {
                if (res.toLowerCase(Locale.ROOT).contains("timeout") || res.toLowerCase(Locale.ROOT).contains("fetch")) {
                    binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.retryLayout.retryBtn.setEnabled(true);
                    binding.semSelectorLayout.setVisibility(View.GONE);
                    binding.enrHistRecyclerView.setVisibility(View.GONE);
                }

            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getResponse: " + e.getMessage());
        }

    }

    void getLinks() {
        try {
            viewModel.getLinks().observe(getActivity(), data -> {
                if (data != null) {
                    list.clear();
                    periodLinks.clear();
                    periodLinks.addAll(data);
                    for (EnrollLinksModel d : data) {
                        list.add(d.getPeriodText());
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    binding.semSelectorLayout.setVisibility(View.VISIBLE);
                    binding.enrHistRecyclerView.setVisibility(View.VISIBLE);
                    arrayAdapter.notifyDataSetChanged();
                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLinks: " + e.getMessage());
        }
    }

    void getData(String link) {
        try {
            viewModel.getData(link).observe(getActivity(), data -> {
                if (data != null) {
                    enrollData.clear();
                    enrollData.addAll(data);
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "getData: " + e.getStackTrace());
        }

    }

    public EnrollmentHistoryFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}