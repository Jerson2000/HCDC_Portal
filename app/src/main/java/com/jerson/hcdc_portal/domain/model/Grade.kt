package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("grades")
data class Grade(
    @PrimaryKey(true)
    val id:Int,
    val termId:Int?,
    val term:String?,
    val offeredNo:String?,
    val subjectCode:String?,
    val description:String,
    val unit:String?,
    val midGrade:String?,
    val midRemark:String?,
    val finalGrade:String?,
    val finalRemark:String?,
    val earnedUnits:String?,
    val weightedAve:String?,
    val teacher:String?,
)
