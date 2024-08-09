package com.jerson.hcdc_portal.presentation.enrollhistory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemEnrollHistoryBinding
import com.jerson.hcdc_portal.domain.model.EnrollHistory

class EnrollHistoryAdapter(private val list:List<EnrollHistory>):RecyclerView.Adapter<EnrollHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding:ItemEnrollHistoryBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:EnrollHistory){
            binding.apply {
                tvOfferNo.text = item.offeredNo
                tvSub.text = item.subjectCode
                tvSubDescription.text = item.description
                tvUnit.text = item.unit
            }
        }

    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollHistoryAdapter.ViewHolder {
           return ViewHolder(
               ItemEnrollHistoryBinding.inflate(
                   LayoutInflater.from(parent.context),
                   parent,
                   false
               )
           )
        }

        override fun onBindViewHolder(holder: EnrollHistoryAdapter.ViewHolder, position: Int) {
            val item = list[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
        return list.size
    }

}