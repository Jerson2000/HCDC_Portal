package com.jerson.hcdc_portal.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TimingInterceptorKt : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.nanoTime()

        val request = chain.request()
        val response = chain.proceed(request)
        val reqUrl = request.url.toString()

        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1000000


        Log.d(
            this.toString(),
            "Request took: $duration ms [URL Request]: $reqUrl"
        )

        return response
    }
}