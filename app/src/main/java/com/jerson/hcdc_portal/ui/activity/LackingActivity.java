package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityLackingBinding;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.adapter.LackingAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.NetworkUtil;
import com.jerson.hcdc_portal.viewmodel.LackingViewModel;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LackingActivity extends BaseActivity<ActivityLackingBinding> {
    private static final String TAG = "LackingActivity";
    private LackingViewModel viewModel;
    private List<String> list = new ArrayList<>();
    private LackingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getBindingNull()) init();
    }

    void init() {

        setSupportActionBar(getBinding().header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            getBinding().header.collapsingToolbar.setTitle("Lacking Credential");
        }

        viewModel = new ViewModelProvider(this).get(LackingViewModel.class);
        adapter = new LackingAdapter(list);
        getBinding().recyclcerView.setLayoutManager(new LinearLayoutManager(this));
        getBinding().recyclcerView.setAdapter(adapter);

        if(NetworkUtil.isConnected()){

        getLacking();
        displayError();
        }else errorCounter(new Exception("No internet connection."));
    }

    void getLacking() {
        viewModel.getLacking().observe(this, data -> {
            if (data != null || !data.equals("")) {
                if (data.toLowerCase(Locale.ROOT).contains("complied")) {
                    compliedLack(data);
                } else {
                    splitLack(data);
                }
                PortalApp.getPreferenceManager().putString(PortalApp.lackingCredential, data);
            }
        });
    }

    void splitLack(String data) {
        String[] s = data.split(":");
        Collections.addAll(list, s);
        adapter.notifyDataSetChanged();
    }

    void compliedLack(String data) {
       /* getBinding().errLayout.emoji.setText(PortalApp.HAPPY_EMOJIS[new Random().nextInt(6)]);
        getBinding().errLayout.response.setText(data);*/
    }

    void displayError() {
        viewModel.getErr().observe(this, err -> {
            if (!PortalApp.getPreferenceManager().getString(PortalApp.lackingCredential).isEmpty()) {
                String s = PortalApp.getPreferenceManager().getString(PortalApp.lackingCredential);
                if (s.toLowerCase(Locale.ROOT).contains("complied")) {
                    compliedLack(s);
                } else {
                    splitLack(s);
                }
            } else {
               errorCounter(err);
            }

        });
    }

    void errorCounter(Throwable err){
        if (!PortalApp.getPreferenceManager().getString(PortalApp.lackingCredential).isEmpty()) {
            String s = PortalApp.getPreferenceManager().getString(PortalApp.lackingCredential);
            if (s.toLowerCase(Locale.ROOT).contains("complied")) {
                compliedLack(s);
            } else {
                splitLack(s);
            }
        }
        /*else {
            getBinding().errLayout.emoji.setText(PortalApp.SAD_EMOJIS[new Random().nextInt(6)]);
            getBinding().errLayout.response.setText(err.getMessage());
        }*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected ActivityLackingBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityLackingBinding.inflate(layoutInflater);
    }
}