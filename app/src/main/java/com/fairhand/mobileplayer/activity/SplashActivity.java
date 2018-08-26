package com.fairhand.mobileplayer.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.fairhand.mobileplayer.ActivityCollector;
import com.fairhand.mobileplayer.R;

/**
 * 启动页
 */
public class SplashActivity extends BaseActivity {
    
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ActivityCollector.addActivity(this);
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 一秒后执行这里
                startActivity();
            }
        }, 800);
        
    }
    
    @Override
    protected void onDestroy() {
        ActivityCollector.removeActivity(this);
        // 移除所有的消息和回调
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    
    /**
     * 跳转到主页面，并把当前页面关闭
     */
    private void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // 关闭当前页面
        finish();
    }
    
}
