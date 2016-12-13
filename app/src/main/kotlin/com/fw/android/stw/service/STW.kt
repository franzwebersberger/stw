package com.fw.android.stw.service

/**
 * Created by fw on 07.12.16.
 */
data class STW(val start: Long, val time: Long) : Comparable<STW> {
    val fmt: String = formatDateAndTime(start, time)

    override fun compareTo(other: STW): Int {
        val dTime = time - other.time
        val dStart = start - other.start
        return if (dTime != 0L) dTime.toInt() else dStart.toInt()
    }

}