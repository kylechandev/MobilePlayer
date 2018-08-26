package com.fairhand.mobileplayer.utils;

import com.fairhand.mobileplayer.entity.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 解析歌词工具类
 */
public class AnalysisLyricUtil {
    
    private static ArrayList<Lyric> lyrics;
    
    /**
     * 读取歌词文件
     */
    public static void readLyricFile(File file) {
        if ((file == null) || !file.exists()) {
            // 歌词文件不存在
            lyrics = null;
        } else {
            // 存在，解析
            lyrics = new ArrayList<>();
            
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                        "GBK"));
                
                String line;
                
                while ((line = reader.readLine()) != null) {
                    line = parseLyric(line);
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
        }
    }
    
    /**
     * 解析一句歌词
     */
    private static String parseLyric(String line) {
        return null;
    }
}
