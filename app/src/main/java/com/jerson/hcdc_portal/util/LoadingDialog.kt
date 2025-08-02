package com.jerson.hcdc_portal.util

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.jerson.hcdc_portal.databinding.DialogLoadingBinding


class LoadingDialog(context: Context) : AlertDialog(context, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog) {
    private var binding:DialogLoadingBinding = DialogLoadingBinding.inflate(LayoutInflater.from(context))

    init {
        setView(binding.root)
        /*setCancelable(false)*/
        setCanceledOnTouchOutside(false)
    }
    override fun show(){
      if(isShowing){
         super.dismiss()
      }else{
          super.show()
      }
    }

    override fun dismiss() {
        if (isShowing) {
            super.dismiss()
        }
    }


}