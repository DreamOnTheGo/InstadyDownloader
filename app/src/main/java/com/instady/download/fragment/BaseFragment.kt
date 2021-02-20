package com.instady.download.fragment

import android.content.DialogInterface

import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors.getColor
import com.google.android.material.snackbar.Snackbar
import com.instady.download.R


open class BaseFragment: Fragment() {
    private lateinit var alertDialog: androidx.appcompat.app.AlertDialog
    open fun showLoadingDialog() {
        alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireActivity()).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable())
        alertDialog.setCancelable(false)
        alertDialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK })
        alertDialog.show()
        alertDialog.setContentView(R.layout.alert_loading)
        alertDialog.setCanceledOnTouchOutside(false)
    }

    open fun dismissLoadingDialog() {
        if (alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    open fun showSnackBar(view: View, message: String){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).apply {
            setAction("OK") { this.dismiss() }
            this@BaseFragment.activity?.let { setActionTextColor(it.getColor(R.color.pink_700)) }
            show()
        }
    }
}