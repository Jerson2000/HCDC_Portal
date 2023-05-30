package com.jerson.hcdc_portal.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClient {
    private static HttpClient instance;
    private OkHttpClient client;
    private Handler handler;
    private Executor executor;
    private CacheControl cacheControl;

    private HttpClient() {
        File cacheDirectory = new File(PortalApp.getAppContext().getCacheDir(), "http-cache");
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(cacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .addInterceptor(new TimingInterceptor())
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(PortalApp.getAppContext())))
                .cache(cache)
                .build();
         cacheControl = new CacheControl.Builder()
                .maxAge(1, TimeUnit.HOURS)
                .maxStale(1, TimeUnit.DAYS)
                .build();
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }


    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PortalApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        }
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }


    public static synchronized HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    public void GET(String url, final OnHttpResponseListener<Document> listener) {
        if(isConnected()){
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(cacheControl)
                    .build();
            executor.execute(()->{
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Handle successful response
                        ResponseBody body = response.body();
                        String html = body.string();
                        handler.post(()-> listener.onResponseCode(response.code()));
                        handler.post(() -> listener.onResponse(Jsoup.parse(html)));

                    } else {
                        // Handle unsuccessful response
                        handler.post(()-> listener.onResponseCode(response.code()));
                    }
                } catch (IOException e) {
                    // Handle network or IO errors
                    handler.post(() -> listener.onFailure(e));
                }
            });
        }else{
            listener.onFailure(new IOException("No internet connection"));
        }



    }

    public void POST(String url, FormBody formBody, OnHttpResponseListener<Document> listener) {
        if(isConnected()){
            executor.execute(()->{
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    Response response = client.newCall(request).execute();

                    System.out.println(response.code()+" - "+response.message());

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .post(formBody)
                                    .build();

                            response = client.newCall(request).execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .build();

                            response = client.newCall(request).execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(()-> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            Document document = Jsoup.parse(responseData);
                            handler.post(()-> listener.onResponse(document));
                        }
                    } else {
                        // Handle error
//                    listener.onFailure(new onResponseException(response.code()));
                        handler.post(()-> listener.onResponseCode(finalResponse.code()));
                        handler.post(()-> listener.onFailure(new IOException("Unexpected code "+ finalResponse)));
                    }
                } catch (IOException e) {
//            e.printStackTrace();
                    handler.post(()-> listener.onFailure(e));
                }
            });
        }else{
            listener.onFailure(new IOException("No internet connection"));
        }

    }


    public void GET_Redirection(String url, final OnHttpResponseListener<Document> listener) {
        executor.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .cacheControl(cacheControl)
                        .build();

                Response response = client.newCall(request).execute();

                System.out.println(response.code() + " - " + response.message());

                while (response.isRedirect()) {
                    if(response.code() == 302){
                        request = request.newBuilder()
                                .url(response.header("Location"))
                                .build();

                        response = client.newCall(request).execute();
                    }
                }

                Response finalResponse = response;
                // Handle the final response
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseData = body.string();
                        Document document = Jsoup.parse(responseData);
                        handler.post(() -> listener.onResponse(document));
                        handler.post(()->listener.onResponseCode(finalResponse.code()));
                    }
                } else {
                    handler.post(() -> listener.onFailure(new IOException("Unexpected code " + finalResponse)));
                }
            } catch (IOException e) {
                handler.post(() -> listener.onFailure(e));
            }
        });

    }


}
