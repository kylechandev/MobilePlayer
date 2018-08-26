package com.fairhand.mobileplayer.entity;

/**
 * 歌词类（每一句歌词）
 * 例：[时间戳]歌词
 * [00:24.12]别堆砌怀念让剧情
 * [00:27.60]变得狗血
 */
public class Lyric {
    
    /**
     * 歌词内容
     */
    private String content;
    
    /**
     * 时间戳
     */
    private long timePoint;
    
    /**
     * 高亮显示时间
     */
    private long highLightTime;
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimePoint() {
        return timePoint;
    }
    
    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }
    
    public long getHighLightTime() {
        return highLightTime;
    }
    
    public void setHighLightTime(long highLightTime) {
        this.highLightTime = highLightTime;
    }
}
