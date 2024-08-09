package com.jerson.hcdc_portal.domain.model

data class SubjectOffered(
    val block_section: Any,
    val course: String,
    val description: String,
    val dissolved: Int,
    val enrolled: Int,
    val id: String,
    val ln: Int,
    val maximum: Int,
    val nop: Int,
    val page_fr: Int,
    val page_to: Int,
    val pages: Int,
    val pn: Int,
    val remaining_slot: Int,
    val so_no: String,
    val subject_code: String,
    val sysem: String,
    val units: Int
)