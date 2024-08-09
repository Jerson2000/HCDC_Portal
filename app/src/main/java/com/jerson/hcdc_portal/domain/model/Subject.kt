package com.jerson.hcdc_portal.domain.model


data class Subject(
    val subjectId:Int,
    val offeredNo:String?,
    val gClassCode:String?,
    val subjectCode:String?,
    val description:String,
    val unit:String?,
    val days:String?,
    val time:String?,
    val room:String?,
    val lecLab:String?,
    val midGrade:String?,
    val midRemark:String?,
    val finalGrade:String?,
    val finalRemark:String?,
    val earnedUnits:String?,
    val weightedAve:String?,
    val teacher:String?,
)

