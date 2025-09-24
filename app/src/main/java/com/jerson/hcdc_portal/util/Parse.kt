package com.jerson.hcdc_portal.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.util.Constants.KEY_CUSTOM_PROFILE_VALUE
import com.jerson.hcdc_portal.util.Constants.KEY_ENROLL_ANNOUNCE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_CUSTOM_PROFILE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_ENROLLED
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_COURSE
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_ID
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_NAME
import com.jerson.hcdc_portal.util.Constants.KEY_USER_AVATAR
import org.jsoup.nodes.Document
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


fun sessionParse(preference: AppPreference, doc: Document): Boolean {
    val token = doc.select("meta[name=csrf-token]").attr("content")
    val isLogin = doc.body().text().contains("CROSSIAN LOG-IN")
    val isNameEmpty = doc.body().select(".app-sidebar__user-name").hasText().not()

    preference.setStringPreference(Constants.KEY_CSRF_TOKEN, token)
    preference.setBooleanPreference(Constants.KEY_IS_SESSION, isNameEmpty)
    Log.e("HUHU-PARSE", "sessionParse: isLoginPage:$isLogin\tisNameEmpty:$isNameEmpty", )
    return isLogin or isNameEmpty
}

/***
 * @param isGrade - 0 = enroll history, 1 = grades, 2 = accounts
 * @param doc = HTML Document
 */
fun termLinksParse(doc: Document, isGrade: Int): List<Term> {
    val list = mutableListOf<Term>()
    val items = doc.select("main.app-content select.select2-multiple.form-control option")

    for (i in 0 until items.size) {

        val item = items[i]
        val term = item.text()
        val link = item.attr("value")

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
    val name = doc.select(".app-sidebar__user-name").text().lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { x-> x.uppercase() } }
    val id = doc.select(".app-sidebar__user-designation").text().replace(Regex("\\(\\)"), "").split(" ")[1]
    val course = doc.select(".app-sidebar__user-designation").text().replace(Regex("\\(\\)"), "").split(" ")[0]
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

@OptIn(ExperimentalEncodingApi::class)
fun userAvatar(pref:AppPreference):Any{
    val avatar = pref.getStringPreference(KEY_USER_AVATAR).substringAfter("base64,")
    return  if(pref.getBooleanPreference(KEY_IS_CUSTOM_PROFILE)){
        pref.getIntPreference(KEY_CUSTOM_PROFILE_VALUE)
    }else{
        if(!avatar.contains("logo")){
            Base64.decode(avatar.toByteArray(), 0, avatar.toByteArray().size)
        }else
            R.drawable.logo
    }
}


// reusable function getParcelableArrayList
inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        // Use the updated getParcelableArrayList() method with the Class object parameter
        getParcelableArrayList(key, T::class.java)
    } else {
        // Use the older, deprecated getParcelableArrayList() method
        @Suppress("DEPRECATION")
        getParcelableArrayList(key)
    }
}

// Reusable function to get a Parcelable object
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        // Use the updated getParcelable() method with the Class object parameter
        getParcelable(key, T::class.java)
    } else {
        // Use the older, deprecated getParcelable() method
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}