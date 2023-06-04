package com.jerson.hcdc_portal;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.appcompat.app.AppCompatDelegate;


public class PortalApp extends Application {
    private static Context appContext;

    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        /*AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);*/

    }


    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PortalApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }



    public static Context getAppContext() {
        return appContext;
    }


    // Preference
    public static final String KEY_SHARED = "studentPortal";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_STUDENT_ID = "studentID";
    public static final String KEY_IS_LOGIN = "isLogin";
    public static final String KEY_IS_ENROLLED = "isEnrolled";
    public static final String KEY_ENROLL_ANNOUNCE = "enrollAnnounce";
    public static final String KEY_STUDENT_NAME = "studentName";
    public static final String KEY_STUDENT_COURSE = "studentCourse";


    // App
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36";
    public static final String baseUrl = "http://studentportal.hcdc.edu.ph";
    public static final String loginUrl = "/login";
    public static final String loginPostUrl = "/loginPost";
    public static final String dashboardUrl = "/home";
    public static final String gradesUrl = "/grade_hed";
    public static final String evaluationsUrl = "/evaluation_hed";
    public static final String accountUrl = "/account_hed";
    public static final String enrollHistory = "/enrollmentHistory";

    public static final String[] SAD_EMOJIS = {"(っ °Д °;)っ","(┬┬﹏┬┬)","¯\\_(ツ)_/¯","___*( ￣皿￣)/#____","ಥ_ಥ","(>ლ)"};
    public static final String[] HAPPY_EMOJIS = {"(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧","(⌐■_■)","✪ ω ✪","( ﾉ ﾟｰﾟ)ﾉ","d=====(￣▽￣*)b","🤝🏻o((>ω< ))o"};


}
