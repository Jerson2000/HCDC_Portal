package com.jerson.hcdc_portal.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Call;

public class DashboardFragment extends Fragment {
    FragmentDashboardBinding binding;
    DashboardAdapter adapter;
    List<DashboardModel> dashList = new ArrayList<>();
    DashboardViewModel viewModel;
    private int responseCode = 0;

    private static final String TAG = "DashboardFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

//        loadDatabaseDATA();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new DashboardAdapter(requireActivity(), dashList);
        binding.recyclerView.setAdapter(adapter);

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
        });
        loadData(requireActivity());
        return binding.getRoot();
    }

    void loadData(Context context) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getData(context).observe(requireActivity(), data -> {
            if (data != null) {
                try {
                    dashList.clear();
                    dashList.addAll(data);
                    adapter.notifyDataSetChanged();
//                    tableDelete();
//                    saveToDatabase();

                    if (binding.progressBar.getVisibility() == View.VISIBLE) {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        return;
                    }

                    binding.retryLayout.getRoot().setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                } catch (NullPointerException e) {
                    Log.d(TAG, "loadData: " + e.getMessage());
                }

            }
            getResCode();
        });
    }

    void getResCode() {
        if (responseCode != 200) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.retryLayout.getRoot().setBackgroundColor(Color.WHITE);
            binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
            binding.retryLayout.retryBtn.setEnabled(true);
        }
    }

    void saveToDatabase() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertDashboard(dashList)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Data saved: " + dashList.size());
                }, throwable -> {
                    Log.d(TAG, "getData: " + throwable);
                })
        );
    }

    void tableDelete() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteAll()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Data deleted: Success");
                }, throwable -> {
                    Log.d(TAG, "Data deleted:: " + throwable);
                })
        );
    }

    private void loadDatabaseDATA() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d(TAG, "Data retrieved successfully: " + data.size());
                    if (data.size() > 0) {
                        dashList.clear();
                        dashList.addAll(data);
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.recyclerView.setVisibility(View.VISIBLE);

                    } else {
                        loadData(getActivity());
                    }
                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "Error retrieving data", throwable);
                }));

    }


    public DashboardFragment() {
        // Required empty public constructor
    }


}