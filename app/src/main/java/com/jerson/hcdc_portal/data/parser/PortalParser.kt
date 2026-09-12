package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.model.Term
import org.jsoup.nodes.Document

data class UserProfile(
    val name: String,
    val units: String,
    val course: String,
    val studentId: String,
    val enrollAnnounce: String,
    val isEnrolled: String,
    val avatarUrl: String? = null
)

data class SessionStatus(
    val csrfToken: String,
    val isSessionExpired: Boolean,
    val isLoginPage: Boolean
)

interface ScheduleParser {
    fun parseSchedules(doc: Document): List<Schedule>
    fun parseUserProfile(doc: Document): UserProfile
}

interface GradeParser {
    fun parseGrades(doc: Document, termId: Int): List<Grade>
    fun parseTerms(doc: Document): List<Term>
}

interface AccountParser {
    fun parseAccounts(doc: Document, termId: Int): List<Account>
    fun parseTerms(doc: Document): List<Term>
}

interface EnrollHistoryParser {
    fun parseEnrollHistory(doc: Document, termId: Int): List<EnrollHistory>
    fun parseTerms(doc: Document): List<Term>
}

interface UserSessionParser {
    fun parseSession(doc: Document): SessionStatus
}
