package com.fw.android.stw.service

import org.junit.Test

/**
 * Created by fw on 30.12.16.
 */
class DiverseTests {

    @Test
    fun test1() {
        val t = listOf(1,2,3)
        val u = t.take(5)
        println("u=$u")
    }

    @Test
    fun test2() {
        val td = TestData
        println("td=$td")
    }

    @Test
    fun test3() {
        (0 .. 10).forEach {
            println(it)
        }
    }

}
