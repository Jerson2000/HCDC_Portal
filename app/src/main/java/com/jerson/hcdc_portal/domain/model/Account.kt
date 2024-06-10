package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("accounts")
data class Account(
    @PrimaryKey(true)
    val id:Int,
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
)
