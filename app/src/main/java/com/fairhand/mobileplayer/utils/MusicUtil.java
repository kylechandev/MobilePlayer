package com.fairhand.mobileplayer.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.fairhand.mobileplayer.MyApplication;
import com.fairhand.mobileplayer.entity.MediaItem;

import java.util.ArrayList;

/**
 * 音乐工具类<br />
 * Create by FairHand on 2018/8/26.
 *
 * @author FairHand
 */
public class MusicUtil {
    
    /**
     * 全局播放音乐列表
     */
    public static ArrayList<MediaItem> mediaItems;
    
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
    
    /**
     * 获取本地音乐数据<br />
     * 1)遍历sdcard，后缀名<br />
     * 2)从内容提供器中获取音乐文件<br />
     * 3)若为6.0以上，需动态读取sdcard的权限
     */
    public static void getDataFromLocal() {
        mediaItems = new ArrayList<>();
        
        new Thread() {
            @Override
            public void run() {
                super.run();
                
                // 获取一个ContentResolver
                ContentResolver resolver = MyApplication.getContext().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.TITLE,// 歌名
                        MediaStore.Audio.Media.DURATION,// 音乐总时长
                        MediaStore.Audio.Media.SIZE,// 音乐的文件大小
                        MediaStore.Audio.Media.DATA,// 音乐的绝对地址
                        MediaStore.Audio.Media.ARTIST,// 歌手
                        MediaStore.Audio.Media.ALBUM_ID,// 专辑图片ID
                        MediaStore.Audio.Media.ALBUM// 专辑名
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);
                        
                        String name = cursor.getString(0);// 音乐的名称
                        mediaItem.setMediaName(name);
                        
                        long duration = cursor.getLong(1);// 音乐的时长
                        mediaItem.setDuration(duration);
                        
                        long size = cursor.getLong(2);// 音乐的大小
                        mediaItem.setSize(size);
                        
                        String data = cursor.getString(3);// 音乐的播放地址
                        mediaItem.setData(data);
                        
                        String artist = cursor.getString(4);// 歌手
                        mediaItem.setMusicArtist(artist);
                        
                        long albumId = cursor.getLong(5);// 专辑图片
                        mediaItem.setAlbumId(albumId);
                        
                        String album = cursor.getString(6);// 专辑名
                        mediaItem.setAlbum(album);
                        
                    }
                    cursor.close();
                }
                
            }
        }.start();
        
    }
    
    /**
     * 保存音乐信息
     */
    public static void saveMusicInfo(int position) {
        // 获取到点击位置的音乐文件
        MediaItem mediaItem = mediaItems.get(position);
        
        // 获取到歌名 歌手 专辑图片路径
        String name = mediaItem.getMediaName();
        String artist = mediaItem.getMusicArtist();
        long albumId = mediaItem.getAlbumId();
        
        // 保存bar信息
        SaveCacheUtil.putMusicBarInfo(MyApplication.getContext(),
                "MUSIC_NAME_KEY", "MUSICIAN_KEY",
                "POSITION_KEY", "ALBUM_KEY",
                name, artist, position, albumId);
    }
    
}
