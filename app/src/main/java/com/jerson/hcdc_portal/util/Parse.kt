package com.jerson.hcdc_portal.util

import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.util.Constants.KEY_ENROLL_ANNOUNCE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_ENROLLED
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_COURSE
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_ID
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_NAME
import org.jsoup.nodes.Document


fun sessionParse(preference: AppPreference, doc: Document): Boolean {
    val token = doc.select("meta[name=csrf-token]").attr("content")
    val isLogin = doc.body().text().contains("CROSSIAN LOG-IN")
    preference.setStringPreference(Constants.KEY_CSRF_TOKEN, token)
    preference.setBooleanPreference(Constants.KEY_IS_SESSION, isLogin)
    return isLogin
}

/***
 * @param isGrade - 0 = enroll history, 1 = grades, 2 = accounts
 * @param doc = HTML Document
 */
fun termLinksParse(doc: Document, isGrade: Int): List<Term> {
    val list = mutableListOf<Term>()
    val items = doc.select("main.app-content ul li.nav-item")

    for (i in 1 until items.size) {

        val item = items[i]
        val term = item.select("a.nav-link").text()
        val link = item.select("a.nav-link").attr("href")

        list.add(
            Term(
                0,
                link,
                term,
                isGrade
            )
        )
    }
    return list.sortedByDescending { it.term }
}

fun userParse(doc: Document, pref: AppPreference) {

    val units = doc.select("div.col-sm-9 > b").text().replace("Total Units: ", "")
    val name = doc.select(".app-sidebar__user-name").text().lowercase()
    val id = doc.select("app-sidebar__user-designation").text().replace(Regex("\\(\\)"), "")[1].toString()
    val course = doc.select("app-sidebar__user-designation").text().replace(Regex("\\(\\)"), "")[0].toString()
    val enrollAnnounce = doc.select(".mybox-body > center > h5").text()
    val isEnrolled = doc.body().text().contains("Officially Enrolled")
    val enrolled = if(isEnrolled)
        "Officially Enrolled."
    else
        "Not enrolled this sem."
    pref.setStringPreference(KEY_STUDENTS_UNITS,units)
    pref.setStringPreference(KEY_STUDENT_NAME,name)
    pref.setStringPreference(KEY_STUDENT_ID,id)
    pref.setStringPreference(KEY_STUDENT_COURSE,course)
    pref.setStringPreference(KEY_ENROLL_ANNOUNCE,enrollAnnounce)
    pref.setStringPreference(KEY_IS_ENROLLED,enrolled)

}