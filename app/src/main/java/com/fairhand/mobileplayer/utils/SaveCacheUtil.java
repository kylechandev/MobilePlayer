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
     * 保存music_bar信息
     *
     * @param artistValues   歌手信息
     * @param nameValues     歌名信息
     * @param positionValues 歌曲在ListView中的位置信息
     * @param albumValues    歌曲专辑图片id
     */
    public static void putMusicBarInfo(Context context, String nameKey, String artistKey,
                                       String positionKey, String albumKey,
                                       String nameValues, String artistValues,
                                       int positionValues, long albumValues) {
        SharedPreferences.Editor editor
                = context.getSharedPreferences("MUSIC_BAR", Context.MODE_PRIVATE).edit();
        editor.putString(nameKey, nameValues);
        editor.putLong(albumKey, albumValues);
        editor.putString(artistKey, artistValues);
        editor.putInt(positionKey, positionValues);
        editor.apply();
        
    }
    
    /**
     * 得到music_bar歌名
     */
    public static String getMusicBarMusicName(Context context, String nameKey) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("MUSIC_BAR", Context.MODE_PRIVATE);
        return sharedPreferences.getString(nameKey, null);
    }
    
    /**
     * 得到music_bar歌手
     */
    public static String getMusicBarMusicArtist(Context context, String artistKey) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("MUSIC_BAR", Context.MODE_PRIVATE);
        return sharedPreferences.getString(artistKey, null);
    }
    
    /**
     * 得到music_bar专辑图片id
     */
    public static long getMusicBarMusicAlbum(Context context, String albumKey) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("MUSIC_BAR", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(albumKey, 0L);
    }
    
    /**
     * 得到music_bar歌曲在ListView中的位置信息
     */
    public static int getMusicBarMusicPosition(Context context, String positionKey) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("MUSIC_BAR", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(positionKey, 0);
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
