package com.fw.android.stw.service

import java.util.*

/**
 * Created by fw on 07.12.16.
 */
object STWService {

    private var running = false
    private var start: Long = 0

    private val stws: LinkedList<STW> = LinkedList()
    val history: List<STW> = stws

    private val sorted: TreeSet<STW> = TreeSet<STW>()
    val top: Set<STW> = sorted

    fun isRunning() = running

    fun start() {
        start = System.currentTimeMillis()
        running = true
    }

    fun stop() {
        if (running) {
            running = false
            stws.addFirst(STW(start, System.currentTimeMillis() - start))
            sorted.add(stws.first)
        }
    }

    fun reset() {
        stws.clear()
        sorted.clear()
    }

    fun removeFirst() {
        if (stws.isNotEmpty()) {
            stws.removeFirst()
            sorted.clear()
            sorted.addAll(stws)
        }
    }

    fun runtime(): Long =
            if (running) System.currentTimeMillis() - start
            else if (stws.isNotEmpty()) stws.first.time
            else 0L

    fun rank(): Int =
            if (running)
                sorted.headSet(STW(start, System.currentTimeMillis() - start)).size + 1
            else if (stws.isNotEmpty()) sorted.headSet(stws.first).size + 1
            else 0

    fun summary(): SummaryStatistics {
        if (stws.isNotEmpty()) {
            val mean = stws.fold(0.0, { t: Double, stw: STW -> t + stw.time }) / stws.size
            val sigma = Math.sqrt(stws.fold(0.0, { t: Double, stw: STW -> t + (stw.time - mean) * (stw.time - mean) }) / stws.size)
            return SummaryStatistics(stws.size, mean, sigma)
        } else {
            return SummaryStatistics(0, 0.0, 0.0)
        }
    }

}