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

import com.jerson.hcdc_portal.databinding.FragmentGradesBinding;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.ui.adapter.GradeAdapter;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;

import java.util.ArrayList;
import java.util.List;


public class GradesFragment extends Fragment {
    private static final String TAG = "GradesFragment";
    private FragmentGradesBinding binding;
    private List<GradeLinksModel> semGradeList = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private List<GradeModel> gradeList = new ArrayList<>();
    private GradeAdapter adapter;
    private GradesViewModel viewModel;
    private ArrayAdapter<String> arrayAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(GradesViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGradesBinding.inflate(inflater, container, false);


        // Material TextField Autocomplete - simply known as dropdown
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);


        binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d(TAG, "onItemClick: " + semGradeList.get(i).getSemGradeLink() + " ()" + i);
            if (i != 0) {
                binding.progressBar.setVisibility(View.VISIBLE);
                getGrade(semGradeList.get(i).getSemGradeLink());
            }
        });


        // RecyclerView Instance
        binding.gradeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GradeAdapter(gradeList, requireActivity());
        binding.gradeRecyclerView.setAdapter(adapter);

        getLinks();

        return binding.getRoot();
    }

    void getLinks() {
        viewModel.getLinks().observe(requireActivity(), data -> {
            if (data != null) {
                list.clear();
                semGradeList.clear();
                semGradeList.addAll(data);
                for (GradeLinksModel d : data) {
                    list.add(d.getSemGradeText());
                }
                arrayAdapter.notifyDataSetChanged();
                binding.semSelectorLayout.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

        });
    }

    void getGrade(String link){
        viewModel.gradeData(link).observe(requireActivity(),data->{
            if (data != null) {
                try {
                    String w = "Weighted % Ave: ";
                    String e = "Earned Units: ";
                    gradeList.clear();
                    gradeList.addAll(data);
                    adapter.notifyDataSetChanged();
                    String earn = gradeList.get(0).getEarnedUnits();
                    String ave = gradeList.get(0).getAverage();
                    binding.earnUnits.setText(e.concat(earn));
                    binding.weightedGrade.setText(w.concat(ave));
                    binding.unitsGradeLayout.setVisibility(View.VISIBLE);
                    binding.semSelectorLayout.setVisibility(View.VISIBLE);
                    binding.gradeRecyclerView.setVisibility(View.VISIBLE);

                    binding.progressBar.setVisibility(View.GONE);

                } catch (Exception e) {
                    Log.d(TAG, "getGrades: " + e.getMessage());
                }

            }
        });
    }


    public GradesFragment() {
    }




}