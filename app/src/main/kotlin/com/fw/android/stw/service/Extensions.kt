package com.fw.android.stw.service

import android.util.Log

/**
 * Created by fw on 10.12.16.
 */

fun <T> T.log(prefix: String = "--- ", suffix: String = "") {
    Log.i(prefix, this.toString() + suffix)
}