package com.example.fw.fwsstopwatch;

import android.util.Log;

/**
 * Created by fw on 30.11.16.
 */
public class GlobalState {
    private static GlobalState ourInstance = new GlobalState();

    private static final String LOGTAG = "--fw-GlobalState--";

    public static GlobalState getInstance() {
        return ourInstance;
    }

    private STWManager stwManager;

    private GlobalState() {
        Log.i(LOGTAG, "new");

        this.stwManager = new STWManager();
    }

    public STWManager getStwManager() {
        return stwManager;
    }
}
