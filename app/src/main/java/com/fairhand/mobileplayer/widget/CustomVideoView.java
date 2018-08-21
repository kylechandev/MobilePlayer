package com.fairhand.mobileplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 自定义VideoView
 */
public class CustomVideoView extends android.widget.VideoView {
    
    /**
     * 在代码中创建用
     */
    public CustomVideoView(Context context) {
        super(context);
    }
    
    /**
     * 当这个类在布局文件，系统通过该方法实例该类
     */
    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * 当需要设置样式时调用该方法
     */
    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 设置视频的宽和高
     * @param videoWidth 指定视频的宽
     * @param videoHeight 指定视频的高
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = videoWidth;
        params.height = videoHeight;
        setLayoutParams(params);
    }
}
