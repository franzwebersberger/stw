package com.example.fw.fwsstopwatch.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.example.fw.fwsstopwatch.stats.Formatter.formatSTW;

/**
 * Created by fw on 03.12.16.
 */

public class Top implements STWStatistics {

    private final int n;

    public Top(int n) {
        this.n = n;
    }

    @Override
    public String stats(Collection<STW> data) {
        SortedSet<STW> sorted = new TreeSet<>(data);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Iterator<STW> it = sorted.iterator(); it.hasNext() && i < n; i++) {
            sb.append(String.format("%3d.\t%s\n", i+1, formatSTW(it.next())));
        }
        return sb.toString();
    }
}
