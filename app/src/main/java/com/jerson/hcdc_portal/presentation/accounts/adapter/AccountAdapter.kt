package com.jerson.hcdc_portal.presentation.accounts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemAccountBinding
import com.jerson.hcdc_portal.domain.model.Account

class AccountAdapter(private val list:List<Account>):RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding:ItemAccountBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Account){
            binding.apply{
                date.text = item.date
                refNo.text = item.reference
                added.text = item.added
                deducted.text = item.deducted
                runBal.text = item.runningBal
                desc.text = item.description
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountAdapter.ViewHolder {
        return ViewHolder(
            ItemAccountBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AccountAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}