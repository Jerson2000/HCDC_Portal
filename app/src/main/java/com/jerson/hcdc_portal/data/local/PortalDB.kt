package com.jerson.hcdc_portal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jerson.hcdc_portal.data.local.dao.AccountDao
import com.jerson.hcdc_portal.data.local.dao.EnrollHistoryDao
import com.jerson.hcdc_portal.data.local.dao.GradeDao
import com.jerson.hcdc_portal.data.local.dao.ScheduleDao
import com.jerson.hcdc_portal.data.local.dao.TermDao
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.model.Term

@Database(
    entities = [
        Account::class,
        EnrollHistory::class,
        Grade::class,
        Schedule::class,
        Term::class
    ],
    version = 1, exportSchema = false
)
abstract class PortalDB : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun accountDao(): AccountDao
    abstract fun gradeDao(): GradeDao
    abstract fun enrollHistoryDao(): EnrollHistoryDao
    abstract fun termDao(): TermDao

}