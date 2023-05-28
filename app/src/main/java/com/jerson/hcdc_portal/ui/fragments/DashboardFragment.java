package com.jerson.hcdc_portal.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.databinding.FragmentDashboardBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DashboardFragment extends Fragment {
    FragmentDashboardBinding binding;
    DashboardAdapter adapter;
    List<DashboardModel> dashList = new ArrayList<>();
    List<DashboardModel> todayList = new ArrayList<>();
    DashboardViewModel viewModel;
    private int responseCode = 0;

    private static final String TAG = "DashboardFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        loadDashboard(isRetrieved);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);


        init();

        return binding.getRoot();
    }


    void init() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new DashboardAdapter(requireActivity(), todayList);
        binding.recyclerView.setAdapter(adapter);

    }


    @SuppressLint("SimpleDateFormat")
    String getDay() {
        String day;
        if (new SimpleDateFormat("EEE").format(new Date()).equals("Thu")) {
            day = "TH";
        } else if (new SimpleDateFormat("EEE").format(new Date()).equals("Sun")) {
            day = "SU";
        } else {
            day = String.valueOf(new SimpleDateFormat("EEE").format(new Date()).charAt(0));
        }
        return day.toUpperCase(Locale.ROOT);
    }


    void getSubjectToday() {
        if (dashList.size() > 0) {
            for (DashboardModel s : dashList) {
                if (s.getDays().contains(getDay())) {
                    todayList.add(s);
                } else {

                }
            }
            if (todayList.size() > 0) {
                adapter.notifyDataSetChanged();
//                binding.progressBar.setVisibility(View.GONE);
//                binding.noSubject.setVisibility(View.GONE);
            } else {
//                binding.noSubject.setVisibility(View.VISIBLE);
//                binding.progressBar.setVisibility(View.GONE);
            }

        }

    }


    void getTotalSubject() {
        List<DashboardModel> distinctList = dashList.stream()
                .filter(distinctList(DashboardModel::getOfferNo))
                .collect(Collectors.toList());
        binding.totalSubTV.setText(""+distinctList.size());

    }

    public <T> Predicate<T> distinctList(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    DynamicListener<Boolean> isRetrieved = object -> {
        if (object) {
            getSubjectToday();
            getTotalSubject();
        }

    };




    /*void getData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getData().observe(requireActivity(), data -> {
            if (data != null) {
                try {
                    dashList.clear();
                    dashList.addAll(data);
                    adapter.notifyDataSetChanged();

                    deleteData(object -> {

                        if(object){
                            saveData();
                        }

                    });


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

        });
    }*/

    /*
    void getResCode() {
        if (responseCode != 200) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.retryLayout.getRoot().setBackgroundColor(Color.WHITE);
            binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
            binding.retryLayout.retryBtn.setEnabled(true);
        }
    }*/

    /*void saveData() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertDashboard(dashList)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "saveData: saved");
                }, throwable -> {
                    Log.e(TAG, "saveData: ", throwable);
                })
        );
    }*/

    /*void deleteData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteAll()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteData: ", throwable);
                })
        );
    }*/

    private void loadDashboard(DynamicListener<Boolean> isRetrieved) {
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
                        isRetrieved.dynamicListener(true);
//                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.recyclerView.setVisibility(View.VISIBLE);

                    } else {
                        if (data.size() == 0 && dashList.size() == 0) {
                            isRetrieved.dynamicListener(false);
//                            getData();
                        }
                    }
                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "Error retrieving data", throwable);
                    isRetrieved.dynamicListener(false);
                }));

    }


    public DashboardFragment() {
        // Required empty public constructor
    }


}