package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.FragmentAccountBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.ui.adapter.AccountAdapter;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.AccountViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    private FragmentAccountBinding binding;
    private AccountViewModel viewModel;
    private AccountAdapter adapter;
    private List<AccountModel> accList = new ArrayList<>();
    private LoginViewModel loginViewModel;
    private PreferenceManager preferenceManager;
    private boolean isInserted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        preferenceManager = new PreferenceManager(requireActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);


        init();


        return binding.getRoot();
    }

    void init() {
        binding.accountRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AccountAdapter(getActivity(), accList);
        binding.accountRecyclerView.setAdapter(adapter);

        getAccounts(getAccountsListener);
        observeErr();

        binding.refreshLayout.setOnRefreshListener(() -> {
            binding.refreshLayout.setRefreshing(true);
            if (PortalApp.isConnected()) {
                checkSession(object -> {
                    if (object) {
                        getData();
                    }
                });
            } else {
                Toast.makeText(requireActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                binding.refreshLayout.setRefreshing(false);
            }


        });

    }


    void getData() {
        viewModel.getData().observe(requireActivity(), data -> {
            if (data != null) {
                accList.clear();
                accList.addAll(data);
                binding.dueText.setText(accList.get(0).getDueText());
                binding.dueAmount.setText(accList.get(0).getDue());

                deleteAccount(object -> {
                    if (object) {
                        insertAccount(object1 -> {
                            if (object1) {
                                binding.refreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });
                adapter.notifyDataSetChanged();
                isLoading(false);
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

    void observeErr(){
        loginViewModel.getErr().observe(requireActivity(),err->{
            showErr(err.getMessage());
        });

        viewModel.getErr().observe(requireActivity(),err->{
            showErr(err.getMessage());
        });
    }

    void showErr(String msg) {
        Random random = new Random();
        int n = random.nextInt(6);
        binding.accountRecyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.errLayout.setVisibility(View.VISIBLE);
        binding.errText.setText(msg);
        binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
    }

    void isLoading(boolean loading) {
        if (loading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.accountRecyclerView.setVisibility(View.GONE);
            binding.dueLayout.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.accountRecyclerView.setVisibility(View.VISIBLE);
            binding.dueLayout.setVisibility(View.VISIBLE);
        }
    }

    /* Database */

    void insertAccount(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertAccount(accList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.d(TAG, "insertAccount: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "insertAccount: ", throwable);
                    listener.dynamicListener(false);
                })

        );
    }

    void deleteAccount(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.d(TAG, "deleteAccount: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "deleteAccount: ", throwable);
                    listener.dynamicListener(false);
                })

        );
    }

    void getAccounts(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.getAccounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (data.size() > 0) {
                        accList.clear();
                        accList.addAll(data);
                        adapter.notifyDataSetChanged();
                        binding.dueText.setText(accList.get(0).getDueText());
                        binding.dueAmount.setText(accList.get(0).getDue());
                        isLoading(false);

                        listener.dynamicListener(true);
                    } else listener.dynamicListener(false);
                    /*Log.d(TAG, "getAccounts: " + data.size());*/
                }, throwable -> {
                    Log.e(TAG, "getAccounts: ", throwable);
                })

        );
    }


    DynamicListener<Boolean> getAccountsListener = object -> {
        if (!object && !binding.refreshLayout.isRefreshing()) {
            if (PortalApp.isConnected()) {
                checkSession(object1 -> {
                    if (object1) {
                        getData();
                    }
                });
            } else
                showErr("No internet connection.");

        }
    };


    public AccountFragment() {
        // Required empty public constructor
    }


}