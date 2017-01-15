package com.fw.android.stw.activity

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import com.fw.android.stw.R
import com.fw.android.stw.activity.MainActivity.ButtonState.*
import com.fw.android.stw.service.*
import com.fw.generic.api.statemachine.StateMachine
import com.fw.generic.api.statemachine.StateMachine.T
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class MainActivity : AppCompatActivity() {

    enum class ButtonState {IDLE, READY, RUNNING }

    private val LOG_TAG = "-- MainActivity --"
    private val TIMER_UPDATE_DELAY = 10L

    private val COLOR_IDLE = Color.argb(0, 0, 0, 0)
    private val COLOR_READY = Color.argb(128, 0, 255, 0)
    private val COLOR_RUNNING = Color.argb(80, 255, 255, 0)
    private val COLOR_NORM_BACKGROUND = Color.argb(16, 0, 128, 255)

    private val HIST_MINX = 25.0
    private val HIST_WIDTH = 20.0
    private val HIST_NORM_N = 30

    private var locked = false
    private var mainButton: Button? = null
    private var mainLayout: LinearLayout? = null
    private var mainTextView: TextView? = null
    private var countTextView: TextView? = null
    private var rankTextView: TextView? = null
    private var summaryView: TextView? = null
    private var topView: TextView? = null
    private var historyView: TextView? = null
    private var mp: MediaPlayer? = null
    private var graphView: GraphView? = null

    private val buttonSTM = StateMachine<ButtonState, Int, View>(
            IDLE,
            T(IDLE, MotionEvent.ACTION_DOWN, READY, { view ->
                Log.i(LOG_TAG, "buttonSTM: IDLE -> READY")
                mainLayout?.setBackgroundColor(COLOR_READY)
                mainTextView?.setText(R.string.stw_zero)
                mainButton?.setText(R.string.stw_zero)
                countTextView?.text = formatCount(1 + STWService.currentCount())
                rankTextView?.text = formatRank(1)
            }),
            T(READY, MotionEvent.ACTION_UP, RUNNING, { view ->
                Log.i(LOG_TAG, "buttonSTM: READY -> RUNNING")
                STWService.start()
                startTimer()
                playSound(R.raw.s1)
                mainLayout?.setBackgroundColor(COLOR_RUNNING)
            }),
            T(RUNNING, MotionEvent.ACTION_DOWN, IDLE, { view ->
                Log.i(LOG_TAG, "buttonSTM: RUNNING -> IDLE")
                STWService.stop()
                stopTimer()
                playSound(R.raw.s2)
                mainLayout?.setBackgroundColor(COLOR_IDLE)
                mainTextView?.text = formatTime(STWService.runtime())
                countTextView?.text = formatCount(STWService.currentCount())
                rankTextView?.text = formatRank(STWService.rank())
                mainButton?.text = formatTime(STWService.runtime())
                statsViewUpdater.obtainMessage().sendToTarget()
            }))

    private fun playSound(soundId: Int) {
        mp?.release()
        mp = MediaPlayer.create(this, soundId)
        mp?.start()
    }

    private val mainButtonTouchListener = View.OnTouchListener { v, event ->
        return@OnTouchListener buttonSTM.handleEvent(event.action, v)
    }

    private val mainTextViewUpdater = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (STWService.isRunning()) {
                val runtime = formatTime(STWService.runtime())
                mainTextView?.text = runtime
                rankTextView?.text = formatRank(STWService.rank())
                mainButton?.text = runtime
            }
        }
    }

    private val statsViewUpdater = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i(LOG_TAG, "statViewUpdater: msg=" + msg)
            updateStats()
        }
    }

    private val timerRefresher = object : Runnable {
        override fun run() {
            mainTextViewUpdater.obtainMessage().sendToTarget()
            mainTextViewUpdater.postDelayed(this, TIMER_UPDATE_DELAY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(LOG_TAG, "onCreate")

        mainButton = findViewById(R.id.button) as Button
        (mainButton as Button).setOnTouchListener(mainButtonTouchListener)
        mainLayout = findViewById(R.id.main_layout) as LinearLayout
        countTextView = findViewById(R.id.left_pad) as TextView
        rankTextView = findViewById(R.id.right_pad) as TextView
        mainTextView = findViewById(R.id.text1) as TextView
        summaryView = findViewById(R.id.summaryView) as TextView
        topView = findViewById(R.id.text2) as TextView
        historyView = findViewById(R.id.text3) as TextView
        graphView = findViewById(R.id.graph) as GraphView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.i(LOG_TAG, "onCreateOptionsMenu()")
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        Log.i(LOG_TAG, "lockMenuItem=" + menu.findItem(R.id.lock))
        menu.findItem(R.id.lock).isChecked = locked
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(LOG_TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.test_data -> return testData()
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
        Log.i(LOG_TAG, "onStart()")
        if (STWService.isRunning()) {
            startTimer()
            mainLayout?.setBackgroundColor(COLOR_RUNNING)
            buttonSTM.state = RUNNING
        }
        updateStats()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(LOG_TAG, "onRestoreInstanceState()")
        setLockState(savedInstanceState.getBoolean("locked"))
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.i(LOG_TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(LOG_TAG, "onSaveInstanceState()")
        outState.putBoolean("locked", locked)
    }

    override fun onStop() {
        super.onStop()
        Log.i(LOG_TAG, "onStop()")
        stopTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "onDestroy()")
    }

    private fun startTimer() {
        mainTextViewUpdater.postDelayed(timerRefresher, TIMER_UPDATE_DELAY)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun stopTimer() {
        mainTextViewUpdater.removeCallbacks(timerRefresher)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun dropLast(): Boolean {
        Log.i(LOG_TAG, "drop")
        STWService.removeFirst()
        mainButton?.setText(R.string.stw_state_ready)
        updateStats()
        return true
    }

    private fun exportData(): Boolean {
        Log.i(LOG_TAG, "export")
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        Log.i(LOG_TAG, "dir=" + dir.absolutePath)
        dir.mkdirs()
        val message = STWService.exportHistory(dir)
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
        return true
    }

    private fun reset(): Boolean {
        Log.i(LOG_TAG, "reset")
        STWService.reset()
        mainButton?.setText(R.string.stw_state_ready)
        updateStats()
        return true
    }

    private fun setLockState(lock: Boolean) {
        this.locked = lock
        Log.i(LOG_TAG, "setLockState")
        mainButton?.isEnabled = !lock
        if (lock) {
            mainButton?.setText(R.string.stw_state_locked)
            mainLayout?.bringToFront()
            mainLayout?.invalidate()
        } else {
            mainButton?.setText(R.string.stw_state_ready)
            mainButton?.bringToFront()
            mainButton?.invalidate()
        }
        statsViewUpdater.obtainMessage().sendToTarget()
    }

    private fun testData(): Boolean {
        STWService.initWithTestData(TestData)
        updateStats()
        return true
    }

    private fun updateStats() {
        val n = if (locked) STWService.currentCount() else 30
        val summary = STWService.summary()
        mainTextView?.text = formatTime(STWService.runtime())
        countTextView?.text = formatCount(STWService.currentCount())
        rankTextView?.text = formatRank(STWService.rank())
        summaryView?.text = formatSummary(summary)
        historyView?.text = formatHistory(STWService.history, n)
        topView?.text = formatTop(STWService.top, n)
        formatHistogram(summary, STWService.top)
        (findViewById(R.id.left_scroll_view) as ScrollView).scrollTo(0, 0)
        (findViewById(R.id.right_scroll_view) as ScrollView).scrollTo(0, 0)
    }

    private fun formatCount(n: Int) = "$n."

    private fun formatRank(rank: Int) = "($rank)"

    private fun formatSummary(summary: SummaryStatistics): String {
        val sb = StringBuilder()
        sb.append("n: ").append(summary.n).append("   ")
        sb.append("µ: ").append(formatTime(summary.mean.toLong())).append("   ")
        sb.append("σ: ").append(formatTime(summary.sigma.toLong())).append("   ")
        return sb.toString()
    }

    private fun formatHistogram(summary: SummaryStatistics, top: Collection<STW>) {
        val histogram = histSeries(top)
        val norm = normSeries(summary)
        val ymax = histogram.map { it.y + 1.0 }.max() ?: 2.0
        graphView?.apply {
            viewport.apply {
                isXAxisBoundsManual = true
                isYAxisBoundsManual = true
                isScrollable = true
                setMinX(HIST_MINX)
                setMaxX(HIST_MINX + HIST_WIDTH)
                setMinY(0.0)
                setMaxY(ymax)
            }
            removeAllSeries()
            addSeries(BarGraphSeries(histogram.toTypedArray()).apply {
                spacing = 10;
                color = Color.LTGRAY
            })
            addSeries(LineGraphSeries(norm.toTypedArray()).apply {
                thickness = 1
                isDrawBackground = true
                backgroundColor = COLOR_NORM_BACKGROUND
            })
        }
    }

    private fun histSeries(top: Collection<STW>): List<DataPoint> {
        val tmin: Double = if (top.isEmpty()) 0.0 else -1.0 + top.first().time / 1000
        val tmax: Double = if (top.isEmpty()) 1.0 else 1.0 + top.last().time / 1000
        val quants = top.map { Math.floor(it.time / 1000.0) }.groupBy { it }
        return (tmin.toInt()..tmax.toInt()).map { ti ->
            val q = quants.get(ti.toDouble())?.size
            q?.let { DataPoint(ti + 0.5, q.toDouble()) } ?: DataPoint(ti + 0.5, 0.0)
        }
    }

    private fun normSeries(summary: SummaryStatistics): List<DataPoint> {
        fun normPD(n: Int, mean: Double, sigma: Double, x: Double): Double {
            val c = if (sigma != 0.0) n * 0.39894228 / sigma else 0.0
            val t = if (sigma != 0.0) (x - mean) / sigma else 0.0
            return c * Math.exp(-0.5 * t * t)
        }
        val mean = summary.mean / 1000
        val sigma = summary.sigma / 1000
        val x0 = mean - 3 * sigma
        val dx = 6 * sigma / HIST_NORM_N
        return if (summary.n > 1) (0..HIST_NORM_N).map {
            val x = x0 + it * dx
            val y = normPD(summary.n, mean, sigma, x)
            DataPoint(x, y)
        }
        else {
            emptyList()
        }
    }

    private fun formatTop(top: Collection<STW>, n: Int): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.stw_stats_top)).append("\n")
        top.take(n).forEachIndexed { i, stw -> sb.append(i + 1).append(".\t").append(stw.fmt).append("\n") }
        return sb.toString()
    }

    private fun formatHistory(history: Collection<STW>, n: Int): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.stw_stats_history)).append("\n")
        history.take(n).forEach { stw -> sb.append(stw.fmt).append("\n") }
        return sb.toString()
    }

}
