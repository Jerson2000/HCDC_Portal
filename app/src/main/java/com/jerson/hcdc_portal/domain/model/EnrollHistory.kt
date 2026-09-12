package com.jerson.hcdc_portal.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

import androidx.room.Index

@Parcelize
@Entity(
    tableName = "enroll_history",
    indices = [Index(value = ["termId"])]
)
data class EnrollHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val termId:Int?,
    val term:String?,
    val offeredNo:String?,
    val subjectCode:String?,
    val description:String,
    val unit:String?,
) : Parcelable
