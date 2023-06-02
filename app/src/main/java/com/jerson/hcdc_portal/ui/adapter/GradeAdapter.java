package com.jerson.hcdc_portal.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerDashboardBinding;
import com.jerson.hcdc_portal.databinding.ItemContainerGradeBinding;
import com.jerson.hcdc_portal.model.GradeModel;

import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder> {

    List<GradeModel> gradeList;
    Context context;

    public GradeAdapter(List<GradeModel> gradeList, Context context) {
        this.gradeList = gradeList;
        this.context = context;
    }

    @NonNull
    @Override
    public GradeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerGradeBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull GradeAdapter.ViewHolder holder, int position) {
        GradeModel model = gradeList.get(position);

        holder.binding.subjCode.setText(model.getCode());
        holder.binding.desc.setText(model.getDescription());
        holder.binding.midGrade.setText(model.getMidGrade());
        holder.binding.midRemark.setText(model.getMidRemark());
        holder.binding.finalGrade.setText(model.getFinalGrade());
        holder.binding.finalRemark.setText(model.getFinalRemark());
        holder.binding.teacher.setText(model.getTeacher());
        holder.binding.unit.setText(model.getUnit());
        holder.binding.subject.setText(model.getSubject());
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGradeBinding binding;
        public ViewHolder(ItemContainerGradeBinding itemContainerGradeBinding) {
            super(itemContainerGradeBinding.getRoot());
            this.binding = itemContainerGradeBinding;
        }
    }
}
