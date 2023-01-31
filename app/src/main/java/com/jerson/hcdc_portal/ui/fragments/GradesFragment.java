package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.jerson.hcdc_portal.databinding.FragmentGradesBinding;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.viewmodel.GradesLinksViewModel;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;

import java.util.ArrayList;
import java.util.List;


public class GradesFragment extends Fragment {
    private static final String TAG = "GradesFragment";
    FragmentGradesBinding binding;
    List<GradeLinksModel> semGradeList = new ArrayList<>();
    List<String> list = new ArrayList<>();
    List<GradeModel> gradeList = new ArrayList<>();

    GradesLinksViewModel viewModel;
    GradesViewModel gradesViewModel;
    ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGradesBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(getActivity()).get(GradesLinksViewModel.class);
        gradesViewModel = new ViewModelProvider(getActivity()).get(GradesViewModel.class);

        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);

        getDataLinks();

        binding.spinnerSem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: "+semGradeList.get(i).getSemGradeLink()+" ()"+i);
                if(i!=0){
                    getGrades(semGradeList.get(i).getSemGradeLink());
                }
            }
        });

        return binding.getRoot();
    }

    void getDataLinks() {
        viewModel.getGradesLink().observeForever(data -> {
            if(data!=null){
                list.clear();
                semGradeList.clear();
                semGradeList.addAll(data);
                for(GradeLinksModel d:data){
                    list.add(d.getSemGradeText());
                }
                binding.spinnerSem.clearListSelection();
                binding.spinnerSem.setSelected(true);
                binding.spinnerSem.setHint(list.get(0));
                arrayAdapter.notifyDataSetChanged();
            }

        });
    }

    void getGrades(String link){
        gradesViewModel.getData(link).observeForever(data->{
            if(data!=null){
                gradeList.clear();
                gradeList.addAll(data);
            }
        });
    }


    public GradesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}