package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.FragmentGradesBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.ui.adapter.GradeAdapter;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class GradesFragment extends Fragment {
    private static final String TAG = "GradesFragment";
    private FragmentGradesBinding binding;
    private List<GradeModel.Link> semGradeList = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private List<GradeModel> gradeList = new ArrayList<>();
    private GradeAdapter adapter;
    private GradesViewModel viewModel;
    private ArrayAdapter<String> arrayAdapter;

    private LoginViewModel loginViewModel;
    private PreferenceManager preferenceManager;
    private int selectedId = 0;
    private String selectedLink = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GradesViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        preferenceManager = new PreferenceManager(requireActivity());
        loadGradeLink(linkListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGradesBinding.inflate(inflater, container, false);


        init();

        return binding.getRoot();
    }

    void init() {

        // dropdown/spinner
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);


        binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d(TAG, "onItemClick: " + semGradeList.get(i).getLink() + "[" + semGradeList.get(i).getId() + "]");
            if (i != 0) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.gradeLayout.setVisibility(View.GONE);
                binding.gradeRecyclerView.setVisibility(View.GONE);

                selectedId = semGradeList.get(i).getId();
                selectedLink = semGradeList.get(i).getLink();

                loadGrade(semGradeList.get(i).getId(), object -> {
                    if (!object && !binding.refreshLayout.isRefreshing()) {
                        if (PortalApp.isConnected()) {
                            checkSession(object2 -> {
                                if (object2) {
                                    getGrade(semGradeList.get(i).getId(), semGradeList.get(i).getLink());
                                }
                            });
                        } else{
                            Toast.makeText(requireActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                            Random random = new Random();
                            int n = random.nextInt(6);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.errLayout.setVisibility(View.VISIBLE);
                            binding.errText.setText("No internet connection.");
                            binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
                        }

                    }
                });


            }
        });

        binding.refreshLayout.setOnRefreshListener(() -> {
            binding.refreshLayout.setRefreshing(true);
            if (PortalApp.isConnected()) {
                checkSession(object -> {
                    if (object) {
                        getGrade(selectedId, selectedLink);
                    }
                });
            } else{
                Toast.makeText(requireActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                binding.refreshLayout.setRefreshing(false);
                Random random = new Random();
                int n = random.nextInt(6);
                binding.progressBar.setVisibility(View.GONE);
                binding.gradeRecyclerView.setVisibility(View.GONE);
                binding.errLayout.setVisibility(View.VISIBLE);
                binding.errText.setText("No internet connection.");
                binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
            }

        });


        binding.gradeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GradeAdapter(gradeList, requireActivity());
        binding.gradeRecyclerView.setAdapter(adapter);


    }

    void getLink() {
        viewModel.getLinks().observe(requireActivity(), data -> {
            if (data != null) {
                list.clear();
                semGradeList.clear();
                semGradeList.addAll(data);

                deleteGradeLink(object -> {
                    if (object) {
                        saveGradeLink();
                    }
                });

                for (GradeModel.Link d : data) {
                    list.add(d.getText());
                }
                arrayAdapter.notifyDataSetChanged();
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

        });
    }

    void getGrade(int link_id, String link) {
        viewModel.gradeData(link).observe(requireActivity(), data -> {
            if (data != null) {
                gradeList.clear();
                gradeList.addAll(data);
                adapter.notifyDataSetChanged();

                deleteGrade(link_id, object -> {
                    if (object) {
                        saveGrade(link_id, data, object1 -> {
                            if (object1) {
                                binding.refreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });

                String earn = gradeList.get(0).getEarnedUnits();
                String ave = gradeList.get(0).getAverage();
                binding.earnUnits.setText(earn);
                binding.weightedAve.setText(ave);

                binding.gradeLayout.setVisibility(View.VISIBLE);
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.gradeRecyclerView.setVisibility(View.VISIBLE);

                binding.progressBar.setVisibility(View.GONE);

            }
        });
    }

    void checkSession(DynamicListener<Boolean> listener) {
        loginViewModel.checkSession(object -> {
            if (object) {
                loginViewModel.Login(preferenceManager.getString(PortalApp.KEY_EMAIL), preferenceManager.getString(PortalApp.KEY_PASSWORD)).observe(requireActivity(), data -> {
                    Log.e(TAG, "dynamicListener: " + data);
                    if (data.toLowerCase(Locale.ROOT).contains("logged")) {
                        checkSession(listener);
                    }
                });

            } else listener.dynamicListener(true);

        });

    }

    /* database */
    void loadGradeLink(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadGradeLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (data.size() > 0) {
                        list.clear();
                        semGradeList.clear();
                        semGradeList.addAll(data);
                        for (GradeModel.Link d : data) {
                            list.add(d.getText());
                        }
                        /*Log.e(TAG, "loadGradeLink: " + data.size());*/
                        listener.dynamicListener(true);
                    } else listener.dynamicListener(false);
                }, throwable -> {

                })
        );
    }

    DynamicListener<Boolean> linkListener = object -> {
        if (!object) {
            if (PortalApp.isConnected()){
                checkSession(object1 -> {
                    if (object1) {
                        getLink();
                    }
                });
            }else{
                Random random = new Random();
                int n = random.nextInt(6);
                binding.progressBar.setVisibility(View.GONE);
                binding.errLayout.setVisibility(View.VISIBLE);
                binding.errText.setText("No internet connection.");
                binding.errEmoji.setText(PortalApp.SAD_EMOJIS[n]);
            }

        } else {
            arrayAdapter.notifyDataSetChanged();
            binding.semSelectorLayout.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };


    void saveGradeLink() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertGradeLink(semGradeList)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.w(TAG, "saveGradeLink Data saved: " + semGradeList.size());*/
                }, throwable -> Log.d(TAG, "saveGradeLink: " + throwable))
        );

    }

    void deleteGradeLink(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteGradeLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> isDeleted.dynamicListener(true), throwable -> {
                    Log.e(TAG, "deleteGradeLink: ", throwable);
                    isDeleted.dynamicListener(false);
                }));
    }

    void loadGrade(int link_id, DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.loadGrade(link_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (data.size() > 0) {
                        gradeList.clear();
                        gradeList.addAll(data);
                        adapter.notifyDataSetChanged();
                        String earn = gradeList.get(0).getEarnedUnits();
                        String ave = gradeList.get(0).getAverage();
                        binding.earnUnits.setText(earn);
                        binding.weightedAve.setText(ave);

                        binding.gradeLayout.setVisibility(View.VISIBLE);
                        binding.semSelectorLayout.setVisibility(View.VISIBLE);
                        binding.gradeRecyclerView.setVisibility(View.VISIBLE);

                        binding.progressBar.setVisibility(View.GONE);
                        binding.errLayout.setVisibility(View.GONE);

                    }
                    listener.dynamicListener(data.size() > 0);
                    /*Log.e(TAG, "loadGrade: " + data.size());*/
                }, throwable -> Log.e(TAG, "loadGrade: ", throwable)));
    }

    void saveGrade(int link_id, List<GradeModel> list, DynamicListener<Boolean> listener) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setLink_id(link_id);
        }
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.insertGrade(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.e(TAG, "saveGrade: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "saveGrade: ", throwable);
                    listener.dynamicListener(false);
                })

        );
    }

    void deleteGrade(int link_id, DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(viewModel.deleteGrade(link_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.e(TAG, "deleteGrade: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "deleteGrade: ", throwable);
                    listener.dynamicListener(false);
                })
        );
    }


    public GradesFragment() {
    }


}