package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("subjects")
data class Subject(
    @PrimaryKey(true)
    val id:Int,
    val offeredNo:String?,
    val gClassCode:String?,
    val subjectCode:String?,
    val description:String,
    val unit:String?,
    val days:String?,
    val time:String?,
    val room:String?,
    val lecLab:String?
)
