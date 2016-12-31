package com.fw.generic.api.statemachine

/**
 * Created by fw on 27.12.16.
 */
class StateMachine<S, in E, in C>(var state: S, vararg ts: T<S, E, C>) {

    data class T<out S, out E, in C>(val state: S, val event: E, val nextState: S, val callback: (C) -> Unit = {})

    private val transitions: Map<Pair<S, E>, T<S, E, C>> = ts.associateBy({ Pair(it.state, it.event) }, { it })

    fun handleEvent(event: E, context: C) = transitions[Pair(state, event)]?.let {
        state = it.nextState
        it.callback(context)
        true
    } ?: false

    override fun toString(): String {
        return "StateMachine(state=$state)"
    }

}