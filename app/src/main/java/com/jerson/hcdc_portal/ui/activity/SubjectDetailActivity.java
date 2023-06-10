package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivitySubjectDetailBinding;
import com.jerson.hcdc_portal.model.DashboardModel;

public class SubjectDetailActivity extends AppCompatActivity {
    private ActivitySubjectDetailBinding binding;
    private DashboardModel subjectData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        subjectData = (DashboardModel) PortalApp.getSerializable(bundle,"subject",DashboardModel.class);

        init();
    }

    void init(){
        binding.btnBack.setOnClickListener(v->onBackPressed());
        setViews();
    }

    void setViews(){
        binding.subjDesc.setText(subjectData.getDescription());
        binding.offerNo.setText(subjectData.getOfferNo());
        binding.subjCode.setText(subjectData.getSubjCode());
        binding.schedule.setText(subjectData.getDays().concat(" - ").concat(subjectData.getTime()));
        binding.room.setText(subjectData.getRoom().replace("-",""));

    }


}