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
import com.jerson.hcdc_portal.listener.DynamicListener;
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
    private static final String TAG = "EnrollmentHistoryFragment";
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
        loadEnrollHistoryLink(linkRetrieved);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEnrollmentHistoryBinding.inflate(inflater, container, false);

        init();

        return binding.getRoot();
    }

    void init() {

        // dropdown/spinner
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);


        binding.enrHistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EnrollHistoryAdapter(getActivity(), enrollData);
        binding.enrHistRecyclerView.setAdapter(adapter);



        /*loadEnrollHistory();*/

        binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d(TAG, "onItemClick: " + periodLinks.get(i).getPeriodText() + " [" + periodLinks.get(i).getId() + "] ");
            binding.progressBar.setVisibility(View.VISIBLE);
            loadEnrollHistory(periodLinks.get(i).getId(), object -> {
                if (!object) {
                    getData(periodLinks.get(i).getPeriodLink(), periodLinks.get(i).getId());
                }
            });

        });

        binding.retryLayout.retryBtn.setOnClickListener(v -> {
            getLinks();
            binding.retryLayout.retryBtn.setEnabled(false);
        });
//        loadEnrollHistory();

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
                    deleteEnrollHistoryLinkData(object -> {
                        if (object) {
                            saveEnrollHistoryLink();
                        }
                    });


                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLinks: " + e.getMessage());
        }
    }


    void saveEnrollHistory(int link_id) {
        for (int i = 0; i < enrollData.size(); i++) {
            enrollData.get(i).setLink_id(link_id);
        }
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistory(enrollData)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.w(TAG, "saveEnrollHistory Data saved: " + enrollData.size());
                }, throwable -> {
                    Log.d(TAG, "saveEnrollHistory: " + throwable);
                })
        );
    }

    void saveEnrollHistoryLink() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistoryLink(periodLinks)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.w(TAG, "saveEnrollHistoryLink Data saved: " + periodLinks.size());
                }, throwable -> {
                    Log.d(TAG, "saveEnrollHistoryLink: " + throwable);
                })
        );
    }

    private void loadEnrollHistory(int link_id, DynamicListener<Boolean> isLoadEnrollHistory) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistory(link_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    Log.d(TAG, "LoadEnrollHistory data retrieved successfully: " + data.size());

                    if (data.size() > 0) {
                        enrollData.clear();
                        enrollData.addAll(data);
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        isLoadEnrollHistory.dynamicListener(true);
                    } else {
                        isLoadEnrollHistory.dynamicListener(false);
                    }

                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "LoadEnrollHistory error retrieving data", throwable);
                }));

    }


    /*private void loadEnrollHistory() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    Log.d(TAG, "loadEnrollHistory Data retrieved successfully: " + data.size());

                    *//*for (EnrollHistModel d : data) {
                        System.out.println("["+d.getLink_id()+"] => "+d.getId() + " =>>" +d.getOfferNo());
                    }*//*

                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "Error retrieving data", throwable);
                }));

    }*/

    private void loadEnrollHistoryLink(DynamicListener<Boolean> linkRetrieved) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistoryLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d(TAG, "LoadEnrollHistoryLink data retrieved: " + data.size());
                    if (data.size() > 0) {
                        list.clear();
                        periodLinks.clear();
                        periodLinks.addAll(data);
                        linkRetrieved.dynamicListener(true);
                        for (EnrollHistModel.Link d : data) {
                            list.add(d.getPeriodText());

                            /*  System.out.println(d.getId() + " =>>" +d.getPeriodText());*/
                        }

                    } else {
                        linkRetrieved.dynamicListener(false);
                    }

                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "LoadEnrollHistoryLink error retrieving data", throwable);
                }));

    }

    void deleteEnrollHistoryLinkData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteEnrollHistoryLinkData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteEnrollHistoryLinkData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteEnrollHistoryLinkData: ", throwable);
                })
        );
    }

    void deleteEnrollHistoryData(int link_id, DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteEnrollHistoryData(link_id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteEnrollHistoryData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteEnrollHistoryData: ", throwable);
                })
        );
    }

    DynamicListener<Boolean> linkRetrieved = new DynamicListener<Boolean>() {
        @Override
        public void dynamicListener(Boolean object) {
            if (object) {

                binding.progressBar.setVisibility(View.GONE);
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.enrHistRecyclerView.setVisibility(View.VISIBLE);
                arrayAdapter.notifyDataSetChanged();
            } else {
                getLinks();
            }
        }
    };


    void getData(String link, int id) {
        viewModel.getData(link).observe(requireActivity(), data -> {
            if (data != null) {
                enrollData.clear();
                enrollData.addAll(data);
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);

                deleteEnrollHistoryData(id, object -> {
                    if (object) {
                        saveEnrollHistory(id);
                    }
                });

            }
        });


    }

    public EnrollmentHistoryFragment() {
        // Required empty public constructor
    }


}