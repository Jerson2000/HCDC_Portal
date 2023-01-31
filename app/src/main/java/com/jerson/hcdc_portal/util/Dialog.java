package com.jerson.hcdc_portal.util;

import android.app.AlertDialog;
import android.content.Context;


public class Dialog {
    public static void AlertDialog(String sTitle, String sMessage, final Context myContext)
    {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(myContext);

        builder
                .setTitle(sTitle)
                .setMessage(sMessage)
                .setCancelable(true)
                .show();
    }
}
