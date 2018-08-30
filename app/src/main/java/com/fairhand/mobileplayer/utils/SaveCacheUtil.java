package com.fairhand.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.fairhand.mobileplayer.service.MusicPlayerService;

/**
 * 保存缓存信息工具
 *
 * @author FairHand
 */
public class SaveCacheUtil {
    
    /**
     * 保存播放模式
     */
    public static void putPlayMode(Context context, String key, int values) {
        SharedPreferences.Editor editor
                = context.getSharedPreferences("PLAY_MODE", Context.MODE_PRIVATE).edit();
        editor.putInt(key, values).apply();
    }
    
    /**
     * 得到播放模式
     */
    public static int getPlayMode(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("PLAY_MODE", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, MusicPlayerService.REPEAT_ALL);
    }
    
    /**
     * 保存当前播放位置
     */
    public static void putCurrentPosition(Context context, String key, int values) {
        SharedPreferences.Editor editor
                = context.getSharedPreferences("CURRENT_POSITION", Context.MODE_PRIVATE).edit();
        editor.putInt(key, values).apply();
    }
    
    /**
     * 得到当前播放位置
     */
    public static int getCurrentPosition(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("CURRENT_POSITION", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }
    
}
