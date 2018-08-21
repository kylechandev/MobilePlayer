package com.fairhand.mobileplayer.utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * 毫秒转标准时间工具
 */
public class TimeConvertUtil {
    
    private static StringBuilder mFortmatBuilder;
    
    private static Formatter mFormatter;
    
    public TimeConvertUtil() {
        // 转换成字符串的时间
        mFortmatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFortmatBuilder, Locale.getDefault());
    }
    
    /**
     * 将毫秒转换成1：20：30形式
     */
    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        
        int minutes = (totalSeconds / 60) % 60;
        
        int hours = totalSeconds / 3600;
        
        mFortmatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    
    }
}
