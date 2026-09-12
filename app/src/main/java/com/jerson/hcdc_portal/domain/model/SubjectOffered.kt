package com.jerson.hcdc_portal.domain.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubjectOffered(
    @SerializedName("block_section") val block_section: Any? = null,
    @SerializedName("course") val course: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("dissolved") val dissolved: Int = 0,
    @SerializedName("enrolled") val enrolled: Int = 0,
    @SerializedName("id") val id: String = "",
    @SerializedName("ln") val ln: Int = 0,
    @SerializedName("maximum") val maximum: Int = 0,
    @SerializedName("nop") val nop: Int = 0,
    @SerializedName("page_fr") val page_fr: Int = 0,
    @SerializedName("page_to") val page_to: Int = 0,
    @SerializedName("pages") val pages: Int = 0,
    @SerializedName("pn") val pn: Int = 0,
    @SerializedName("remaining_slot") val remaining_slot: Int = 0,
    @SerializedName("so_no") val so_no: String = "",
    @SerializedName("subject_code") val subject_code: String = "",
    @SerializedName("sysem") val sysem: String = "",
    @SerializedName("units") val units: Int = 0
)