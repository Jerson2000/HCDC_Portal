package com.jerson.hcdc_portal.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
*  To measure the execution time of an request
*
* */
public class TimingInterceptor implements Interceptor {
    private static final String TAG = "TimingInterceptor";
    @Override
    public Response intercept(Chain chain) throws IOException {
        long startTime = System.nanoTime();

        Request request = chain.request();
        Response response = chain.proceed(request);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // convert to milliseconds

        Log.d(TAG, "Request took: " + duration + " ms");

        return response;
    }
}