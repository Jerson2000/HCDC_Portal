package com.jerson.hcdc_portal.util

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.R

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

fun extractBuilding(str:String):String{
    val x = str.filter { it.isLetter() }
    val xx = "Building:"
    when(x.lowercase()){
        "sa" ->{
            return "$xx Sta. Ana Hall"
        }
        "ch" ->{
            return "$xx Capalla Hall"
        }
        "gh" ->{
            return "$xx Gmeiner Hall"
        }
        "th"->{
            return "$xx Thibault Hall"
        }
        "ph"->{
            return "$xx Palma Gil Hall"
        }
        "mh"->{
            return "$xx Mabutas Hall"
        }
        "j"->{
            return "$xx St. John Paul & St. John XXIII"
        }
        else ->{
            return "Building"
        }
    }
}


// Check if the recyclerView is at Bottom
val RecyclerView.isAtBottom: Boolean
    get() {
        val layoutManager = layoutManager as? LinearLayoutManager
        return layoutManager?.let {
            val lastVisible = it.findLastCompletelyVisibleItemPosition()
            val total = adapter?.itemCount ?: 0
            lastVisible >= total - 1
        } ?: false
    }

//
fun parseTerm(term: String): Pair<Int, Int> {
    val semester = when {
        term.startsWith("1st", ignoreCase = true) -> 1
        term.startsWith("2nd", ignoreCase = true) -> 2
        term.startsWith("Summer", ignoreCase = true) -> 3
        else -> Int.MAX_VALUE // Unknown/lowest priority
    }

    // Extract academic year (e.g., "2023" from "2023-2024")
    val yearStart = term.substringAfterLast(" ").substringBefore("-").toIntOrNull() ?: 0

    return semester to yearStart
}

fun convertToReadableTerm(term: String?): String {

    if (term.isNullOrEmpty()) {
        return "Unknown Term"
    }

    val semesterNumber = term.substringAfter("_", "").toIntOrNull()
    if (semesterNumber == null || term.length < 8) {
        return "Unknown Term"
    }

    val yearRange = term.substring(0, 8)
    val yearStart = yearRange.substring(0, 4)  // Extract start year, e.g., "2025"
    val yearEnd = yearRange.substring(4, 8)    // Extract end year, e.g., "2026"

    return when (semesterNumber) {
        1 -> "1st Semester $yearStart-$yearEnd"
        2 -> "2nd Semester $yearStart-$yearEnd"
        3 -> "Summer $yearStart-$yearEnd"
        else -> "Unknown Term"
    }
}



fun downloadApk(url: String) {
    val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val urlX = url.toUri()
    val request = DownloadManager.Request(urlX)
        .setTitle("${appContext.getString(R.string.app_name)} ${urlX.lastPathSegment?.substringAfter("app-")}")
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, urlX.lastPathSegment)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setMimeType("application/vnd.android.package-archive")
    downloadManager.enqueue(request)
}


// check and request permission - extension of an activity
// android 10 below
const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
fun Activity.checkAndRequestPermissions() {
    val permissionsToRequest = mutableListOf<String>()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (!checkPermission()) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }
}
// check permission
fun Activity.checkPermission():Boolean{
    return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}


