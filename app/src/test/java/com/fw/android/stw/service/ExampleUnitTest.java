package com.fw.android.stw.service;

import com.fw.android.stw.service.STW;
import com.fw.android.stw.service.STWService;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public void testSTWService() {
        STWService stwService = STWService.INSTANCE;
        stwService.start();
        System.out.println(stwService.isRunning());
        stwService.stop();

        STW stw = new STW(0L, 1000L);
        System.out.println(stw.getFmt());
        System.out.println(stw.getFmt());
        System.out.println(stw.getFmt());
    }

    @Test
    public void testDateFormat() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String fname = "fws-stw-" + fmt.format(new Date()) + ".csv";
        System.out.println(fname);
    }
}