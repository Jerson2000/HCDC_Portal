package com.jerson.hcdc_portal.util

import android.view.View

/**
 * Centralized utility to convert raw network, server, database, and parsing exceptions into
 * clean, user-friendly, and actionable messages for better user experience.
 */
object ErrorMessageHelper {

    fun format(rawMessage: String?): String {
        if (rawMessage.isNullOrBlank() || rawMessage.trim().equals("null", ignoreCase = true)) {
            return "Something went wrong. Please try again."
        }

        val msg = rawMessage.lowercase()
        return when {
            // Preserve session expiration triggers so reLogon() works
            msg.contains("session end") -> rawMessage

            // Offline / DNS / Host resolution / Connection issues
            msg.contains("unable to resolve host") ||
            msg.contains("no address associated with hostname") ||
            msg.contains("failed to connect") ||
            msg.contains("network is unreachable") ||
            msg.contains("connectexception") ||
            msg.contains("noroutetohostexception") ||
            msg.contains("portunreachableexception") ||
            msg.contains("socketexception") ||
            msg.contains("unknownhostexception") -> {
                "You're offline. Please check your internet connection."
            }

            // Socket / Read timeouts
            msg.contains("timeout") ||
            msg.contains("timed out") ||
            msg.contains("time out") -> {
                "Connection timed out. Please check your internet connection and try again."
            }

            // SSL / Security handshake failures
            msg.contains("sslhandshake") ||
            msg.contains("certpathvalidator") ||
            msg.contains("sslpeerunverified") -> {
                "Secure connection could not be established. Please try again later."
            }

            // HTTP 5xx Server Errors
            msg.contains("500") ||
            msg.contains("502") ||
            msg.contains("503") ||
            msg.contains("504") ||
            msg.contains("internal server error") ||
            msg.contains("bad gateway") ||
            msg.contains("service unavailable") -> {
                "The school portal server is currently unavailable. Please try again later."
            }

            // HTTP 401 / 403 Authentication & Authorization
            msg.contains("401") ||
            msg.contains("403") ||
            msg.contains("unauthorized") ||
            msg.contains("forbidden") -> {
                "Your session has expired. Please sign in again."
            }

            // HTTP 404
            msg.contains("404") ||
            msg.contains("not found") -> {
                "The requested resource was not found on the portal."
            }

            // Parsing / Data format errors
            msg.contains("jsonsyntax") ||
            msg.contains("jsonexception") ||
            msg.contains("malformed") -> {
                "Unable to process server response. Please try again later."
            }

            // Clean up raw Java exception prefixes if any unhandled message remains
            msg.startsWith("java.io.ioexception:") -> {
                rawMessage.substringAfter("java.io.ioexception:").trim().ifBlank {
                    "A network communication error occurred. Please try again."
                }
            }
            msg.startsWith("java.lang.exception:") -> {
                rawMessage.substringAfter("java.lang.exception:").trim().ifBlank {
                    "Something went wrong. Please try again."
                }
            }

            else -> rawMessage
        }
    }

    fun formatThrowable(throwable: Throwable?): String {
        if (throwable == null) return "Something went wrong. Please try again."
        return format(throwable.localizedMessage ?: throwable.message ?: throwable.javaClass.simpleName)
    }
}

/**
 * Extension on Throwable to format into a user-friendly error message.
 */
fun Throwable.toUserFriendlyMessage(): String = ErrorMessageHelper.formatThrowable(this)

/**
 * Extension on SnackBarKt to display a user-friendly error message.
 */
fun SnackBarKt.snackBarFriendlyError(view: View, rawMessage: String?) {
    val friendly = ErrorMessageHelper.format(rawMessage)
    if (friendly.isNotBlank()) {
        snackBarLong(view, friendly)
    }
}
