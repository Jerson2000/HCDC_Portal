package com.jerson.hcdc_portal.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Room(
    @SerializedName("building") val building: List<Building> = emptyList()
)

@Keep
@Parcelize
data class Building(
    @SerializedName("floor_desc") val floor_desc: String = "",
    @SerializedName("floors") val floors: List<Floor> = emptyList(),
    @SerializedName("history") val history: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("img_path") val img_path: String = "",
    @SerializedName("located") val located: String = "",
    @SerializedName("name") val name: String = ""
): Parcelable

@Keep
@Parcelize
data class Floor(
    @SerializedName("floor_no") val floor_no: Int = 0,
    @SerializedName("rooms") val rooms: List<Rooms> = emptyList()
): Parcelable

@Keep
@Parcelize
data class Rooms(
    @SerializedName("desc") val desc: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("preview") val preview: List<Preview> = emptyList(),
    @SerializedName("room_no") val room_no: String = ""
): Parcelable

@Keep
@Parcelize
data class Preview(
    @SerializedName("img_desc") val img_desc: String = "",
    @SerializedName("img_path") val img_path: String = ""
): Parcelable

// --- Prospectus & Curriculum Domain Models ---

@Keep
@Parcelize
data class CanonicalProgram(
    val code: String = "",
    val canonicalName: String = "",
    val aliases: List<String> = emptyList(),
    val department: String = "",
    val versions: List<ProspectusVersion> = emptyList()
) : Parcelable

@Keep
@Parcelize
data class ProspectusVersion(
    val academicYear: String = "",
    val curriculumVersion: String = "",
    val prospectusUuid: String = "",
    val versionNumber: Int = 1,
    val isDefault: Boolean = true
) : Parcelable

@Keep
@Parcelize
data class ProspectusMetadata(
    val programCode: String = "",
    val programName: String = "",
    val revision: String = "",
    val effectiveAy: String = "",
    val uuid: String = "",
    val versionNumber: Int = 1,
    val isOfflineCached: Boolean = false,
    val lastSyncedTimestamp: Long = 0L
) : Parcelable

@Keep
@Parcelize
data class Prospectus(
    val metadata: ProspectusMetadata = ProspectusMetadata(),
    val yearLevels: List<YearLevelCurriculum> = emptyList(),
    val totalCurriculumUnits: Double = 0.0
) : Parcelable

@Keep
@Parcelize
data class YearLevelCurriculum(
    val yearLevelName: String = "",
    val semesters: List<SemesterCurriculum> = emptyList()
) : Parcelable

@Keep
@Parcelize
data class SemesterCurriculum(
    val semesterName: String = "",
    val subjects: List<ProspectusSubject> = emptyList(),
    val totalUnitsDisplay: String = ""
) : Parcelable

@Keep
@Parcelize
data class ProspectusSubject(
    val code: String = "",
    val name: String = "",
    val units: String = "",
    val preRequisites: String = "",
    val isLab: Boolean = false,
    val isReligious: Boolean = false
) : Parcelable