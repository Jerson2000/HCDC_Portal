package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.adapter.EnrollHistoryAdapter;
import com.jerson.hcdc_portal.util.BaseFragment;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class EnrollmentHistoryFragment extends BaseFragment<FragmentEnrollmentHistoryBinding> {
    private static final String TAG = "EnrollmentHistoryFragment";
    private FragmentEnrollmentHistoryBinding binding;
    private EnrollHistoryViewModel viewModel;
    private List<EnrollHistModel.Link> periodLinks = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private List<EnrollHistModel> enrollData = new ArrayList<>();
    private EnrollHistoryAdapter adapter;
    private LoginViewModel loginViewModel;
    private PreferenceManager preferenceManager;
    private int selectedId = 0;
    private String selectedLink = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EnrollHistoryViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        preferenceManager = new PreferenceManager(requireActivity());
        loadEnrollHistoryLink(linkRetrieved);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = getBinding();
        if (!getBindingNull()) init();

    }

    void init() {

        // dropdown/spinner
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);


        binding.enrHistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EnrollHistoryAdapter(getActivity(), enrollData);
        binding.enrHistRecyclerView.setAdapter(adapter);


        observeErr();

        binding.spinnerSem.setFocusable(false);
        binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            /*Log.d(TAG, "onItemClick: " + periodLinks.get(i).getPeriodText() + " [" + periodLinks.get(i).getId() + "] ");*/
            binding.refreshLayout.setEnabled(true);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.enrHistRecyclerView.setVisibility(View.GONE);

            selectedId = periodLinks.get(i).getId();
            selectedLink = periodLinks.get(i).getPeriodLink();

            loadEnrollHistory(periodLinks.get(i).getId(), object -> {
                if (!object && !binding.refreshLayout.isRefreshing()) {
                    if (PortalApp.isConnected()) {
                        checkSession(object1 -> {
                            if (object1) {
                                getData(periodLinks.get(i).getPeriodLink(), periodLinks.get(i).getId());
                            }
                        });
                    } else
                        showErr("No internet connection.");


                }
            });

        });


        binding.refreshLayout.setOnRefreshListener(() -> {
            binding.refreshLayout.setRefreshing(true);
            if (PortalApp.isConnected()) {
                checkSession(object -> {
                    if (object) {
                        if (!binding.spinnerSem.getText().toString().equals(""))
                            getData(selectedLink, selectedId);
                        else
                            getLinks();
                    }
                });
            } else {
                Toast.makeText(requireActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                binding.refreshLayout.setRefreshing(false);

            }
        });


        binding.retryLayout.retryBtn.setOnClickListener(v -> {
            getLinks();
            binding.retryLayout.retryBtn.setEnabled(false);
        });

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
                            deleteAllEnrollHistoryData(isDel -> {
                                if (isDel) {
                                    saveEnrollHistoryLink(isSave -> {
                                        if (isSave) {
                                            binding.refreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                            });
                        }
                    });


                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLinks: " + e.getMessage());
        }
    }

    void observeErr() {
        loginViewModel.getErr().observe(requireActivity(), err -> {
            showErr(err.getMessage());
        });

        viewModel.getErr().observe(requireActivity(), err -> {
            showErr(err.getMessage());
        });
    }


    void saveEnrollHistory(int link_id, List<EnrollHistModel> list, DynamicListener<Boolean> listener) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setLink_id(link_id);
        }
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistory(list)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.w(TAG, "saveEnrollHistory Data saved: " + enrollData.size());*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.d(TAG, "saveEnrollHistory: " + throwable);
                    listener.dynamicListener(false);
                })
        );
    }

    void saveEnrollHistoryLink(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertEnrollHistoryLink(periodLinks)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.w(TAG, "saveEnrollHistoryLink Data saved: " + periodLinks.size());*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.d(TAG, "saveEnrollHistoryLink: " + throwable);
                    listener.dynamicListener(false);
                })
        );
    }

    private void loadEnrollHistory(int link_id, DynamicListener<Boolean> isLoadEnrollHistory) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistory(link_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    /*Log.d(TAG, "LoadEnrollHistory data retrieved successfully: " + data.size());*/

                    if (data.size() > 0) {
                        enrollData.clear();
                        enrollData.addAll(data);
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.enrHistRecyclerView.setVisibility(View.VISIBLE);
                        binding.errLayout.setVisibility(View.GONE);
                        isLoadEnrollHistory.dynamicListener(true);
                    } else {
                        isLoadEnrollHistory.dynamicListener(false);
                    }

                }, throwable -> {
                    // Handle the error
                    Log.e(TAG, "LoadEnrollHistory error retrieving data", throwable);
                }));

    }


    private void loadEnrollHistoryLink(DynamicListener<Boolean> linkRetrieved) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadEnrollHistoryLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    /*Log.d(TAG, "LoadEnrollHistoryLink data retrieved: " + data.size());*/
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
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteEnrollHistoryData: ", throwable);
                })
        );
    }

    void deleteAllEnrollHistoryData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteAllEnrollHistoryData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                })
        );
    }

    DynamicListener<Boolean> linkRetrieved = new DynamicListener<Boolean>() {
        @Override
        public void dynamicListener(Boolean object) {
            if (!object) {
                if (PortalApp.isConnected() && !binding.refreshLayout.isRefreshing()) {
                    checkSession(object1 -> {
                        if (object1) {
                            getLinks();
                        }
                    });
                }

                if (!PortalApp.isConnected()) showErr("No internet connection.");

            } else {
                arrayAdapter.notifyDataSetChanged();
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
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
                        saveEnrollHistory(id, data, object1 -> {
                            if (object1) {
                                binding.refreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });

            }
        });


    }

    void checkSession(DynamicListener<Boolean> listener) {
        loginViewModel.checkSession(object -> {
            if (object) {
                loginViewModel.Login(preferenceManager.getString(PortalApp.KEY_EMAIL), preferenceManager.getString(PortalApp.KEY_PASSWORD)).observe(requireActivity(), data -> {
                    /*Log.e(TAG, "dynamicListener: " + data);*/
                    if (data.toLowerCase(Locale.ROOT).contains("logged")) {
                        checkSession(listener);
                    }
                });

            } else listener.dynamicListener(true);

        });

    }

    void showErr(String msg) {
        Random random = new Random();
        int n = random.nextInt(6);
        binding.progressBar.setVisibility(View.GONE);
        binding.errLayout.setVisibility(View.VISIBLE);
        binding.errText.setText(msg);
        binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
    }


    public EnrollmentHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    protected FragmentEnrollmentHistoryBinding onCreateViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
        return FragmentEnrollmentHistoryBinding.inflate(layoutInflater, container, false);
    }

}