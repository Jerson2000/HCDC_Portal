package com.jerson.hcdc_portal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.Index

@Entity(
    tableName = "grades",
    indices = [Index(value = ["termId"])]
)
data class Grade(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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

// --- Prospectus Room Database Entities ---

@Entity(
    tableName = "prospectus_meta",
    indices = [Index(value = ["programCode", "effectiveAy"], unique = true)]
)
data class ProspectusEntity(
    @PrimaryKey
    val id: String, // e.g. "BSIT_2023-2024"
    val programCode: String,
    val programName: String,
    val revision: String,
    val effectiveAy: String,
    val totalUnits: Double,
    val lastSyncedTimestamp: Long
)

@Entity(
    tableName = "prospectus_subjects",
    indices = [Index(value = ["prospectusId", "yearLevelOrder", "semesterOrder"])]
)
data class ProspectusSubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val prospectusId: String,
    val yearLevelName: String,
    val yearLevelOrder: Int,
    val semesterName: String,
    val semesterOrder: Int,
    val subjectCode: String,
    val subjectName: String,
    val units: String,
    val preRequisites: String,
    val totalUnitsDisplay: String
)
