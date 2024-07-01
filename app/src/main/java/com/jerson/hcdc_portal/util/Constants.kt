package com.jerson.hcdc_portal.util

object Constants {
    // Preference
    const val KEY_SHARED = "studentPortal"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"
    const val KEY_IS_LOGIN = "isLogin"
    const val KEY_IS_ENROLLED = "isEnrolled"
    const val KEY_ENROLL_ANNOUNCE = "enrollAnnounce"
    const val KEY_STUDENT_ID = "studentID"
    const val KEY_STUDENT_NAME = "studentName"
    const val KEY_STUDENT_COURSE = "studentCourse"
    const val KEY_STUDENTS_UNITS = "units"
    const val KEY_SETTINGS_THEME_MODE = "themeMode"
    const val KEY_HTML_EVALUATION = "evaluationHTML"
    const val KEY_CSRF_TOKEN = "csrf_token"
    const val KEY_IS_SESSION = "isSession"
    const val KEY_USER_AVATAR = "avatar"

    const val KEY_IS_SCHEDULE_LOADED = "scheduleLoaded"

    const val KEY_IS_GRADE_LOADED = "gradeLoaded"
    const val KEY_SELECT_GRADE_TERM = "gradeSelectedTerm"

    const val KEY_IS_ENROLL_HISTORY_LOADED = "enrollHistoryLoaded"
    const val KEY_SELECTED_ENROLL_HISTORY_TERM = "selectedEnrollHistoryTerm"

    const val KEY_IS_ACCOUNT_LOADED = "accountLoaded"
    const val KEY_SELECTED_ACCOUNT_TERM = "selectAccountTerm"


    // App
    const val userAgent =
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
    const val baseUrl = "https://studentportal.hcdc.edu.ph"
    const val loginUrl = "/login"
    const val loginPostUrl = "/loginPost"
    const val dashboardUrl = "/home"
    const val gradesUrl = "/grade_hed"
    const val evaluationsUrl = "/evaluation_hed"
    const val accountUrl = "/account_hed"
    const val enrollHistory = "/enrollmentHistory"
    const val subjectOffered = "/subject/p"
    const val subjectOfferedSearch = "/subject/s"
    const val lackingCredential = "/lacking"


    const val github = "https://github.com/Jerson2000/HCDC_Portal"
}