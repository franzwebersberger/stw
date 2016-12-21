package com.fw.android.stw.activity

/**
 * Created by fw on 21.12.16.
 */
enum class STWActivityState {
    /**
     * stw is idle
     */
    IDLE,
    /**
     * stw is ready for starting, button is pressed but not released yet
     */
    READY,
    /**
     * stw is running
     */
    RUNNING
}