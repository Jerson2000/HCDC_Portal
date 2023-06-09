package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

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

        subjectData = (DashboardModel) getIntent().getSerializableExtra("subject");

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