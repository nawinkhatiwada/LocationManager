package com.androidbolts.library.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.androidbolts.library.R

inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}

fun showLoadingDialog(
    context:Context,
    message: String?,
    title: String? = "",
    cancelable: Boolean = false,
    onPositiveButtonClicked:() -> Unit): AlertDialog? {
    val builder = AlertDialog.Builder(context)
    val view = LayoutInflater.from(context).inflate(R.layout.progress_layout, null)
    val tvTitle = view.findViewById<TextView>(R.id.title)
    tvTitle.visibility = View.GONE
    if (!title.isNullOrEmpty()) {
        tvTitle.text = title
        tvTitle.visibility = View.VISIBLE
    }
    view.findViewById<TextView>(R.id.message).text = message
    builder.setPositiveButton("Retry") { _, _->
        onPositiveButtonClicked()
    }
    builder.setView(view)
    val dialog = builder.create()
    dialog.setCancelable(cancelable)
    return dialog
}