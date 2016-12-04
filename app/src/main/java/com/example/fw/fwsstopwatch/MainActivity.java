package com.example.fw.fwsstopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "--fw--";

    private boolean locked = false;
    private Button mainButton;
    private LinearLayout mainLayout;
    private TextView mainTextView;
    private TextView statTextView;
    private TextView historyView;

    private STWManager stwManager;
    private Timer timer;

    private Handler mainTextViewUpdater = new Handler() {
        public void handleMessage(Message msg) {
            if (stwManager.isRunning()) {
                String runtime = stwManager.formatRuntime();
                mainTextView.setText(runtime);
                mainButton.setText(runtime);
            } else {
                mainButton.setText(getString(R.string.stw_state_ready));
            }
        }
    };

    private View.OnTouchListener mainButtonTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            Log.i(LOGTAG, "onTouch()");
            if (stwManager.isRunning()) {
                stopTimer();
                stwManager.stop();
                mainTextView.setText(stwManager.formatLastStopTime());
                mainButton.setText(R.string.stw_state_ready);
                updateStats();
                v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                return true;
            }
            else {
                mainButton.setText(R.string.stw_state_start);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(LOGTAG, "onCreate()");

        stwManager = GlobalState.getInstance().getStwManager();
        mainButton = (Button) findViewById(R.id.button);
        mainButton.setOnTouchListener(mainButtonTouchListener);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainTextView = (TextView) findViewById(R.id.text1);
        statTextView = (TextView) findViewById(R.id.text2);
        historyView = (TextView) findViewById(R.id.text3);
        historyView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.i(LOGTAG, "onCreateOptionsMenu()");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem lockMenuItem = (MenuItem) menu.findItem(R.id.lock);
        Log.i(LOGTAG, "lockMenuItem=" + lockMenuItem);
        lockMenuItem.setChecked(locked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOGTAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.reset:
                return reset();
            case R.id.drop:
                return dropLast();
            case R.id.lock:
                item.setChecked(!item.isChecked());
                setLockState(item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOGTAG, "onStart()");
        if (stwManager.isRunning()) {
            startTimer();
        }
        updateStats();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(LOGTAG, "onRestoreInstanceState()");
        setLockState(savedInstanceState.getBoolean("locked"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOGTAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOGTAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOGTAG, "onSaveInstanceState()");
        outState.putBoolean("locked", locked);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOGTAG, "onStop()");
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOGTAG, "onDestroy()");
    }

    public void mainButtonAction(View view) {
        Log.i(LOGTAG, "mainButtonAction: view=" + view);
        if (stwManager.isRunning()) {
            stopTimer();
            stwManager.stop();
            mainTextView.setText(stwManager.formatLastStopTime());
            mainButton.setText(R.string.stw_state_ready);
            updateStats();
        } else {
            startTimer();
            stwManager.start();
        }
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainTextViewUpdater.obtainMessage().sendToTarget();
            }
        }, 0, 10);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private boolean dropLast() {
        Log.i(LOGTAG, "drop");
        stwManager.removeFirst();
        updateStats();
        return true;
    }

    private boolean reset() {
        Log.i(LOGTAG, "reset");
        stwManager.reset();
        mainTextView.setText(R.string.stw_zero);
        updateStats();
        return true;
    }

    private void setLockState(boolean lock) {
        this.locked = lock;
        Log.i(LOGTAG, "lock");
        mainButton.setEnabled(!lock);
        if (lock) {
            mainButton.setText(R.string.stw_state_locked);
            mainLayout.bringToFront();
            mainLayout.invalidate();
        } else {
            mainButton.setText(R.string.stw_state_ready);
            mainButton.bringToFront();
            mainButton.invalidate();

        }
    }

    private void updateStats() {
        Log.i(LOGTAG, "updateStats()");
        statTextView.setText(stwManager.stats());
        historyView.setText(stwManager.history());
    }

}
