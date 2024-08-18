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


