package com.jerson.hcdc_portal.util;


import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarUtil {
    public static Snackbar SnackBarShort(View view,String msg){
        return Snackbar.make(view,msg,Snackbar.LENGTH_SHORT);
    }

    public static Snackbar SnackBarLong(View view,String msg){
        return Snackbar.make(view,msg,Snackbar.LENGTH_LONG);
    }

    public static Snackbar SnackBarCustomDuration(View view,String msg,int duration){
        return Snackbar.make(view,msg,duration);
    }

    public static Snackbar SnackBarIndefiniteDuration(View view,String msg){
        return Snackbar.make(view,msg,Snackbar.LENGTH_INDEFINITE);
    }
}
