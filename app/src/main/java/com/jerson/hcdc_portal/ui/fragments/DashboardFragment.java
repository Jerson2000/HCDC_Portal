package com.jerson.hcdc_portal.ui.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.FragmentDashboardBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.ui.activity.LoginActivity;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.PreferenceManager;
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
    PreferenceManager preferenceManager;

    private static final String TAG = "DashboardFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        preferenceManager = new PreferenceManager(requireActivity());

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

        if(!preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE).equals("")){
            binding.enrollAnnounceLayout.setVisibility(View.VISIBLE);
            binding.enrollAnnounce.setText(preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE));
        }
        binding.enrolledTV.setText(preferenceManager.getString(PortalApp.KEY_IS_ENROLLED));

        String pDetails ="ID number: " + preferenceManager.getString(PortalApp.KEY_STUDENT_ID)+"\n" +
                "Name: "+preferenceManager.getString(PortalApp.KEY_STUDENT_NAME).toLowerCase(Locale.ROOT)+"\n" +
                "Course: "+preferenceManager.getString(PortalApp.KEY_STUDENT_COURSE);
        binding.btnProfile.setOnClickListener(v->{
            Dialog.Dialog("Student Info.",pDetails,requireActivity()).show();
        });
        binding.btnLogout.setOnClickListener(v->{
            Dialog.Dialog("WARNING!","Are you sure you want to logout?",requireActivity())
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferenceManager.clear();
                            startActivity(new Intent(requireActivity(),LoginActivity.class));
                            requireActivity().finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        });


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
            if (todayList.size() > 0)  adapter.notifyDataSetChanged();

        }

    }


    void getTotalSubject() {
        List<DashboardModel> distinctList = dashList.stream()
                .filter(distinctList(DashboardModel::getOfferNo))
                .collect(Collectors.toList());
        binding.totalSubTV.setText(String.valueOf(distinctList.size()));

    }

    public <T> Predicate<T> distinctList(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    DynamicListener<Boolean> isRetrieved = object -> {
        if (object) {
            getSubjectToday();
            getTotalSubject();
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    };



    private void loadDashboard(DynamicListener<Boolean> isRetrieved) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    Log.d(TAG, "loadDashboard: " + data.size());
                    if (data.size() > 0) {
                        dashList.clear();
                        dashList.addAll(data);
                        isRetrieved.dynamicListener(true);
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.recyclerView.setVisibility(View.VISIBLE);

                    } else {
                        if (data.size() == 0 && dashList.size() == 0) {
                            isRetrieved.dynamicListener(false);
                        }
                    }
                }, throwable -> {
                    Log.e(TAG, "loadDashboard", throwable);
                    isRetrieved.dynamicListener(false);
                }));

    }


    public DashboardFragment() {
        // Required empty public constructor
    }


}