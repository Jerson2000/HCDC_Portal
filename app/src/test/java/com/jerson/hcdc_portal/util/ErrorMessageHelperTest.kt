package com.jerson.hcdc_portal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMessageHelperTest {

    @Test
    fun `format returns offline message for UnknownHostException and DNS errors`() {
        val raw = "java.net.UnknownHostException: Unable to resolve host \"my.hcdc.edu.ph\": No address associated with hostname"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("You're offline. Please check your internet connection.", formatted)
    }

    @Test
    fun `format returns offline message for connection failure`() {
        val raw = "Failed to connect to my.hcdc.edu.ph/192.168.1.1:443"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("You're offline. Please check your internet connection.", formatted)
    }

    @Test
    fun `format returns offline message for unreachable network`() {
        val raw = "java.net.ConnectException: Network is unreachable"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("You're offline. Please check your internet connection.", formatted)
    }

    @Test
    fun `format returns timeout message for socket timeout`() {
        val raw = "java.net.SocketTimeoutException: timeout"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("Connection timed out. Please check your internet connection and try again.", formatted)
    }

    @Test
    fun `format returns server unavailable message for 500 error`() {
        val raw = "HTTP error 500: Internal Server Error"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("The school portal server is currently unavailable. Please try again later.", formatted)
    }

    @Test
    fun `format returns server unavailable message for 502 error`() {
        val raw = "HTTP error 502: Bad Gateway"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals("The school portal server is currently unavailable. Please try again later.", formatted)
    }

    @Test
    fun `format preserves session end for reLogon detection`() {
        val raw = "session end - 200"
        val formatted = ErrorMessageHelper.format(raw)
        assertEquals(raw, formatted)
        assertTrue(formatted.contains("session end"))
    }

    @Test
    fun `format handles null and empty messages gracefully`() {
        assertEquals("Something went wrong. Please try again.", ErrorMessageHelper.format(null))
        assertEquals("Something went wrong. Please try again.", ErrorMessageHelper.format(""))
        assertEquals("Something went wrong. Please try again.", ErrorMessageHelper.format("null"))
    }

    @Test
    fun `format returns session expired message for 401 and 403`() {
        assertEquals("Your session has expired. Please sign in again.", ErrorMessageHelper.format("HTTP error 401: Unauthorized"))
        assertEquals("Your session has expired. Please sign in again.", ErrorMessageHelper.format("HTTP error 403: Forbidden"))
    }

    @Test
    fun `format returns not found message for 404`() {
        assertEquals("The requested resource was not found on the portal.", ErrorMessageHelper.format("HTTP error 404: Not Found"))
    }

    @Test
    fun `format returns secure connection message for SSL handshake failures`() {
        val raw = "javax.net.ssl.SSLHandshakeException: Handshake failed"
        assertEquals("Secure connection could not be established. Please try again later.", ErrorMessageHelper.format(raw))
    }

    @Test
    fun `formatThrowable works with standard throwables`() {
        val ex = java.net.UnknownHostException("Unable to resolve host \"my.hcdc.edu.ph\": No address associated with hostname")
        assertEquals("You're offline. Please check your internet connection.", ErrorMessageHelper.formatThrowable(ex))
        assertEquals("You're offline. Please check your internet connection.", ex.toUserFriendlyMessage())
    }
}
