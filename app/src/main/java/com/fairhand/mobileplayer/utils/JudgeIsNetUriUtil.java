package com.fairhand.mobileplayer.utils;

/**
 * 判断是否为网络资源工具
 */
public class JudgeIsNetUriUtil {
    
    /**
     * 判断是否为网络资源
     */
    public static boolean isNetUri(String uri) {
        if ((uri.toLowerCase().startsWith("http")) ||
                    uri.toLowerCase().startsWith("rtsp") ||
                    uri.toLowerCase().startsWith("mms")) {
            return true;
        }
        return false;
    }
    
}
