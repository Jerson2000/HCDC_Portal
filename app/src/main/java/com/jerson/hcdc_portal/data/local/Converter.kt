package com.jerson.hcdc_portal.data.local

import androidx.room.TypeConverter
import androidx.room.TypeConverters

@TypeConverters
class Converter {

    @TypeConverter
    fun fromAnyToString(attribute: Any?): String {
        return attribute?.toString() ?: ""
    }

    @TypeConverter
    fun fromStringToAny(attribute: String?): Any {
        return attribute ?: ""
    }
}