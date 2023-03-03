package com.jerson.hcdc_portal.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
*  To measure the execution time of an request
*
* */
public class TimingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        long startTime = System.nanoTime();

        Request request = chain.request();
        Response response = chain.proceed(request);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // convert to milliseconds

        System.out.println("Request took: " + duration + " ms");

        return response;
    }
}