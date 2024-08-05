package com.jerson.hcdc_portal.domain.model

data class Room(
    val building: List<Building>
)

data class Building(
    val floor_desc: String,
    val floors: List<Floor>,
    val history: String,
    val id: String,
    val img_path: String,
    val located: String,
    val name: String
)

data class Floor(
    val floor_no: Int,
    val rooms: List<Rooms>
)

data class Rooms(
    val desc: String,
    val id: String,
    val preview: List<Preview>,
    val room_no: String
)

data class Preview(
    val img_path: String
)