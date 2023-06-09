package com.jerson.hcdc_portal.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.FragmentSubjectsBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.activity.SubjectDetailActivity;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubjectsFragment extends Fragment implements OnClickListener<DashboardModel> {
    private static final String TAG = "SubjectsFragment";
    private FragmentSubjectsBinding binding;
    private List<DashboardModel> subjectList = new ArrayList<>();
    private DashboardAdapter adapter;
    private DashboardViewModel viewModel;
    private LoginViewModel loginViewModel;
    private PreferenceManager preferenceManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        preferenceManager = new PreferenceManager(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubjectsBinding.inflate(inflater, container, false);

        init();

        return binding.getRoot();
    }


    void init() {
        binding.subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new DashboardAdapter(requireActivity(), subjectList,this::onItemClick);
        binding.subjectsRecyclerView.setAdapter(adapter);

        loadSubject();
        observeRequest();


        binding.refreshLayout.setOnRefreshListener(() -> {
            binding.refreshLayout.setRefreshing(true);
            if (PortalApp.isConnected()) {
                checkSession(object -> {
                    if (object) {
                        getSubjects();
                    }
                });
            } else {
                Toast.makeText(requireActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                binding.refreshLayout.setRefreshing(false);
            }
        });


    }

    void checkSession(DynamicListener<Boolean> listener) {
        loginViewModel.checkSession(object -> {
            if (object) {
                loginViewModel.Login(preferenceManager.getString(PortalApp.KEY_EMAIL), preferenceManager.getString(PortalApp.KEY_PASSWORD)).observe(requireActivity(), data -> {
                    if (data.toLowerCase(Locale.ROOT).contains("logged")) {
                        checkSession(listener);
                    }
                });

            } else listener.dynamicListener(true);

        });

    }

    void getSubjects() {
        viewModel.getData().observe(requireActivity(), data -> {
            if (data != null) {
                deleteSubjects(object -> {
                    if (object) {
                        saveSubjects(data, object1 -> binding.refreshLayout.setRefreshing(false));
                    }
                });
            }
        });
    }

    void observeRequest() {
        viewModel.getErr().observe(requireActivity(),err->{
            showErr(err.getMessage());
        });

        loginViewModel.getErr().observe(requireActivity(),err->{
            showErr(err.getMessage());
        });
    }

    void showErr(String err) {
        Random random = new Random();
        int n = random.nextInt(6);
        binding.errLayout.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.subjectsRecyclerView.setVisibility(View.GONE);
        binding.errText.setText(err);
        binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
    }

    void isLoading(boolean loading){
        if(loading){
            binding.subjectsRecyclerView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.errLayout.setVisibility(View.GONE);
        }else{
            binding.subjectsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            binding.errLayout.setVisibility(View.GONE);
        }
    }



    /* database */
    private void loadSubject() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {

                    if (data.size() > 0) {
                        subjectList.clear();
                        subjectList.addAll(data);
                        adapter.notifyDataSetChanged();
                        isLoading(false);

                    } else {
                        showErr("No subjects");
                    }
                   /* Log.d(TAG, "loadSubject: " + data.size());*/
                }, throwable -> {
                    Log.e(TAG, "loadSubject", throwable);
                    showErr(throwable.getMessage());
                }));

    }

    void saveSubjects(List<DashboardModel> data, DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertDashboard(data)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.d(TAG, "saveSubjects: saved " + data.size());*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "saveSubjects: ", throwable);
                    showErr(throwable.getMessage());
                    listener.dynamicListener(false);
                })
        );

    }

    void deleteSubjects(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteDashboardData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteSubjects: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    showErr(throwable.getMessage());
                    Log.e(TAG, "deleteSubjects: ", throwable);
                })
        );
    }


    public SubjectsFragment() {
    }

    @Override
    public void onItemClick(DashboardModel object) {
        Intent intent = new Intent(requireActivity(), SubjectDetailActivity.class);
        intent.putExtra("subject",object);
        startActivity(intent);

    }
}