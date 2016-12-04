package com.example.fw.fwsstopwatch;

import com.example.fw.fwsstopwatch.stats.STW;
import com.example.fw.fwsstopwatch.stats.STWStatistics;
import com.example.fw.fwsstopwatch.stats.Summary;
import com.example.fw.fwsstopwatch.stats.Top;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void topTest() {
        STWStatistics stats = new Top(5);
        List<STW> data = new LinkedList<>();
        data.add(new STW(0, 1000));
        data.add(new STW(0, 1001));
        data.add(new STW(1000, 1000));
        System.out.println(stats.stats(data));
    }

    @Test
    public void summaryTest() {
        STWStatistics stats = new Summary();
        List<STW> data = new LinkedList<>();
        data.add(new STW(0, 1000));
        data.add(new STW(0, 1001));
        data.add(new STW(1000, 1000));
        System.out.println(stats.stats(data));
    }

}