package com.jerson.hcdc_portal.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserSessionParserTest {

    private lateinit var parser: UserSessionParserImpl

    @Before
    fun setUp() {
        parser = UserSessionParserImpl()
    }

    @Test
    fun parseSession_activeSession_returnsValidStatus() {
        val html = """
            <html>
            <head>
                <meta name="csrf-token" content="secret-csrf-token-12345">
            </head>
            <body>
                <div class="app-sidebar__user-name">JUAN DELA CRUZ</div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val status = parser.parseSession(doc)

        assertEquals("secret-csrf-token-12345", status.csrfToken)
        assertFalse(status.isSessionExpired)
        assertFalse(status.isLoginPage)
    }

    @Test
    fun parseSession_loginRedirect_detectsExpiredSession() {
        val html = """
            <html>
            <head>
                <meta name="csrf-token" content="new-token-login">
            </head>
            <body>
                <h1>CROSSIAN LOG-IN</h1>
                <form action="/loginPost" method="POST">
                    <input type="text" name="email" />
                </form>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val status = parser.parseSession(doc)

        assertEquals("new-token-login", status.csrfToken)
        assertTrue(status.isSessionExpired)
        assertTrue(status.isLoginPage)
    }

    @Test
    fun parseSession_missingUserName_detectsExpiredSession() {
        val html = """
            <html>
            <head>
                <meta name="csrf-token" content="token-empty">
            </head>
            <body>
                <div class="app-sidebar__user-name"></div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val status = parser.parseSession(doc)

        assertTrue(status.isSessionExpired)
    }
}
