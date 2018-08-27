package com.fairhand.mobileplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by FairHand on 2018/8/27.
 * 实现文字图片居中显示<br />
 * 把当前Button里的图片和文字全部靠左，再往右偏移计算好的宽度达到预期效果
 */
public class CustomImageButton extends android.support.v7.widget.AppCompatButton {
    
    public CustomImageButton(Context context) {
        super(context);
    }
    
    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas = getTopCanvas(canvas);
        super.onDraw(canvas);
    }
    
    private Canvas getTopCanvas(Canvas canvas) {
        // 返回包含控件left,top,right,bottom四个位置的Drawable的数组
        Drawable[] drawables = getCompoundDrawables();
        
        Drawable drawable = drawables[0];// 判断左面的drawable是否存在
        if (drawable == null) {
            drawable = drawables[2];// 判断右面的drawable是否存在
        }
        
        float textWidth = getPaint().measureText(getText().toString());// 获取文本的宽度
        int ImgWidth = drawable.getIntrinsicWidth();// 获取图片的宽度
        int buttonPadding = getCompoundDrawablePadding();// 获取图片与文本之间的距离
        float contentWidth = textWidth + ImgWidth + buttonPadding;// 获取当前总共所占宽度
        int rightPadding = (int) (getWidth() - contentWidth);// 计算右边应该保留的宽度
        setPadding(0, 0, rightPadding, 0); // 贴到Button左边
        float dx = (getWidth() - contentWidth) / 2;// 获取中心位置
        canvas.translate(dx, 0);// 整体右移
        return canvas;
        
    }
}
