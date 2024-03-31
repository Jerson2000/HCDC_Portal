package com.jerson.hcdc_portal.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.jerson.hcdc_portal.PortalApp
import okhttp3.FormBody
import okhttp3.Request

object HttpClients {
    fun isConnected(): Boolean {
        val connectivityManager = PortalApp.getAppContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
}


