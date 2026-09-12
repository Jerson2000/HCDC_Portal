package com.jerson.hcdc_portal.presentation.grade.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemGradeBinding

import com.jerson.hcdc_portal.domain.model.Grade

class GradeAdapter(private val list: List<Grade>) :
    RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Grade) {
            binding.apply {
                tvSub.text = item.subjectCode
                tvSubDescription.text = item.description
                tvGradeMidterm.text = if (item.midGrade.isNullOrBlank()) "0.0" else item.midGrade
                tvTeacher.text = if (item.teacher.isNullOrBlank()) "TBD" else item.teacher
                tvOfferNo.text = item.offeredNo

                // Semantic Grade Status indicator
                val finalG = item.finalGrade?.trim().orEmpty()
                val remark = (item.finalRemark ?: item.midRemark).orEmpty().trim().lowercase()
                val finalNum = finalG.toDoubleOrNull()

                val (statusText, textColor) = when {
                    remark.contains("passed") || (finalNum != null && finalNum in 1.0..3.0) -> {
                        Pair("Passed", androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_passed_text))
                    }
                    remark.contains("failed") || finalG == "5.0" || finalG == "5.00" -> {
                        Pair("Failed", androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_failed_text))
                    }
                    remark.contains("inc") || finalG.contains("INC", ignoreCase = true) -> {
                        Pair("Incomplete", androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_pending_text))
                    }
                    finalG.isNotBlank() -> {
                        Pair("Completed", androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_neutral_text))
                    }
                    else -> {
                        Pair("Ongoing", androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.hcdc_secondary))
                    }
                }
                tvGradeStatus.text = statusText
                tvGradeStatus.setTextColor(textColor)
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