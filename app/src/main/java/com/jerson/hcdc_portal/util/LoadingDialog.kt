package com.jerson.hcdc_portal.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.jerson.hcdc_portal.databinding.DialogLoadingBinding

class LoadingDialog(private val hostContext: Context) :
    AlertDialog(hostContext, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog) {
    private var binding: DialogLoadingBinding = DialogLoadingBinding.inflate(LayoutInflater.from(hostContext))

    init {
        setView(binding.root)
        setCanceledOnTouchOutside(false)
    }

    override fun show() {
        val activity = hostContext as? Activity
        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            return
        }
        if (!isShowing) {
            try {
                super.show()
            } catch (_: Exception) { }
        }
    }

    override fun dismiss() {
        if (isShowing) {
            try {
                super.dismiss()
            } catch (_: Exception) { }
        }
    }
}