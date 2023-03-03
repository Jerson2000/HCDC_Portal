package com.jerson.hcdc_portal.ui.fragments;

import android.content.Context;
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
import java.util.concurrent.ExecutionException;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class DashboardFragment extends Fragment {

    FragmentDashboardBinding binding;
    DashboardAdapter adapter;
    List<DashboardModel> dashList = new ArrayList<>();
    DashboardViewModel viewModel;
    List<DashboardModel> roomDashList = new ArrayList<>();

    private static final String TAG = "DashboardFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DashboardAdapter(getActivity(), dashList);
        binding.recyclerView.setAdapter(adapter);


        getData(getActivity());
//        getDataRes();

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
//            retry();
        });


        return binding.getRoot();
    }

    void getData(Context context) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getData(context);
        viewModel.dataS().observe(getActivity(), data -> {
            if (data != null) {
                try {
                    dashList.clear();
                    dashList.addAll(data);
                    adapter.notifyDataSetChanged();
                    saveToDatabase();

                    if (binding.progressBar.getVisibility() == View.VISIBLE) {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        return;
                    }

                    binding.retryLayout.getRoot().setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                } catch (NullPointerException e) {
                    Log.d(TAG, "getData: " + e.getMessage());
                }

            }
        });
    }

    void getDataRes() {
        viewModel.getDashboardResponse().observe(getActivity(), res -> {
            Log.d(TAG, "getDataRes: " + res);
            if (res.contains("timeout") || res.contains("error fetching")) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.retryLayout.getRoot().setBackgroundColor(Color.WHITE);
                binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
                binding.retryLayout.retryBtn.setEnabled(true);

            }
        });
    }

    void retry() {
        try {
            viewModel.getDashboardData();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.retryLayout.retryBtn.setEnabled(false);

        } catch (NullPointerException e) {
            Log.d(TAG, "retry: " + e.getMessage());
        }


    }

    void saveToDatabase(){
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertDashboard(dashList)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Data saved: " + dashList.size());
                },throwable -> {
                    Log.d(TAG, "getData: "+throwable);
                })
        );
    }

    private void loadWatchList() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d("TAG", "Data retrieved successfully: " + data.toString());
                }, throwable -> {
                    // Handle the error
                    Log.e("TAG", "Error retrieving data", throwable);
                }));

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