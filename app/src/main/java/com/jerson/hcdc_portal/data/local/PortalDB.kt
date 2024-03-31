package com.jerson.hcdc_portal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jerson.hcdc_portal.data.local.dao.SubjectDao
import com.jerson.hcdc_portal.domain.model.Subject

@Database(entities = [Subject::class], version = 1, exportSchema = false)
abstract class PortalDB : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao

}