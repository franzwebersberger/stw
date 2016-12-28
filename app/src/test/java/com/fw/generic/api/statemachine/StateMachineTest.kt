package com.fw.generic.api.statemachine

import com.fw.generic.api.statemachine.StateMachine.T
import com.fw.generic.api.statemachine.StateMachineTest.EVENT.*
import com.fw.generic.api.statemachine.StateMachineTest.STATE.*
import org.junit.Test

/**
 * Created by fw on 27.12.16.
 */
class StateMachineTest {
    enum class STATE {
        S0, S1, S2
    }

    enum class EVENT {
        E1, E2, E3
    }

    val s0e2Callback: (Any) -> Unit = {
        println("s0e2Callback")
        true
    }

    val s2e1Callback: (Any) -> Unit = {
        println("s2e1Callback")
        false
    }

    @Test
    fun test1() {
        val stm = StateMachine<STATE, EVENT, Any>(
                S0,
                T(S0, E1, S1),
                T(S1, E2, S2),
                T(S2, E3, S0),
                T(S0, E2, S2, s0e2Callback),
                T(S2, E1, S0, s2e1Callback)
        )

        println(stm)
        var t = stm.handleEvent(E1, this)
        println("$stm\t$t")
        t = stm.handleEvent(E2, this)
        println("$stm\t$t")
        t = stm.handleEvent(E3, this)
        println("$stm\t$t")
        t = stm.handleEvent(E2, this)
        println("$stm\t$t")
        t = stm.handleEvent(E1, this)
        println("$stm\t$t")
        t = stm.handleEvent(E3, this)
        println("$stm\t$t")
        println(stm.state)

    }

}