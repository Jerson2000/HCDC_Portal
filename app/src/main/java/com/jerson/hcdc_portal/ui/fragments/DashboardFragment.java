package com.jerson.hcdc_portal.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.FragmentDashboardBinding;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    FragmentDashboardBinding binding;
    DashboardAdapter adapter;
    List<DashboardModel> dashList = new ArrayList<>();
    DashboardViewModel viewModel;

    private static final String TAG = "DashboardFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DashboardAdapter(getActivity(), dashList);
        binding.recyclerView.setAdapter(adapter);


        if (dashList.size() == 0) {
            getData();
            getDataRes();
        }

        binding.retryLayout.retryBtn.setOnClickListener(v->{
            retry();
        });


        return binding.getRoot();
    }

    void getData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getDashboardData().observeForever(data -> {
            if (data != null) {
                dashList.clear();
                dashList.addAll(data);
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.retryLayout.getRoot().setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    void getDataRes() {
        viewModel.getDashboardResponse().observeForever(res -> {
            Log.d(TAG, "getDataRes: " + res);
            if(res.contains("timeout")) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.GONE);
                binding.retryLayout.getRoot().setBackgroundColor(Color.WHITE);
                binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
                binding.retryLayout.retryBtn.setEnabled(true);

            }
        });
    }

    void retry(){
        viewModel.getDashboardData();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.retryLayout.retryBtn.setEnabled(false);

    }


    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}