package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import android.os.Build;
import android.os.Bundle;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivitySubjectDetailBinding;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.viewmodel.RoomViewModel;

public class SubjectDetailActivity extends AppCompatActivity {
    private ActivitySubjectDetailBinding binding;
    private DashboardModel subjectData;
    private RoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        subjectData = (DashboardModel) PortalApp.getSerializable(bundle, "subject", DashboardModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        init();
    }

    void init() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        setViews();
        getRooms();
    }

    void getRooms() {
        roomViewModel.getRooms().observe(this, data -> {
            for (RoomModel.rooms s : data.getRooms()) {
                if (s.getRoomId().trim().equals(subjectData.getRoom().replace("-","").trim())) {
                    System.out.println(s.getLocation());
                    for(RoomModel.previews img:s.getPreviews()){
                        System.out.println(img.getImage());
                    }
                }
            }
        });
    }

    void setViews() {
        binding.subjDesc.setText(subjectData.getDescription());
        binding.offerNo.setText(subjectData.getOfferNo());
        binding.subjCode.setText(subjectData.getSubjCode());
        binding.schedule.setText(subjectData.getDays().concat(" - ").concat(subjectData.getTime()));
        binding.room.setText(subjectData.getRoom().replace("-", ""));

    }


}