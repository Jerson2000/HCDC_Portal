package com.jerson.hcdc_portal.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppConstants {

    //Global Variable
    public static String msg;
    public static List<String> error = new ArrayList<>();

    // Preference
    public static final String KEY_SHARED = "student";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_STUDENT_ID = "studentID";
    public static final Boolean KEY_IS_LOGIN = false;



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

}
