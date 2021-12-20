package com.bistu.cs.musicplayer;

import android.app.Application;

import org.xutils.x;

public class APP extends Application {
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);

    }
}

