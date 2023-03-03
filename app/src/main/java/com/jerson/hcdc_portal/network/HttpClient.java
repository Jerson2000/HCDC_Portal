package com.jerson.hcdc_portal.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private HttpClient(Context context) {
        client = new OkHttpClient.Builder()
                .addInterceptor(new TimingInterceptor())
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))
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
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    handler.post(() -> listener.onFailure(new IOException("Unexpected code " + response)));
                    return;
                }
                ResponseBody body = response.body();
                String html = body.string();

                handler.post(() -> listener.onResponse(Jsoup.parse(html)));
            }
        });
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

                // Handle the final response
                if (response.isSuccessful()) {
                    // Success
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseData = body.string();
                        Document document = Jsoup.parse(responseData);
//                    System.out.println(document);
                        listener.onResponse(document);
                    }
                } else {
                    // Handle error
                }
            } catch (IOException e) {
//            e.printStackTrace();
                listener.onFailure(e);
            }
        });
    }


}
