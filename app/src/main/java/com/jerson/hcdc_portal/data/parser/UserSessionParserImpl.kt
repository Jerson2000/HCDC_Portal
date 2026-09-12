package com.jerson.hcdc_portal.data.parser

import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionParserImpl @Inject constructor() : UserSessionParser {

    override fun parseSession(doc: Document): SessionStatus {
        val token = doc.select("meta[name=csrf-token]").attr("content").trim()
        val isLogin = doc.body().text().contains("CROSSIAN LOG-IN")
        val isNameEmpty = !doc.body().select(".app-sidebar__user-name").hasText()
        val isSessionExpired = isLogin || isNameEmpty

        return SessionStatus(
            csrfToken = token,
            isSessionExpired = isSessionExpired,
            isLoginPage = isLogin
        )
    }
}
