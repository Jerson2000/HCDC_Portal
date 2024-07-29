package com.jerson.hcdc_portal.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboard() {
    val currentFocus = currentFocus
    if (currentFocus != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.ime())
        } else {
            // For Android 10 and below
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}