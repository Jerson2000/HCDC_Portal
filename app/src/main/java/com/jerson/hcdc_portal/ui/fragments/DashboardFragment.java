package com.jerson.hcdc_portal.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.gson.Gson;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.FragmentDashboardBinding;
import com.jerson.hcdc_portal.databinding.ProfileLayoutDialogBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.ChatAIRepo;
import com.jerson.hcdc_portal.ui.activity.BuildingActivity;
import com.jerson.hcdc_portal.ui.activity.ChatAIActivity;
import com.jerson.hcdc_portal.ui.activity.EvaluationActivity;
import com.jerson.hcdc_portal.ui.activity.LackingActivity;
import com.jerson.hcdc_portal.ui.activity.SettingsActivity;
import com.jerson.hcdc_portal.ui.activity.SubjectDetailActivity;
import com.jerson.hcdc_portal.ui.activity.SubjectOfferedActivity;
import com.jerson.hcdc_portal.ui.adapter.DashboardAdapter;
import com.jerson.hcdc_portal.util.BaseFragment;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.NetworkUtil;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.ChatAIViewModel;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DashboardFragment extends BaseFragment<FragmentDashboardBinding> implements OnClickListener<DashboardModel> {
    private FragmentDashboardBinding binding;
    private DashboardAdapter adapter;
    private List<DashboardModel> dashList = new ArrayList<>();
    private List<DashboardModel> todayList = new ArrayList<>();
    private DashboardViewModel viewModel;
    private PreferenceManager preferenceManager;
    private ProfileLayoutDialogBinding profileDialogLayoutBinding;

    private static final String TAG = "DashboardFragment";
    private AlertDialog dialog;

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
        profileDialogLayoutBinding = ProfileLayoutDialogBinding.inflate(LayoutInflater.from(requireActivity()));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new DashboardAdapter(requireActivity(), todayList, this);
        binding.recyclerView.setAdapter(adapter);

        if (preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE) != null && !preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE).equals("")) {
            binding.enrollAnnounceLayout.setVisibility(View.VISIBLE);
            binding.enrollAnnounce.setText(preferenceManager.getString(PortalApp.KEY_ENROLL_ANNOUNCE));
        } else {
            binding.enrollAnnounceLayout.setVisibility(View.GONE);
        }
        binding.enrolledTV.setText(preferenceManager.getString(PortalApp.KEY_IS_ENROLLED)==null?"":preferenceManager.getString(PortalApp.KEY_IS_ENROLLED));
        binding.unitsTV.setText(preferenceManager.getString(PortalApp.KEY_STUDENTS_UNITS)==null?"0":preferenceManager.getString(PortalApp.KEY_STUDENTS_UNITS));

        String pDetails = "ID number: " + preferenceManager.getString(PortalApp.KEY_STUDENT_ID) + "\n" +
                "Name: " + preferenceManager.getString(PortalApp.KEY_STUDENT_NAME) + "\n" +
                "Course: " + preferenceManager.getString(PortalApp.KEY_STUDENT_COURSE);

        binding.btnProfile.setOnClickListener(v -> {
            ViewGroup parentView = (ViewGroup) profileDialogLayoutBinding.getRoot().getParent();
            if (parentView != null) {
                parentView.removeView(profileDialogLayoutBinding.getRoot());
            }
            dialog = Dialog.CustomDialog("Student Info.", pDetails, requireActivity(), profileDialogLayoutBinding.getRoot())
                    .setPositiveButton("Okay", (dialogInterface, i) -> dialog.dismiss()).show();
            profileDialogLayoutBinding.lackBtn.setOnClickListener(s -> {
                startActivity(new Intent(requireActivity(), LackingActivity.class));
                dialog.dismiss();
            });
            profileDialogLayoutBinding.chatAIBtn.setOnClickListener(s -> {
                if(NetworkUtil.isConnected()){
                    startActivity(new Intent(requireActivity(), ChatAIActivity.class));
                    dialog.dismiss();
                }else{
                    PortalApp.showToast("No internet connection!");
                }

            });
        });
        binding.btnSetting.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
            requireActivity().finish();
        });

        binding.evaluation.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EvaluationActivity.class));
        });

        binding.subjectOffered.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), SubjectOfferedActivity.class));
        });
        binding.buildings.setOnClickListener(v->{
            startActivity(new Intent(requireActivity(), BuildingActivity.class));
        });

        binding.evaluationIV.setImage(new ImageSource.Resource(R.drawable.paper));

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
        viewModel.loadSubjects().observe(requireActivity(),data->{
            if (data.size() > 0) {
                dashList.clear();
                dashList.addAll(data);
                isRetrieved.dynamicListener(true);
                isLoading(false);

            } else {
                isRetrieved.dynamicListener(false);
            }
        });
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