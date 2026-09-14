package com.jerson.hcdc_portal.util

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

object SnackBarKt {
    fun snackBarShort(view: View, msg: String?) {
        val formatted = ErrorMessageHelper.format(msg)
        if (formatted.isNotBlank()) {
            Snackbar.make(view, formatted, Snackbar.LENGTH_SHORT).apply {
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.maxLines = 4
            }.show()
        }
    }

    fun snackBarLong(view: View, msg: String?) {
        val formatted = ErrorMessageHelper.format(msg)
        if (formatted.isNotBlank()) {
            Snackbar.make(view, formatted, Snackbar.LENGTH_LONG).apply {
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.maxLines = 4
            }.show()
        }
    }

    fun snackBarCustomDuration(view: View, msg: String?, duration: Int) {
        val formatted = ErrorMessageHelper.format(msg)
        if (formatted.isNotBlank()) {
            Snackbar.make(view, formatted, duration).apply {
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.maxLines = 4
            }.show()
        }
    }

    fun snackBarIndefinite(view: View, msg: String?) {
        val formatted = ErrorMessageHelper.format(msg)
        if (formatted.isNotBlank()) {
            Snackbar.make(view, formatted, Snackbar.LENGTH_INDEFINITE).apply {
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.maxLines = 4
            }.show()
        }
    }
}