package com.jerson.hcdc_portal.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarKt {
    fun snackBarShort(view: View, msg:String){
        Snackbar.make(view,msg, Snackbar.LENGTH_SHORT).show()
    }
    fun snackBarLong(view: View, msg:String){
        Snackbar.make(view,msg, Snackbar.LENGTH_LONG).show()
    }
    fun snackBarCustomDuration(view: View, msg:String,duration:Int){
        Snackbar.make(view,msg, duration).show()
    }
    fun snackBarIndefinite(view: View, msg:String){
        Snackbar.make(view,msg, Snackbar.LENGTH_INDEFINITE).show()
    }


}