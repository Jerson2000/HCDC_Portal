package com.jerson.hcdc_portal;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.jerson.hcdc_portal.ui.activity.CrashActivity;
import com.jerson.hcdc_portal.util.PreferenceManager;

import org.jsoup.nodes.Document;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;


public class PortalApp extends Application implements LifecycleObserver {
    private static final String TAG = "PortalApp";
    private static Context appContext;
    private static PreferenceManager preferenceManager;

    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        preferenceManager = new PreferenceManager(PortalApp.getAppContext());
        int themeMode = preferenceManager.getInteger(PortalApp.KEY_SETTINGS_THEME_MODE) != 0 ? preferenceManager.getInteger(PortalApp.KEY_SETTINGS_THEME_MODE) : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(themeMode);


        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, e) -> {


                    Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString(); // stack trace as a string
                    intent.putExtra("ex", stackTrace);
                    startActivity(intent);
                    Log.e(TAG, "uncaughtException: ", e);
                    System.exit(1);
                    CrashActivity.PENDING_ERROR = e;

                });

    }

    public static PreferenceManager getPreferenceManager() {
        if (preferenceManager == null) {
            preferenceManager = new PreferenceManager(PortalApp.getAppContext());
        }
        return preferenceManager;
    }

    public static void showToast(String msg) {
        Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
    }


    public static void parseUser(Document response) {
        boolean enrolled = response.body().text().contains("Officially Enrolled");

        String id = response.select(".app-sidebar__user-designation").text().replace("(", " ").replace(")", " ");
        String[] courseID = id.split(" ");
        String[] units = response.select(".row div b").eq(0).text().split(" ");

        if (enrolled)
            preferenceManager.putString(PortalApp.KEY_IS_ENROLLED, response.select(".row div b").eq(1).text());
        else
            preferenceManager.putString(PortalApp.KEY_IS_ENROLLED, response.select(".app-title > div > p").text());

        preferenceManager.putString(PortalApp.KEY_STUDENTS_UNITS, units[units.length - 1]);
        preferenceManager.putString(PortalApp.KEY_ENROLL_ANNOUNCE, response.select(".mybox-body > center > h5").text());
        preferenceManager.putString(PortalApp.KEY_STUDENT_ID, courseID[courseID.length - 1]);
        preferenceManager.putString(PortalApp.KEY_STUDENT_COURSE, courseID[0]);
        preferenceManager.putString(PortalApp.KEY_STUDENT_NAME, response.select(".app-sidebar__user-name").text());
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Nullable
    public static <T extends Serializable> T getSerializable(@Nullable Bundle bundle, @Nullable String key, @NonNull Class<T> clazz) {
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return bundle.getSerializable(key, clazz);
            } else {
                try {
                    return (T) bundle.getSerializable(key);
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }


    public static Context getAppContext() {
        return appContext;
    }


    // Preference
    public static final String KEY_SHARED = "studentPortal";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_IS_LOGIN = "isLogin";
    public static final String KEY_IS_ENROLLED = "isEnrolled";
    public static final String KEY_ENROLL_ANNOUNCE = "enrollAnnounce";
    public static final String KEY_STUDENT_ID = "studentID";
    public static final String KEY_STUDENT_NAME = "studentName";
    public static final String KEY_STUDENT_COURSE = "studentCourse";
    public static final String KEY_STUDENTS_UNITS = "units";

    public static final String KEY_SETTINGS_THEME_MODE = "themeMode";

    public static final String KEY_HTML_EVALUATION = "evaluationHTML";
    public static final String KEY_CSRF_TOKEN = "csrf_token";


    // App
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36";
    public static final String baseUrl = "https://studentportal.hcdc.edu.ph";
    public static final String loginUrl = "/login";
    public static final String loginPostUrl = "/loginPost";
    public static final String dashboardUrl = "/home";
    public static final String gradesUrl = "/grade_hed";
    public static final String evaluationsUrl = "/evaluation_hed";
    public static final String accountUrl = "/account_hed";
    public static final String enrollHistory = "/enrollmentHistory";
    public static final String subjectOffered = "/subject/p";
    public static final String subjectOfferedSearch = "/subject/s";
    public static final String lackingCredential = "/lacking";


    public static final String github = "https://github.com/Jerson2000/HCDC_Portal";


    public static final String[] SAD_EMOJIS = {"(っ °Д °;)っ", "(┬┬﹏┬┬)", "¯\\_(ツ)_/¯", "___*( ￣皿￣)/#____", "ಥ_ಥ", "(>ლ)"};
    public static final String[] HAPPY_EMOJIS = {"(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "(⌐■_■)", "✪ ω ✪", "( ﾉ ﾟｰﾟ)ﾉ", "d=====(￣▽￣*)b", "🤝🏻o((>ω< ))o"};


}
