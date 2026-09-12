package com.jerson.hcdc_portal.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

import androidx.room.Index

@Parcelize
@Entity(
    tableName = "accounts",
    indices = [Index(value = ["termId"])]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val termId:Int?,
    val term:String?,
    val date:String?,
    val reference:String?,
    val description:String?,
    val period:String?,
    val added:String?,
    val deducted:String?,
    val runningBal:String?,
    val dueTextPeriod:String?,
    val dueAmount:String?
) : Parcelable