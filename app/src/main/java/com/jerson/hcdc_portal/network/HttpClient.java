package com.jerson.hcdc_portal.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.util.onResponseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
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

    private HttpClient(Context context) {
        File cacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(cacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .addInterceptor(new TimingInterceptor())
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))
                .cache(cache)
                .build();
         cacheControl = new CacheControl.Builder()
                .maxAge(1, TimeUnit.HOURS)
                .maxStale(1, TimeUnit.DAYS)
                .build();
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized HttpClient getInstance(Context context) {
        if (instance == null) {
            instance = new HttpClient(context);
        }
        return instance;
    }

    public void GET(String url, final OnHttpResponseListener<Document> listener) {
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



//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                handler.post(() -> listener.onFailure(e));
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    handler.post(() -> listener.onFailure(new IOException("Unexpected code " + response)));
//                    handler.post(()-> listener.onResponseCode(response.code()));
//                    return;
//                }
//                ResponseBody body = response.body();
//                String html = body.string();
//                handler.post(()-> listener.onResponseCode(response.code()));
//                handler.post(() -> listener.onResponse(Jsoup.parse(html)));
//            }
//        });
    }

    public void POST(String url, FormBody formBody, OnHttpResponseListener<Document> listener) {
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
    }


}
