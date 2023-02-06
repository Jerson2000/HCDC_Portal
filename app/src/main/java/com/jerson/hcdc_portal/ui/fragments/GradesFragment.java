package com.jerson.hcdc_portal.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.jerson.hcdc_portal.databinding.FragmentGradesBinding;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.ui.adapter.GradeAdapter;
import com.jerson.hcdc_portal.viewmodel.GradesLinksViewModel;
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
    private GradesLinksViewModel viewModel;
    private GradesViewModel gradesViewModel;
    private ArrayAdapter<String> arrayAdapter;
    private int position = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGradesBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(getActivity()).get(GradesLinksViewModel.class);
        gradesViewModel = new ViewModelProvider(getActivity()).get(GradesViewModel.class);

        // Material TextField Autocomplete - simply known as dropdown
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSem.setAdapter(arrayAdapter);

        try {
            getDataLinks();

            binding.spinnerSem.setOnItemClickListener((adapterView, view, i, l) -> {
                Log.d(TAG, "onItemClick: " + semGradeList.get(i).getSemGradeLink() + " ()" + i);
                if (i != 0) {
                    position = i;
                    binding.progressBar.setVisibility(View.VISIBLE);
                    getGrades(semGradeList.get(i).getSemGradeLink(), i);
                    getGradesResponse();
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: " + e.getMessage());
        }


        // RecyclerView Instance
        binding.gradeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GradeAdapter(gradeList, getActivity());
        binding.gradeRecyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    void getDataLinks() {
        viewModel.getGradesLink().observeForever(data -> {
            if (data != null) {
                list.clear();
                semGradeList.clear();
                semGradeList.addAll(data);
                for (GradeLinksModel d : data) {
                    list.add(d.getSemGradeText());
                }
//                binding.spinnerSem.clearListSelection();
//                binding.spinnerSem.setSelected(true);
//                binding.spinnerSem.setHint(list.get(0));
                arrayAdapter.notifyDataSetChanged();

            }

        });
    }

    void getGrades(String link, int pos) {
        gradesViewModel.getData(link).observe(getActivity(),data -> {
            if (data != null) {
                try{
                    String w = "Weighted % Ave: ";
                    String e = "Earned Units: ";
                    gradeList.clear();
                    gradeList.addAll(data);
                    adapter.notifyDataSetChanged();
                    String earn = gradeList.get(pos).getEarnedUnits();
                    String ave = gradeList.get(pos).getAverage();
                    binding.earnUnits.setText(e.concat(earn));
                    binding.weightedGrade.setText(w.concat(ave));
                    binding.unitsGradeLayout.setVisibility(View.VISIBLE);
                    binding.semSelectorLayout.setVisibility(View.VISIBLE);
                    binding.gradeRecyclerView.setVisibility(View.VISIBLE);

                    binding.progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "getGrades: " + data.get(pos).getAverage() + "-" + data.get(pos).getEarnedUnits());
                }
                catch (Exception e){
                    Log.d(TAG, "getGrades: "+e.getMessage());
                }

            }
        });
    }

    void getGradesResponse() {
        gradesViewModel.getResponse().observe(getActivity(),response -> {
            Log.d(TAG, "getGradesResponse: " + response);
            if (response == null) {
                binding.retryLayout.getRoot().setVisibility(View.VISIBLE);
                binding.gradeRecyclerView.setVisibility(View.GONE);
                binding.unitsGradeLayout.setVisibility(View.GONE);
                binding.semSelectorLayout.setVisibility(View.GONE);
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