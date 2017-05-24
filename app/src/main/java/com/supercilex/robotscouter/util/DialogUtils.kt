package com.supercilex.robotscouter.util

import android.support.v7.app.AlertDialog

inline fun AlertDialog.Builder.createAndListen(crossinline listener: AlertDialog.() -> Unit): AlertDialog =
        create().apply { setOnShowListener { (it as AlertDialog).listener() } }