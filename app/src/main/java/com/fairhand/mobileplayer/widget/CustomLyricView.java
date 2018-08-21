package com.fairhand.mobileplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.fairhand.mobileplayer.domain.Lyric;

import java.util.ArrayList;

/**
 * 自定义歌词显示控件
 */
public class CustomLyricView extends android.support.v7.widget.AppCompatTextView {
    
    /**
     * 歌词列表
     */
    private ArrayList<Lyric> lyrics;
    
    /**
     * 画笔
     */
    private Paint mCurrentLyricPaint;
    
    private Paint mWhitePaint;
    
    /**
     * 歌词控件的宽
     */
    private int width;
    
    /**
     * 歌词控件的高
     */
    private int height;
    
    /**
     * 一句歌词在列表中的索引位置
     */
    private int index;
    
    /**
     * 设置每句歌词的行高
     */
    private int lyricHeight = 128;
    
    /**
     * 当前播放进度
     */
    private int currentPositon;
    
    /**
     * 高亮显示的时间
     */
    private long highLightTime;
    
    /**
     * 时间戳（到高亮某句歌词的时刻）
     */
    private long timePoint;
    
    
    /**
     * 设置歌词列表
     */
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }
    
    /**
     * 在代码中创建用
     */
    public CustomLyricView(Context context) {
        this(context, null);
    }
    
    /**
     * 当这个类在布局文件，系统通过该方法实例该类
     */
    public CustomLyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * 当需要设置样式时调用该方法
     */
    public CustomLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        initView();// 初始化布局
    }
    
    /**
     * 初始化布局
     */
    private void initView() {
        // 创建画笔
        mCurrentLyricPaint = new Paint();
        mCurrentLyricPaint.setColor(Color.RED);
        mCurrentLyricPaint.setTextSize(64);
        mCurrentLyricPaint.setAntiAlias(true);// 抗锯齿
        mCurrentLyricPaint.setTextAlign(Paint.Align.CENTER);// 设置居中对齐
        
        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setTextSize(64);
        mWhitePaint.setAntiAlias(true);// 抗锯齿
        mWhitePaint.setTextAlign(Paint.Align.CENTER);// 设置居中对齐
        
        lyrics = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Lyric lyric = new Lyric();
            // 把歌词添加到集合中
            lyrics.add(lyric);
            lyric.setTimePoint(1000 * i);
            lyric.setHighLightTime(1500 + i);
            lyric.setContent(i + "qaqaqaqaqaqaq" + i);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 获得自定义歌词控件的高宽
        width = w;
        height = h;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((lyrics != null) && (lyrics.size() > 0)) {
            // 绘制歌词（当前句）
            String currentLyric = lyrics.get(index).getContent();// 当前歌词
            canvas.drawText(currentLyric, width / 2, height / 2, mCurrentLyricPaint);
            
            // 绘制前面部分
            int tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {
                // 每一句歌词
                String preLyric = lyrics.get(i).getContent();
                tempY = tempY - lyricHeight;
                if (tempY < 0) {// 超出控件高度
                    break;
                }
                canvas.drawText(preLyric, width / 2, tempY, mWhitePaint);// 在控件中间绘制歌词
            }
            
            // 绘制后面部分
            tempY = height / 2;
            for (int i = index + 1; i < lyrics.size(); i++) {
                String nextLyric = lyrics.get(i).getContent();
                tempY = tempY + lyricHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextLyric, width / 2, tempY, mWhitePaint);
            }
            
        } else {
            // 没有歌词
            canvas.drawText("暂无歌词", width / 2, height / 2, mCurrentLyricPaint);
        }
    }
    
    /**
     * 根据当前播放位置，计算显示下一句高亮歌词
     */
    public void setShowNextLyric(int currentPositon) {
        this.currentPositon = currentPositon;
        if ((lyrics == null) || (lyrics.size() == 0))
            return;
        
        for (int i = 1; i < lyrics.size(); i++) {
            if (currentPositon < lyrics.get(i).getTimePoint()) {
                int tempIndex = i - 1;
                if (currentPositon >= lyrics.get(tempIndex).getTimePoint()) {
                    // 得到当前正在播放的歌词
                    index = tempIndex;
                    highLightTime = lyrics.get(index).getHighLightTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }
            }
        }
        
        // 重绘
        invalidate();// 必须在main线程中
        //        postInvalidate(); 子线程
    }
}
















