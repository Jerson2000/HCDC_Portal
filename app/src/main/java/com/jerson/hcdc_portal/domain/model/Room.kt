package com.jerson.hcdc_portal.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Room(
    val building: List<Building>
)
@Parcelize
data class Building(
    val floor_desc: String,
    val floors: List<Floor>,
    val history: String,
    val id: String,
    val img_path: String,
    val located: String,
    val name: String
): Parcelable

@Parcelize
data class Floor(
    val floor_no: Int,
    val rooms: List<Rooms>
): Parcelable
@Parcelize
data class Rooms(
    val desc: String,
    val id: String,
    val preview: List<Preview>,
    val room_no: String
): Parcelable
@Parcelize
data class Preview(
    val img_desc:String,
    val img_path: String
): Parcelable