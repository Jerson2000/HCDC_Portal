package com.jerson.hcdc_portal.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerSubjectOfferedBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.SubjectOfferedModel;

import java.util.List;

public class SubjectOfferedAdapter extends RecyclerView.Adapter<SubjectOfferedAdapter.ViewHolder> {
    private List<SubjectOfferedModel.SubjectOffered> list;
    private OnClickListener<Integer> listener;

    public SubjectOfferedAdapter(List<SubjectOfferedModel.SubjectOffered> list, OnClickListener<Integer> listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectOfferedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemContainerSubjectOfferedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectOfferedAdapter.ViewHolder holder, int position) {
        SubjectOfferedModel.SubjectOffered item = list.get(position);
        holder.binding.course.setText(item.getCourse());
        holder.binding.OfferNo.setText("Offer #: "+item.getOfferedNo());
        holder.binding.subjectCode.setText("Subject Code: "+item.getSubjectCode());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerSubjectOfferedBinding binding;
        public ViewHolder(@NonNull ItemContainerSubjectOfferedBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            binding.getRoot().setOnClickListener(v->{
                listener.onItemClick(getAdapterPosition());
            });
        }
    }
}
