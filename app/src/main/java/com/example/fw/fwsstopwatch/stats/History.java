package com.example.fw.fwsstopwatch.stats;

import static com.example.fw.fwsstopwatch.stats.Formatter.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by fw on 03.12.16.
 */

public class History implements STWStatistics {

    private final int n;

    public History(int n) {
        this.n = n;
    }

    @Override
    public String stats(Collection<STW> data) {
        StringBuilder sb = new StringBuilder();
        int l = data.size();
        int i = 0;
        for (Iterator<STW> it = data.iterator(); it.hasNext() && i < n; i++) {
            sb.append(String.format("%3d.\t%s\n", l--, formatSTW(it.next())));
        }
        return sb.toString();
    }
}
