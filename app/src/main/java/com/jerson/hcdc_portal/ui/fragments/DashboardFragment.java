package com.jerson.hcdc_portal.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.FragmentDashboardBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.activity.EvaluationActivity;
import com.jerson.hcdc_portal.ui.activity.SettingsActivity;
import com.jerson.hcdc_portal.ui.activity.SubjectDetailActivity;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.BaseFragment;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.DownloadWorker;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DashboardFragment extends BaseFragment<FragmentDashboardBinding> implements OnClickListener<DashboardModel> {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = getBinding();
        if (!getBindingNull()) init();
    }

    void init() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new DashboardAdapter(requireActivity(), todayList, this::onItemClick);
        binding.recyclerView.setAdapter(adapter);

        if (!preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE).equals("")) {
            binding.enrollAnnounceLayout.setVisibility(View.VISIBLE);
            binding.enrollAnnounce.setText(preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE));
        } else {
            binding.enrollAnnounceLayout.setVisibility(View.GONE);
        }
        binding.enrolledTV.setText(preferenceManager.getString(PortalApp.KEY_IS_ENROLLED));
        binding.unitsTV.setText(preferenceManager.getString(PortalApp.KEY_STUDENTS_UNITS));

        String pDetails = "ID number: " + preferenceManager.getString(PortalApp.KEY_STUDENT_ID) + "\n" +
                "Name: " + preferenceManager.getString(PortalApp.KEY_STUDENT_NAME) + "\n" +
                "Course: " + preferenceManager.getString(PortalApp.KEY_STUDENT_COURSE);
        binding.btnProfile.setOnClickListener(v -> {
            Dialog.Dialog("Student Info.", pDetails, requireActivity()).show();
        });
        binding.btnSetting.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
        });

        binding.evaluation.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EvaluationActivity.class));
        });

        binding.evaluationIV.setImage(ImageSource.resource(R.drawable.paper));
        binding.enrollAnnounceLayout.setOnClickListener(v -> {
            Data inputData = new Data.Builder()
                    .putString("url", "https://raw.githubusercontent.com/Jerson2000/jerson2000/portal_assets/room.json")
                    .putString("fileName", "test.json")
                    .build();

            // Create a OneTimeWorkRequest for the DownloadWorker
            OneTimeWorkRequest downloadRequest =
                    new OneTimeWorkRequest.Builder(DownloadWorker.class)
                            .setInputData(inputData)
                            .build();

            // Enqueue the request with WorkManager
            WorkManager.getInstance(requireActivity()).enqueue(downloadRequest);
        });


    }


    @SuppressLint("SimpleDateFormat")
    String getDay() {
        String day;
        if (new SimpleDateFormat("EEE").format(new Date()).equals("Thu")) {
            day = "Th";
        } else if (new SimpleDateFormat("EEE").format(new Date()).equals("Sun")) {
            day = "Su";
        } else {
            day = String.valueOf(new SimpleDateFormat("EEE").format(new Date()).charAt(0));
        }


        return day;
    }

    void getSubjectToday() {
        if (dashList.size() > 0) {
            for (DashboardModel s : dashList) {
                if (s.getDays().contains(getDay())) {
                    todayList.add(s);
                } else {

                }
            }
            if (todayList.size() > 0) adapter.notifyDataSetChanged();
            else
                errShow("No subject/s for today.");
        } else
            errShow("No subject/s for today.");

    }


    void getTotalSubject() {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < dashList.size(); i++) {
            if (!list.contains(dashList.get(i).getOfferNo())) {
                list.add(dashList.get(i).getOfferNo());
            }
        }

        binding.totalSubTV.setText(String.valueOf(list.size()));
    }


    DynamicListener<Boolean> isRetrieved = object -> {
        if (object) {
            getSubjectToday();
            getTotalSubject();
        } else {
            errShow("No subject/s for today.");
        }

    };


    private void loadDashboard(DynamicListener<Boolean> isRetrieved) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (data.size() > 0) {
                        dashList.clear();
                        dashList.addAll(data);
                        isRetrieved.dynamicListener(true);
                        isLoading(false);

                    } else {
                        isRetrieved.dynamicListener(false);
                    }
                }, throwable -> {
                    isRetrieved.dynamicListener(false);
                }));

    }

    void errShow(String msg) {
        Random random = new Random();
        int n = random.nextInt(6);
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.errLayout.setVisibility(View.VISIBLE);
        binding.errText.setText(msg);
        binding.errEmoji.setText(PortalApp.HAPPY_EMOJIS[n]);
    }

    void isLoading(boolean loading) {
        if (loading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }


    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onItemClick(DashboardModel object) {
        Intent intent = new Intent(requireActivity(), SubjectDetailActivity.class);
        intent.putExtra("subject", object);
        startActivity(intent);
    }

    @Override
    protected FragmentDashboardBinding onCreateViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
        return FragmentDashboardBinding.inflate(layoutInflater, container, false);
    }
}