package com.fairhand.mobileplayer.entity;

import java.io.Serializable;

/**
 * 代表一个视频或音频
 */
public class MediaItem implements Serializable {
    
    /**
     * 媒体名称
     */
    private String mediaName;
    
    /**
     * 时长
     */
    private long duration;
    
    /**
     * 大小
     */
    private long size;
    
    /**
     * 播放地址
     */
    private String data;
    
    /**
     * 网络视频的描述
     */
    private String describe;
    
    /**
     * 网络视频的图片
     */
    private String imageUrl;
    
    /**
     * 歌手
     */
    private String musicArtist;
    
    /**
     * 专辑图片ID
     */
    private long albumId;
    
    /**
     * 专辑名
     */
    private String album;
    
    public String getAlbum() {
        return album;
    }
    
    public void setAlbum(String album) {
        this.album = album;
    }
    
    public long getAlbumId() {
        return albumId;
    }
    
    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
    
    public String getDescribe() {
        return describe;
    }
    
    public void setDescribe(String describe) {
        this.describe = describe;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getMediaName() {
        return mediaName;
    }
    
    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getMusicArtist() {
        return musicArtist;
    }
    
    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }
    
}
