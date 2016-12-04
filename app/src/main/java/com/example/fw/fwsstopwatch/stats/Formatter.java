package com.example.fw.fwsstopwatch.stats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fw on 03.12.16.
 */

public class Formatter {
    private static final DateFormat FDATE = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat FTIME = new SimpleDateFormat("mm:ss.SSS");

    private Formatter() {
    }

    public static String formatDate(long start) {
        return FDATE.format(new Date(start));
    }

    public static String formatTime(long time ) {
        return FTIME.format(new Date(time));
    }

    public static String formatSTW(STW stw) {
        return formatDate(stw.start) + "\t" + formatTime(stw.time);
    }

}
