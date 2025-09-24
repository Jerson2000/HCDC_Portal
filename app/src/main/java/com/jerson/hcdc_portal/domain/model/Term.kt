package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "terms",
    indices = [Index(value = ["isGrade"])]
)
data class Term(
    @PrimaryKey(true)
    val id:Int,
    val urlPath:String?,
    val term:String?,
    val isGrade:Int
)
