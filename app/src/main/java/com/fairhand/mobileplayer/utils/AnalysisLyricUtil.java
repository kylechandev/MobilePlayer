package com.fairhand.mobileplayer.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.fairhand.mobileplayer.entity.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 解析歌词工具类
 */
public class AnalysisLyricUtil {
    
    /**
     * 是否存在歌词
     */
    private static boolean isExistsLyric = false;
    
    
    private static ArrayList<Lyric> lyrics;
    
    /**
     * 得到解析好的歌词列表
     */
    public static ArrayList<Lyric> getLyrics() {
        return lyrics;
    }
    
    /**
     * 是否存在歌词
     */
    public static boolean isExistsLyric() {
        return isExistsLyric;
    }
    
    /**
     * 读取歌词文件
     */
    public static void readLyricFile(File file) {
        if ((file == null) || !file.exists()) {
            // 歌词文件不存在
            lyrics = null;
            isExistsLyric = false;
        } else {
            // 存在，解析
            lyrics = new ArrayList<>();
            isExistsLyric = true;
            
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                        "UTF-8"));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLyric(line);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // 排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public int compare(Lyric left, Lyric right) {
                    return Long.compare(left.getTimePoint(), right.getTimePoint());
                }
            });
            
            // 计算高亮显示的时间
            for (int i = 0; i < lyrics.size(); i++) {
                Lyric oneLyric = lyrics.get(i);
                if (i + 1 < lyrics.size()) {
                    Lyric twoLyric = lyrics.get(i + 1);
                    oneLyric.setHighLightTime(twoLyric.getTimePoint() - oneLyric.getTimePoint());
                }
            }
        }
    }
    
    /**
     * 解析一句歌词
     *
     * @param line [02:04.12][03:37.32][00:59.73]这样格式的
     */
    private static void parseLyric(String line) {
        // 第一次出现[ ]的位置
        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        
        if (pos1 == 0 && pos2 != -1) {// 一句歌词
            // 装时间
            long[] times = new long[getCountTag(line)];
            
            // String类型的时间 [02:04.12]
            String strTime = line.substring(pos1 + 1, pos2);
            times[0] = strTime2LongTime(strTime);
            
            String content = line;
            int i = 1;
            while (pos1 == 0 && pos2 != -1) {
                content = content.substring(pos2 + 1);
                pos1 = content.indexOf("[");
                pos2 = content.indexOf("]");
                if (pos2 != -1) {
                    strTime = content.substring(pos1 + 1, pos2);
                    times[i] = strTime2LongTime(strTime);
                    
                    if (times[i] == -1) {
                        return;
                    }
                    
                    i++;
                }
            }
            
            Lyric lyric = new Lyric();
            // 把时间数组和文本关联起来，并且加入到集合中
            for (long time : times) {
                
                if (time != 0) {// 有时间戳
                    
                    lyric.setContent(content);
                    lyric.setTimePoint(time);
                    // 添加到集合中
                    lyrics.add(lyric);
                    lyric = new Lyric();
                    
                }
                
            }
        }
    }
    
    /**
     * 将String类型的时间转换为long类型
     *
     * @param strTime String类型的时间
     */
    private static long strTime2LongTime(String strTime) {
        long result;
        
        try {
            // 1.把02:04.12按照:切割成02和04.12
            String[] s1 = strTime.split(":");
            // 2.把04.12按照.切割成04和12
            String[] s2 = s1[1].split("\\.");
            
            // 1.分
            long minute = Long.parseLong(s1[0]);
            
            // 2.秒
            long second = Long.parseLong(s2[0]);
            
            // 3.毫秒
            long millisecond = Long.parseLong(s2[1]);
            
            result = minute * 60 * 1000 + second * 1000 + millisecond * 10;
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        
        return result;
    }
    
    /**
     * 判断一行里有多少句歌词
     */
    private static int getCountTag(String line) {
        int result;
        String[] left = line.split("\\[");
        String[] right = line.split("]");
        
        if (left.length == 0 && right.length == 0) {
            result = 1;
        } else if (left.length > right.length) {
            result = left.length;
        } else {
            result = right.length;
        }
        return result;
    }
}
