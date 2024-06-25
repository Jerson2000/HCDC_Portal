package com.jerson.hcdc_portal.util

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.domain.model.Term

class TermSelectionDialog(private val context: Context) : MaterialAlertDialogBuilder(
    context
) {
    private val items = mutableListOf<Term>()
    private var dialog: AlertDialog? = null

    fun setTerms(terms: List<Term>) {
        items.clear()
        items.addAll(terms)
    }

    fun showDialog(item: (Term) -> Unit) {
            setTitle("Select term")
            setItems(items.map { it.term }.toTypedArray()) { _, which ->
                item(items[which])
            }
            setNegativeButton("OK") { _, _ ->
                dismiss()
            }
            dialog = create()

        if (!dialog?.isShowing!!) {
            dialog?.show()
        }else{
            dialog?.dismiss()
        }
    }

    private fun dismiss() {

        if (dialog?.isShowing!!) {
            dialog?.dismiss()
        }
    }


}