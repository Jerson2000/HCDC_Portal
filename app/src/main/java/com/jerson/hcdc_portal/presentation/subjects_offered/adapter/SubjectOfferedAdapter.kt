package com.jerson.hcdc_portal.presentation.subjects_offered.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerSubjectOfferedBinding
import com.jerson.hcdc_portal.domain.model.SubjectOffered

class SubjectOfferedAdapter(private val list:List<SubjectOffered>, private val itemCallback:(SubjectOffered)-> Unit):RecyclerView.Adapter<SubjectOfferedAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding:ItemContainerSubjectOfferedBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:SubjectOffered){
            binding.apply{
                course.text = item.course
                OfferNo.text = item.so_no
                subjectCode.text = item.subject_code
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerSubjectOfferedBinding.inflate(
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
        val item = list[position]
        holder.bind(item)
        holder.itemView.setOnClickListener{
            itemCallback(item)
        }
    }
}