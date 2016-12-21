package com.fw.android.stw.activity

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.fw.android.stw.R
import com.fw.android.stw.activity.STWActivityColors.COLOR_IDLE
import com.fw.android.stw.activity.STWActivityColors.COLOR_READY
import com.fw.android.stw.activity.STWActivityColors.COLOR_RUNNING
import com.fw.android.stw.activity.STWActivityState.*
import com.fw.android.stw.service.STW
import com.fw.android.stw.service.STWService
import com.fw.android.stw.service.SummaryStatistics
import com.fw.android.stw.service.formatTime

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val LOGTAG = "-- MainActivity --"
    private val TIMER_UPDATE_DELAY = 10L

    private var locked = false
    private var state = IDLE
    private var mainButton: Button? = null
    private var mainLayout: LinearLayout? = null
    private var mainTextView: TextView? = null
    private var countTextView: TextView? = null
    private var rankTextView: TextView? = null
    private var summaryView: TextView? = null
    private var topView: TextView? = null
    private var historyView: TextView? = null

    private val mainTextViewUpdater = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (STWService.isRunning()) {
                val runtime = formatTime(STWService.runtime())
                mainTextView?.text = runtime
                rankTextView?.text = formatRank(STWService.rank())
                mainButton?.text = runtime
            } else {
                mainButton?.setText(R.string.stw_state_ready)
            }
        }
    }

    private val statsViewUpdater = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i(LOGTAG, "statViewUpdater: msg=" + msg)
            updateStats()
        }
    }

    private val timerRefresher = object : Runnable {
        override fun run() {
            mainTextViewUpdater.obtainMessage().sendToTarget()
            mainTextViewUpdater.postDelayed(this, TIMER_UPDATE_DELAY)
        }
    }

    private val mainButtonTouchListener = View.OnTouchListener { v, event ->
        Log.i(LOGTAG, "onTouch() event=" + event)
        return@OnTouchListener when (event.action) {
            MotionEvent.ACTION_DOWN -> mainButtonDown(v)
            MotionEvent.ACTION_UP -> mainButtonUp(v)
            else -> false
        }
    }

    private fun mainButtonDown(view: View): Boolean = when (state) {
        IDLE -> {
            mainLayout?.setBackgroundColor(COLOR_READY)
            mainTextView?.setText(R.string.stw_zero)
            mainButton?.setText(R.string.stw_zero)
            countTextView?.text = formatCount(1 + STWService.currentCount())
            rankTextView?.text = formatRank(1)
            state = READY
            true
        }
        READY -> {
            state = READY
            true
        }
        RUNNING -> {
            STWService.stop()
            stopTimer()
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            mainLayout?.setBackgroundColor(COLOR_IDLE)
            mainTextView?.text = formatTime(STWService.runtime())
            countTextView?.text = formatCount(STWService.currentCount())
            rankTextView?.text = formatRank(STWService.rank())
            mainButton?.text = formatTime(STWService.runtime())
            statsViewUpdater.obtainMessage().sendToTarget()
            state = IDLE
            true
        }
    }

    private fun mainButtonUp(view: View): Boolean = when (state) {
        IDLE -> {
            state = IDLE
            true
        }
        READY -> {
            STWService.start()
            startTimer()
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            mainLayout?.setBackgroundColor(COLOR_RUNNING)
            state = RUNNING
            true
        }
        RUNNING -> {
            state = RUNNING
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(LOGTAG, "onCreate")

        state = if (STWService.isRunning()) RUNNING else IDLE
        mainButton = findViewById(R.id.button) as Button
        (mainButton as Button).setOnTouchListener(mainButtonTouchListener)
        mainLayout = findViewById(R.id.main_layout) as LinearLayout
        countTextView = findViewById(R.id.left_pad) as TextView
        rankTextView = findViewById(R.id.right_pad) as TextView
        mainTextView = findViewById(R.id.text1) as TextView
        summaryView = findViewById(R.id.summaryView) as TextView
        topView = findViewById(R.id.text2) as TextView
        historyView = findViewById(R.id.text3) as TextView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.i(LOGTAG, "onCreateOptionsMenu()")
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        Log.i(LOGTAG, "lockMenuItem=" + menu.findItem(R.id.lock))
        menu.findItem(R.id.lock).isChecked = locked
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(LOGTAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.export -> return exportData()
            R.id.reset -> return reset()
            R.id.drop -> return dropLast()
            R.id.lock -> {
                item.isChecked = !item.isChecked
                setLockState(item.isChecked)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(LOGTAG, "onStart()")
        if (STWService.isRunning()) {
            startTimer()
        }
        updateStats()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(LOGTAG, "onRestoreInstanceState()")
        setLockState(savedInstanceState.getBoolean("locked"))
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOGTAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.i(LOGTAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(LOGTAG, "onSaveInstanceState()")
        outState.putBoolean("locked", locked)
    }

    override fun onStop() {
        super.onStop()
        Log.i(LOGTAG, "onStop()")
        stopTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOGTAG, "onDestroy()")
    }

    private fun startTimer() {
        mainTextViewUpdater.postDelayed(timerRefresher, TIMER_UPDATE_DELAY)
    }

    private fun stopTimer() {
        mainTextViewUpdater.removeCallbacks(timerRefresher)
    }

    private fun dropLast(): Boolean {
        Log.i(LOGTAG, "drop")
        STWService.removeFirst()
        updateStats()
        return true
    }

    private fun exportData(): Boolean {
        Log.i(LOGTAG, "export")
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        Log.i(LOGTAG, "dir=" + dir.absolutePath)
        val mkdirs = dir.mkdirs()
        val message = STWService.exportHistory(dir)
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
        return true
    }

    private fun reset(): Boolean {
        Log.i(LOGTAG, "reset")
        STWService.reset()
        mainTextView?.setText(R.string.stw_zero)
        updateStats()
        return true
    }

    private fun setLockState(lock: Boolean) {
        this.locked = lock
        Log.i(LOGTAG, "lock")
        mainButton?.setEnabled(!lock)
        if (lock) {
            mainButton?.setText(R.string.stw_state_locked)
            mainLayout?.bringToFront()
            mainLayout?.invalidate()
        } else {
            mainButton?.setText(R.string.stw_state_ready)
            mainButton?.bringToFront()
            mainButton?.invalidate()
        }
    }

    private fun updateStats() {
        mainTextView?.setText(formatTime(STWService.runtime()))
        countTextView?.setText(formatCount(STWService.currentCount()))
        rankTextView?.setText(formatRank(STWService.rank()))
        summaryView?.setText(formatSummary(STWService.summary()))
        val h = formatHistory(STWService.history)
        historyView?.setText(h.substring(0..Math.min(h.length - 1, 20000)))
        val t = formatTop(STWService.top)
        topView?.setText(t.substring(0..Math.min(t.length - 1, 20000)))
    }

    private fun formatCount(n: Int) = "$n."

    private fun formatRank(rank: Int) = "($rank)"

    private fun formatSummary(summary: SummaryStatistics): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.stw_stats_summary)).append("\n")
        sb.append("n: ").append(summary.n).append("   ")
        sb.append("µ: ").append(formatTime(summary.mean.toLong())).append("   ")
        sb.append("σ: ").append(formatTime(summary.sigma.toLong())).append("   ")
        return sb.toString()
    }

    private fun formatTop(top: Collection<STW>): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.stw_stats_top)).append("\n")
        top.forEachIndexed { i, stw -> sb.append(i + 1).append(".\t").append(stw.fmt).append("\n") }
        return sb.toString()
    }

    private fun formatHistory(history: Collection<STW>): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.stw_stats_history)).append("\n")
        history.forEach { stw -> sb.append(stw.fmt).append("\n") }
        return sb.toString()
    }

}
