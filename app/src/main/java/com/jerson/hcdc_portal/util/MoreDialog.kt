package com.jerson.hcdc_portal.util

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.DialogMoreBinding
import com.jerson.hcdc_portal.presentation.chat.ChatGPT
import com.jerson.hcdc_portal.presentation.lacking.Lacking

class MoreDialog : DialogFragment() {
    private lateinit var binding: DialogMoreBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogMoreBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(requireContext())

        binding.btnVMCoreVal.setOnClickListener {
            this.dismiss()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Vision-Mission-Core Values")
                .setMessage(R.string.vm_core_values)
                .show()
        }

        binding.btnLack.setOnClickListener {
            startActivity(Intent(requireContext(), Lacking::class.java))
        }
        binding.btnChatGPT.setOnClickListener {
            startActivity(Intent(requireContext(), ChatGPT::class.java))
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("More options")
            .setView(binding.root)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}