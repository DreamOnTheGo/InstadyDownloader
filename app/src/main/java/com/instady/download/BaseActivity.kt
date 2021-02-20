package com.instady.download

import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


open class BaseActivity : AppCompatActivity() {
    private lateinit var alertDialog: AlertDialog
    open fun showLoadingDialog() {
        alertDialog = AlertDialog.Builder(this).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable())
        alertDialog.setCancelable(false)
        alertDialog.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK }
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
            setActionTextColor(getColor(R.color.pink_700))
            show()
        }
    }
}