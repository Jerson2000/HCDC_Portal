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
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.ui.adapter.EnrollHistoryAdapter;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class EnrollmentHistoryFragment extends Fragment {
    private static final String TAG = "EnrollmentHistoryFragme";
    FragmentEnrollmentHistoryBinding binding;
    private EnrollHistoryViewModel viewModel;
    private List<EnrollHistModel.Link> periodLinks = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private List<EnrollHistModel> enrollData = new ArrayList<>();
    private EnrollHistoryAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EnrollHistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
            getData(periodLinks.get(i).getPeriodLink(),periodLinks.get(i).getId());
//            getResponse();

        });

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
            getLinks();
            binding.retryLayout.retryBtn.setEnabled(false);
        });
//        loadEnrollHistory();
        loadEnrollHistoryLink();

        return binding.getRoot();
    }

    void getResponse() {
        try {
            viewModel.getResponse().observe(requireActivity(), res -> {
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
            viewModel.getLinks().observe(requireActivity(), data -> {
                if (data != null) {

                    list.clear();
                    periodLinks.clear();
                    periodLinks.addAll(data);
                    for (EnrollHistModel.Link d : data) {
                        list.add(d.getPeriodText());
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    binding.semSelectorLayout.setVisibility(View.VISIBLE);
                    binding.enrHistRecyclerView.setVisibility(View.VISIBLE);
                    arrayAdapter.notifyDataSetChanged();
//                    saveEnrollHistoryLink();
                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLinks: " + e.getMessage());
        }
    }


    void saveEnrollHistory(int link_id) {
        enrollData.get(0).setLink_id(link_id);
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistory(enrollData)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.w(TAG, "saveEnrollHistory Data saved: " + enrollData.size());
                }, throwable -> {
                    Log.d(TAG, "getData: " + throwable);
                })
        );
    }

    void saveEnrollHistoryLink(){
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistoryLink(periodLinks)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.w(TAG, "saveEnrollHistoryLink Data saved: " + periodLinks.size());
                }, throwable -> {
                    Log.d(TAG, "getData: " + throwable);
                })
        );
    }

    private void loadEnrollHistory() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d(TAG, "Data retrieved successfully: " + data.size());
                    List<EnrollHistModel> s = data;
                    System.out.println(s.get(0).getLink_id() +" ~~~~~"+s.get(0).getSubjCode());
                    System.out.println(s.get(1).getLink_id() +" ~~~~~"+s.get(1).getSubjCode());
                    System.out.println(s.get(2).getLink_id() +" ~~~~~"+s.get(2).getSubjCode());


                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "Error retrieving data", throwable);
                }));

    }
    private void loadEnrollHistoryLink() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistoryLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d(TAG, "Data retrieved successfully: " + data.size());
                    List<EnrollHistModel.Link> s = data;
                    System.out.println(s.get(0).getId() + "  ~~~~~~~~~~~   "+s.get(0).getPeriodText());
                    System.out.println(s.get(1).getId() + "  ~~~~~~~~~~~   "+s.get(1).getPeriodText());
                    System.out.println(s.get(2).getId() + "  ~~~~~~~~~~~   "+s.get(3).getPeriodText());


                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "Error retrieving data", throwable);
                }));

    }



    void getData(String link,int id) {
        try {
            viewModel.getData(link).observe(requireActivity(), data -> {
                if (data != null) {
                    enrollData.clear();
                    enrollData.addAll(data);
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);

                    saveEnrollHistory(id);
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "getData: " + e.getMessage());
        }

    }

    public EnrollmentHistoryFragment() {
        // Required empty public constructor
    }


}