package com.fw.android.stw.service

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by fw on 07.12.16.
 */
val FDATE = SimpleDateFormat("HH:mm:ss")
val FTIME = SimpleDateFormat("mm:ss.SSS")

fun formatDate(start: Long) = FDATE.format(Date(start))

fun formatTime(time: Long) = FTIME.format(Date(time))

fun formatDateAndTime(start: Long, time: Long) = formatDate(start) + "\t" + formatTime(time)

fun formatSTW(stw: STW) = formatDate(stw.start) + "\t" + formatTime(stw.time)



