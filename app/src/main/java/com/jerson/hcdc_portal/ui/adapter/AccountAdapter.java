package com.jerson.hcdc_portal.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerAccountBinding;
import com.jerson.hcdc_portal.databinding.ItemContainerGradeBinding;
import com.jerson.hcdc_portal.model.AccountModel;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    Context context;
    List<AccountModel> list;

    public AccountAdapter(Context context, List<AccountModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AccountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerAccountBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.ViewHolder holder, int position) {
        AccountModel model = list.get(position);

        holder.binding.refNo.setText(model.getReference());
        holder.binding.added.setText(model.getAdded());
        holder.binding.deducted.setText(model.getAdded());
        holder.binding.runBal.setText(model.getRunBal());
        holder.binding.date.setText(model.getDate());
        holder.binding.desc.setText(model.getDescription());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAccountBinding binding;
        public ViewHolder(ItemContainerAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
