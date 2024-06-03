package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("enroll_history")
data class EnrollHistory(
    @PrimaryKey(true)
    val id:Int,
    val termId:Int?,
    val term:String?,
    val offeredNo:String?,
    val subjectCode:String?,
    val description:String,
    val unit:String?,
)
