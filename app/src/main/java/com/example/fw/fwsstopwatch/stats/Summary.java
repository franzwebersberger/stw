package com.example.fw.fwsstopwatch.stats;

import java.util.Collection;
import java.util.Iterator;

import static com.example.fw.fwsstopwatch.stats.Formatter.*;

/**
 * Created by fw on 03.12.16.
 */

public class Summary implements STWStatistics {
    @Override
    public String stats(Collection<STW> data) {
        StringBuilder sb = new StringBuilder();
        if (data.size() > 0) {
            int n = data.size();
            double sum = 0.0;
            for (Iterator<STW> it = data.iterator(); it.hasNext(); ) {
                sum += it.next().time;
            }
            double mean = sum / n;
            double svar = 0.0;
            for (Iterator<STW> it = data.iterator(); it.hasNext(); ) {
                double ds = it.next().time - mean;
                svar += ds * ds;
            }
            double sigma = Math.sqrt(svar / n);
            sb.append("count:\t").append(n).append("\n");
            sb.append(" mean:\t").append(formatTime((long) mean)).append("\n");
            sb.append("sigma:\t").append(formatTime((long) sigma)).append("\n");
        }
        return sb.toString();
    }
}
