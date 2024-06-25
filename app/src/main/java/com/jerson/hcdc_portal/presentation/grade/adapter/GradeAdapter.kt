package com.jerson.hcdc_portal.presentation.grade.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemGradeBinding

import com.jerson.hcdc_portal.domain.model.Grade

class GradeAdapter(private val list:List<Grade>):RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding:ItemGradeBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Grade){
            binding.apply {
                tvSub.text = item.subjectCode
                tvSubDescription.text = item.description
                tvGradeMidterm.text = item.midGrade
                tvGradeFinal.text = item.finalGrade
                tvTeacher.text = item.teacher
                tvOfferNo.text = item.offeredNo
            }
        }

    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeAdapter.ViewHolder {
           return ViewHolder(
               ItemGradeBinding.inflate(
                   LayoutInflater.from(parent.context),
                   parent,
                   false
               )
           )
        }

        override fun onBindViewHolder(holder: GradeAdapter.ViewHolder, position: Int) {
            val item = list[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
        return list.size
    }

}