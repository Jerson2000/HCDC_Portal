package com.jerson.hcdc_portal.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


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

    /* Material Dialog */
    public static MaterialAlertDialogBuilder Dialog(String sTitle, String sMessage, final Context context) {
        return new MaterialAlertDialogBuilder(context)
                .setTitle(sTitle)
                .setMessage(sMessage);
    }

    public static MaterialAlertDialogBuilder CustomDialog(String sTitle, final Context context, View customView) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle(sTitle);
        if (customView != null) {
            dialogBuilder.setView(customView);
        }

        return dialogBuilder;
    }
}
