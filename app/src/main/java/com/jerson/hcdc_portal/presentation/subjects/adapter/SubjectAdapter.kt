package com.jerson.hcdc_portal.presentation.subjects.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerDashboardBinding
import com.jerson.hcdc_portal.domain.model.Schedule

class SubjectAdapter(private val list:List<Schedule>,private val itemCallback:(Schedule)->Unit):RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding:ItemContainerDashboardBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Schedule){
            binding.apply {
                subjCode.text = item.subjectCode
                desc.text = item.description
                schedDays.text = item.days
                schedTime.text = item.time

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerDashboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = list[position]
        holder.bind(subject)
        holder.itemView.setOnClickListener{
            itemCallback(subject)
        }
    }
}