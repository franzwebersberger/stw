package com.example.fw.fwsstopwatch;

import android.util.Log;

import com.example.fw.fwsstopwatch.stats.History;
import com.example.fw.fwsstopwatch.stats.STW;
import com.example.fw.fwsstopwatch.stats.STWStatistics;
import com.example.fw.fwsstopwatch.stats.Summary;
import com.example.fw.fwsstopwatch.stats.Top;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import static com.example.fw.fwsstopwatch.stats.Formatter.formatSTW;
import static com.example.fw.fwsstopwatch.stats.Formatter.formatTime;

/**
 * Created by fw on 27.11.16.
 */

public class STWManager {

    private static final String LOGTAG = "--stw--";

    private final Deque<STW> stws = new LinkedList<>();
    private final STWStatistics history = new History(1000);
    private final STWStatistics top = new Top(10);
    private final STWStatistics summary = new Summary();

    private boolean running = false;
    private long start;


    public void start() {
        start = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        if (running) {
            running = false;
            stws.addFirst(new STW(start, System.currentTimeMillis() - start));
        }
    }

    public void reset() {
        stws.clear();
    }

    public void removeFirst() {
        if (!stws.isEmpty()) {
            stws.removeFirst();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String formatRuntime() {
        if (running) {
            long time = System.currentTimeMillis() - start;
            return formatTime(time);
        }
        return "";
    }

    public String formatLastStopTime() {
        if (!stws.isEmpty()) {
            return formatTime(stws.getFirst().time);
        }
        return "";
    }

    public String stats() {
        StringBuilder sb = new StringBuilder();
        sb.append(summary.stats(stws));
        sb.append("\n");
        sb.append("Top 10\n");
        sb.append(top.stats(stws));
        return sb.toString();
    }

    public String history() {
        StringBuilder sb = new StringBuilder();
        sb.append("History\n");
        sb.append(history.stats(stws));
        return sb.toString();
    }

    public void log() {
        Log.i(LOGTAG, "n=" + stws.size());
        for (Iterator<STW> it = stws.iterator(); it.hasNext(); ) {
            Log.i(LOGTAG, formatSTW(it.next()));
        }
    }
}
