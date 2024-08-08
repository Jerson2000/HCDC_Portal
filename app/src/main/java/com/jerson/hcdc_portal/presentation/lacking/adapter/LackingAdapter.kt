package com.jerson.hcdc_portal.presentation.lacking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerLackingBinding

class LackingAdapter(private val list:List<String>): RecyclerView.Adapter<LackingAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemContainerLackingBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:String){
            binding.lack.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LackingAdapter.ViewHolder {
        return ViewHolder(
            ItemContainerLackingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: LackingAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }
}