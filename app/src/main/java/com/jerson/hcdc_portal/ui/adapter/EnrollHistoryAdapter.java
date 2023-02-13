package com.jerson.hcdc_portal.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerAccountBinding;
import com.jerson.hcdc_portal.databinding.ItemContainerEnrollHistoryBinding;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.EnrollHistModel;

import java.util.List;

public class EnrollHistoryAdapter extends RecyclerView.Adapter<EnrollHistoryAdapter.ViewHolder> {
    Context context;
    List<EnrollHistModel> list;

    public EnrollHistoryAdapter(Context context, List<EnrollHistModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public EnrollHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerEnrollHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EnrollHistoryAdapter.ViewHolder holder, int position) {
        EnrollHistModel model = list.get(position);

        holder.binding.offerNo.setText(model.getOfferNo());
        holder.binding.desc.setText(model.getDescription());
        holder.binding.subjCode.setText(model.getSubjCode());
        holder.binding.unit.setText(model.getUnits());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerEnrollHistoryBinding binding;

        public ViewHolder(ItemContainerEnrollHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
