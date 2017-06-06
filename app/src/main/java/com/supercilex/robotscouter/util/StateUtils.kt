package com.supercilex.robotscouter.util

import android.os.Bundle

val TAB_KEY = "tab_key"

fun getTabKeyBundle(key: String?) = Bundle().apply { putString(TAB_KEY, key) }

fun getTabKey(bundle: Bundle): String? = bundle.getString(TAB_KEY)
