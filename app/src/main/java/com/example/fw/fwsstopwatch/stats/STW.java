package com.example.fw.fwsstopwatch.stats;

/**
 * Created by fw on 27.11.16.
 */

public class STW implements Comparable<STW> {

    public final long start;
    public final long time;

    public STW(long start, long time) {
        this.start = start;
        this.time = time;
    }

    @Override
    public String toString() {
        return "STW{" +
                "start=" + start +
                ", time=" + time +
                '}';
    }

    @Override
    public int compareTo(STW another) {
        int dTime = (int) (this.time - another.time);
        if (dTime == 0) {
            return (int) (this.start - another.start);
        }
        return dTime;
    }
}
