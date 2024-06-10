package com.jerson.hcdc_portal.util

import com.jerson.hcdc_portal.domain.model.Term
import org.jsoup.nodes.Document


fun sessionParse(preference: AppPreference,doc: Document):Boolean{
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
fun termLinksParse(doc:Document,isGrade:Int):List<Term>{
    val list = mutableListOf<Term>()
    val items = doc.select("main.app-content ul li.nav-item")

    for (x in items){
        list.add(
            Term(
                0,
                x.select("a.nav-link").attr("href"),
                x.select("a.nav-link").text(),
                isGrade
            )
        )
    }
    return  list
}