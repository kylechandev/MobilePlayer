package com.fairhand.mobileplayer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import org.xutils.x;

public class MyApplication extends Application {
    
    /**
     * 全局Context
     */
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);// 是否输出debug日志，开启debug会影响性能
        context = getApplicationContext();// 获取到全局Context
    }
    
    /**
     * 获取到全局Context
     */
    public static Context getContext() {
        return context;
    }
}