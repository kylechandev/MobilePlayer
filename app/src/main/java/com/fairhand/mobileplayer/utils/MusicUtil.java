package com.fairhand.mobileplayer.utils;

import android.database.Cursor;
import android.net.Uri;

import com.fairhand.mobileplayer.MyApplication;

/**
 * 音乐工具类
 *
 * @author FairHand
 */
public class MusicUtil {
    
    /**
     * 获取到本地音乐的专辑图片存储地址
     */
    public static String getAlbumArt(long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cursor = MyApplication.getContext().getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Long.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        assert cursor != null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToNext();
            album_art = cursor.getString(0);
        }
        cursor.close();
        return album_art;
        
    }
}
