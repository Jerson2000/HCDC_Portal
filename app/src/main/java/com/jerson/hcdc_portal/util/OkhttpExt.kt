package com.jerson.hcdc_portal.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response

suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    val callback = ContinuationCallCallback(this, continuation)
    enqueue(callback)
    continuation.invokeOnCancellation(callback)
}

val Response.mimeType: String?
    get() = header("content-type")?.takeUnless { it.isEmpty() }

val Response.contentDisposition: String?
    get() = header("Content-Disposition")

fun Headers.Builder.mergeWith(other: Headers, replaceExisting: Boolean): Headers.Builder {
    for ((name, value) in other) {
        if (replaceExisting || this[name] == null) {
            this[name] = value
        }
    }
    return this
}

fun Response.copy() = newBuilder()
    .body(peekBody(Long.MAX_VALUE))
    .build()

fun isConnected(@ApplicationContext context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities != null && networkCapabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}

fun postRequest(url: String, formBody: FormBody): Request {
    return Request.Builder().url(url).post(formBody).build()
}

fun getRequest(url: String): Request {
    return Request.Builder().url(url).build()
}