package com.framgia.oleo.utils.extension


import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun View.show() {
  visibility = View.VISIBLE
}

fun View.hide() {
  visibility = View.INVISIBLE
}

fun View.gone() {
  visibility = View.GONE
}

fun View.isVisible(): Boolean {
  return visibility == View.VISIBLE
}

fun Context.showToast(message: CharSequence) = Toast.makeText(this, message,
    Toast.LENGTH_SHORT).show()

fun View.showSnackBar(message: CharSequence) = Snackbar.make(this, message,
    Snackbar.LENGTH_SHORT).show()

fun createDialog(context: Context, view: View): AlertDialog {
  return AlertDialog.Builder(context)
      .setView(view)
      .create()
}
