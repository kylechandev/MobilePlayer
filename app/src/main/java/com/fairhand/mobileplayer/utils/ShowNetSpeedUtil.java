package com.fairhand.mobileplayer.utils;

import android.content.Context;
import android.net.TrafficStats;

/**
 * 显示网速工具
 */
public class ShowNetSpeedUtil {
    
    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;
    
    /**
     * 获取网速
     * 每秒获取一次
     */
    public static String getNetSpeed(Context context) {
        
        String netSpeed;
        long nowTotalRxBytes =
                TrafficStats.getUidRxBytes(
                        context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ?
                        0 : (TrafficStats.getTotalRxBytes() / 1024);// 转换为kb
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 /
                              (nowTimeStamp - lastTimeStamp));// 毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        netSpeed = String.valueOf(speed);
        // kb超过4位转为Mb
        if (netSpeed.length() >= 4) {
            // Mb精确到2位小数
            String temp = String.valueOf((float) speed / 1024);
            netSpeed = temp.substring(temp.indexOf(".") + 1, temp.length()).length() > 2 ?
                               String.valueOf(((double) Math.round(Double.valueOf(temp) * 1E2) /
                                                       1E2)) + " Mb/s" : temp + " Mb/s";
        } else {
            netSpeed = String.valueOf(speed) + " kb/s";
        }
        return netSpeed;
    }
}
