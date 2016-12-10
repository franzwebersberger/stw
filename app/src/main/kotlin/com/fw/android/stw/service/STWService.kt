package com.fw.android.stw.service

import android.util.Log
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Created by fw on 07.12.16.
 */
object STWService {
    private val LOGTAG = "-- STWService --"

    private var running = false
    private var start: Long = 0

    private val stws: Deque<STW> = LinkedList()

    fun start() {
        start = System.currentTimeMillis()
        running = true
    }

    fun stop() {
        if (running) {
            running = false
            stws.addFirst(STW(start, System.currentTimeMillis() - start))
        }
    }

    fun reset() {
        stws.clear()
    }

    fun removeFirst() {
        if (!stws.isEmpty()) {
            stws.removeFirst()
        }
    }

    fun isRunning() = running

    fun formatRuntime(): String {
        if (running) {
            val time = System.currentTimeMillis() - start
            return formatTime(time)
        }
        return ""
    }

    fun formatLastStopTime(): String {
        if (!stws.isEmpty()) {
            return formatTime(stws.first.time)
        }
        return ""
    }

    fun top(): String {
        val sb = StringBuilder()
        sb.append("Top\n")
        measureTimeMillis {
            TreeSet(stws).forEachIndexed { i, stw -> sb.append(i + 1).append(".\t").append(stw.fmt).append("\n") }
        }.log("top.time")
        return sb.toString()
    }

    fun history(): String {
        val sb = StringBuilder()
        sb.append("History\n")
        measureTimeMillis {
            stws.forEachIndexed { i, stw -> sb.append(stws.size-i).append(".\t").append(stw.fmt).append("\n") }
        }.log("history.time")
        return sb.toString()
    }

    fun summary(): String {
        val sb = StringBuilder()
        sb.append("Summary\n")
        if (stws.size > 0) {
            measureTimeMillis {
                val mean = stws.fold(0.0, { t: Double, stw: STW -> t + stw.time }) / stws.size
                val sigma = Math.sqrt(stws.fold(0.0, { t: Double, stw: STW -> t + (stw.time - mean) * (stw.time - mean) }) / stws.size)
                sb.append("n:\t").append(stws.size).append("\n")
                sb.append("µ:\t").append(formatTime(mean.toLong())).append("\n")
                sb.append("\u03C3:\t").append(formatTime(sigma.toLong())).append("\n")
            }.log("summary.time")
        }
        return sb.toString()
    }

}