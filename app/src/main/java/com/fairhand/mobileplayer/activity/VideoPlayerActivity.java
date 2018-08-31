package com.fairhand.mobileplayer.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fairhand.mobileplayer.ActivityCollector;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.entity.MediaItem;
import com.fairhand.mobileplayer.utils.JudgeIsNetUriUtil;
import com.fairhand.mobileplayer.utils.ShowNetSpeedUtil;
import com.fairhand.mobileplayer.utils.TimeConvertUtil;
import com.fairhand.mobileplayer.widget.CustomVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 自定义系统播放器
 */
public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    
    private static final String TAG = VideoPlayerActivity.class.getSimpleName();
    
    /**
     * 视频进度的更新
     */
    private static final int PROGRRESS = 1;
    
    /**
     * 隐藏视频播放控制器
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    
    /**
     * 显示网速
     */
    private static final int SHOW_SPEED = 3;
    
    /**
     * 电量等级（注意字段为level）
     */
    private static final String BATTERY_LEVEL = "level";
    
    /**
     * 默认屏幕大小
     */
    private static final int DEFAULT_SCREEN = 0;
    
    /**
     * 全屏
     */
    private static final int FULL_SCREEN = 1;
    
    /**
     * 传入的video序列
     */
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 要播放的video的具体位置
     */
    private int position;
    
    /**
     * 屏幕的宽
     */
    private int screenWidth = 0;
    
    /**
     * 屏幕的高
     */
    private int screenHeight = 0;
    
    /**
     * 视频真实的宽
     */
    private int videoWidth = 0;
    
    /**
     * 视频真实的高
     */
    private int videoHeight = 0;
    
    /**
     * 视频播放地址
     */
    Uri uri;
    
    /**
     * 是否显示视频控制器
     */
    private boolean isShowMediaController = false;
    
    /**
     * 是否为全屏
     */
    private boolean isFullScreen = false;
    
    /**
     * 定义手势识别器
     */
    private GestureDetector mGesture;
    
    /**
     * 广播接收器
     */
    private MyReceiver myReceiver;
    
    /**
     * 声音管理器（调节声音）
     */
    private AudioManager mAudioManager;
    
    /**
     * 当前音量（设置SeekBar使用）
     */
    private int currentVoice;
    
    /**
     * 最大音量
     * 0~15个等级
     */
    private int maxVoice;
    
    /**
     * 是否为静音
     */
    private boolean isMute = false;
    
    /**
     * 开始手指按下点的Y坐标
     */
    private float startY;
    
    /**
     * 屏幕的高（上下滑动调节音量使用）
     */
    private float touchRang;
    
    /**
     * 当按下时的当前的音量（上下滑动调节音量使用）
     */
    private int mVoice;
    
    /**
     * 是否为网络资源
     */
    private boolean isNetUri;
    
    /**
     * 上一次播放进度位置
     */
    private int precurrentPosition;
    
    /**
     * 判断系统是否支持卡的判断
     */
    private boolean isUseSystem;
    
    /**
     * 视图
     */
    private RelativeLayout mediaController;
    private LinearLayout netBufferFace;
    private LinearLayout loadingFace;
    private CustomVideoView mCustomVideoView;
    private ImageView battery;
    private SeekBar seekbarVoice;
    private SeekBar seekbarVideo;
    private TextView videoName;
    private TextView systemTime;
    private TextView currentTime;
    private TextView viedoDuration;
    private Button videoVoice;
    private Button switchScreen;
    private Button videoPre;
    private Button videoPause;
    private Button videoNext;
    private TextView netBufferSpeed;
    private TextView loadingSpeed;
    private TextView brightnessTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// 先初始化父类，再初始化子类
        setContentView(R.layout.activity_video_player);
        ActivityCollector.addActivity(this);
        
        initViews();// 初始化视图
        
        initData();// 初始化数据
        
        setListener();// 设置监听
        
        getDatePath();// 获取视频播放路径
        
        setDataPath();// 设置视频播放路径
        
        // 设置控制面板
        // mVideoView.setMediaController(new MediaController(this));// 系统自带
    }
    
    @Override
    protected void onDestroy() {
        ActivityCollector.removeActivity(this);
        // 移除所有消息
        handler.removeCallbacksAndMessages(null);
        // 释放资源时，先释放子类，再释放父类
        if (myReceiver != null) {
            // 销毁时取消注册广播
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }
        
        super.onDestroy();
    }
    
    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-08-10 22:13:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     * 初始化视图
     */
    private void initViews() {
        mediaController = findViewById(R.id.media_controller);
        videoName = findViewById(R.id.video_name);
        battery = findViewById(R.id.battery);
        systemTime = findViewById(R.id.system_time);
        videoVoice = findViewById(R.id.video_voice);
        seekbarVoice = findViewById(R.id.seekbar_voice);
        currentTime = findViewById(R.id.current_time);
        seekbarVideo = findViewById(R.id.seekbar_video);
        viedoDuration = findViewById(R.id.viedo_duration);
        switchScreen = findViewById(R.id.switch_screen);
        videoPre = findViewById(R.id.video_pre);
        videoPause = findViewById(R.id.video_pause);
        videoNext = findViewById(R.id.video_next);
        mCustomVideoView = findViewById(R.id.video_view);
        netBufferFace = findViewById(R.id.buffer_face);
        loadingFace = findViewById(R.id.loading_face);
        loadingSpeed = findViewById(R.id.loading_speed);
        netBufferSpeed = findViewById(R.id.net_buffer_speed);
        brightnessTextView = findViewById(R.id.bright_text);
        
        videoVoice.setOnClickListener(this);
        switchScreen.setOnClickListener(this);
        videoPre.setOnClickListener(this);
        videoPause.setOnClickListener(this);
        videoNext.setOnClickListener(this);
    }
    
    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-08-10 22:13:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_voice:
                isMute = !isMute;
                updateVoice(currentVoice, isMute);
                break;
            case R.id.switch_screen:
                setFullScreenOrDefault();
                break;
            case R.id.video_pre:
                playPreVideo();
                break;
            case R.id.video_pause:
                playAndPauseVideo();
                break;
            case R.id.video_next:
                playNextVideo();
                break;
            default:
                break;
        }
        
        // 点击按钮时 移除隐藏消息 并重新发送隐藏消息
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
    }
    
    /**
     * 处理播放或是暂停视频
     */
    private void playAndPauseVideo() {
        if (mCustomVideoView.isPlaying()) {
            // 若正在播放，点击暂停
            mCustomVideoView.pause();
            // 设置标志为开始
            videoPause.setBackgroundResource(R.drawable.video_start_selector);
        } else {
            // 反之，点击播放
            mCustomVideoView.start();
            // 设置标志为暂停
            videoPause.setBackgroundResource(R.drawable.video_pause_selector);
        }
    }
    
    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            if (position >= mediaItems.size()) {
                position = mediaItems.size() - 2;
            } else {
                position--;
            }
            if ((position >= 0) && (position < mediaItems.size())) {
                // 显示加载页面
                loadingFace.setVisibility(View.VISIBLE);
                // 获取到上一个视频
                MediaItem mediaItem = mediaItems.get(position);
                // 设置参数
                videoName.setText(mediaItem.getMediaName());
                // 判断是否为网络资源
                isNetUri = JudgeIsNetUriUtil.isNetUri(mediaItem.getData());
                mCustomVideoView.setVideoPath(mediaItem.getData());
            }
            setButtonState(videoPre);// 设置按钮状态
        } else if (uri != null) {
            setButtonState(videoPre);// 设置按钮状态
        }
    }
    
    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            if (position < 0) {
                position = 1;
            } else {
                position++;
            }
            if ((position < mediaItems.size()) && (position >= 0)) {
                // 显示加载页面
                loadingFace.setVisibility(View.VISIBLE);
                // 获取到下一个视频
                MediaItem mediaItem = mediaItems.get(position);
                // 设置参数
                videoName.setText(mediaItem.getMediaName());
                // 判断是否为网络资源
                isNetUri = JudgeIsNetUriUtil.isNetUri(mediaItem.getData());
                mCustomVideoView.setVideoPath(mediaItem.getData());
            }
            setButtonState(videoNext);// 设置按钮状态
        } else if (uri != null) {
            setButtonState(videoNext);// 设置按钮状态
        }
    }
    
    /**
     * 设置按钮状态
     */
    private void setButtonState(Button buttonWhat) {
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            if (mediaItems.size() == 1) {
                // 若只有一个视频
                Toast.makeText(VideoPlayerActivity.this,
                        "一共只有一个视频哦", Toast.LENGTH_SHORT).show();
            } else {
                if ((position > mediaItems.size() - 1) && (buttonWhat == videoNext)) {
                    Toast.makeText(VideoPlayerActivity.this,
                            "这是最后一个视频啦", Toast.LENGTH_SHORT).show();
                } else if ((position < 0) && (buttonWhat == videoPre)) {
                    Toast.makeText(VideoPlayerActivity.this,
                            "已经是第一个视频啦", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (uri != null) {
            // 播放第三方视频，设置按钮不可用
            videoPre.setEnabled(false);
            videoNext.setEnabled(false);
        }
    }
    
    /**
     * 消息处理事件
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRRESS:
                    // 得到当前视频的播放进程
                    int currentPosition = mCustomVideoView.getCurrentPosition();
                    // 设置当前进度
                    seekbarVideo.setProgress(currentPosition);
                    // 更新文本播放进度
                    currentTime.setText(TimeConvertUtil.stringForTime(currentPosition));
                    // 设置系统时间
                    systemTime.setText(getSystemTime());
                    // 缓冲进度更新
                    if (isNetUri) {
                        // 网络视频设置缓冲效果
                        int buffer = mCustomVideoView.getBufferPercentage();// 0~100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        // 本地视频无缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }
                    
                    // 若系统版本不支持，自行进行判断是否卡
                    if (!isUseSystem) {
                        if (mCustomVideoView.isPlaying()) {
                            int bufferTime = currentPosition - precurrentPosition;// 缓冲
                            if (bufferTime < 500) {
                                // 缓冲时间小于500ms，显示加载
                                netBufferFace.setVisibility(View.VISIBLE);
                            } else {
                                netBufferFace.setVisibility(View.GONE);
                            }
                        } else {
                            netBufferFace.setVisibility(View.GONE);
                        }
                    }
                    
                    precurrentPosition = currentPosition;
                    
                    // 每秒更新一次
                    removeMessages(PROGRRESS);
                    sendEmptyMessageDelayed(PROGRRESS, 1000);
                    break;
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();// 隐藏控制面板
                    break;
                case SHOW_SPEED:
                    // 获取到网速
                    String netSpeedValue =
                            ShowNetSpeedUtil.getNetSpeed(VideoPlayerActivity.this);
                    // 显示网络速度
                    loadingSpeed.setText("正在加载中..." + netSpeedValue);
                    netBufferSpeed.setText("缓存中..." + netSpeedValue);
                    // 每秒更新一次
                    removeMessages(SHOW_SPEED);
                    sendEmptyMessageDelayed(SHOW_SPEED, 1000);
                    break;
            }
        }
    };
    
    /**
     * 获取系统时间
     */
    private String getSystemTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(new Date());
    }
    
    /**
     * 设置视频播放路径
     */
    private void setDataPath() {
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            MediaItem mediaItem = mediaItems.get(position);
            // 设置视频的名称
            videoName.setText(mediaItem.getMediaName());
            // 判断是否为网络资源
            isNetUri = JudgeIsNetUriUtil.isNetUri(mediaItem.getData());
            // 设置视频文件的路径
            mCustomVideoView.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            // 第三方视频打开
            videoName.setText(uri.toString());
            // 判断是否为网络资源
            isNetUri = JudgeIsNetUriUtil.isNetUri(uri.toString());
            mCustomVideoView.setVideoURI(uri);
        } else {
            Toast.makeText(VideoPlayerActivity.this,
                    "没有传入数据", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取视频播放路径
     */
    private void getDatePath() {
        // 得到播放地址
        uri = getIntent().getData();
        
        // 获取到video序列
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        
        // 获取到点击位置
        position = getIntent().getIntExtra("position", 0);
        
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        
        // 实例化广播接收器
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        // 当电量发生变化接收广播
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        // 注册电量广播
        registerReceiver(myReceiver, filter);
        
        // 实例化手势识别器
        mGesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            
            /**
             * 双击（双击暂停或是播放）
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                playAndPauseVideo();
                return super.onDoubleTap(e);
            }
            
            /**
             * 单击 （隐藏或是显示播放控制器）
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    // 隐藏
                    hideMediaController();
                    // 移除消息
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    // 显示
                    showMediaController();
                    // 发消息隐藏
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
                }
                
                return super.onSingleTapConfirmed(e);
            }
            
        });
        
        // 获取屏幕的大小
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        
        // 获取Audio系统服务
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert mAudioManager != null;
        // 得到当前音量
        currentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 得到最大音量
        maxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 设置SeekBar关联最大音量
        seekbarVoice.setMax(maxVoice);
        // 设置SeekBar关联当前音量
        seekbarVoice.setProgress(currentVoice);
        
        // 开始更新网速
        handler.sendEmptyMessage(SHOW_SPEED);
    }
    
    /**
     * 设置全屏播放或是默认播放
     */
    private void setFullScreenOrDefault() {
        if (isFullScreen) {
            // 默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            // 设置全屏
            setVideoType(FULL_SCREEN);
        }
    }
    
    /**
     * 设置视频的屏幕类型（全屏或默认）
     */
    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN:
                // 设置视频为全屏
                mCustomVideoView.setVideoSize(screenWidth, screenHeight);
                // 设置按钮状态为默认
                switchScreen.setBackgroundResource(R.drawable.switch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN:
                // 视频的真实宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;
                // 修改后视频的宽和高
                int width = screenWidth;
                int height = screenHeight;
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                // 设置视频的宽高
                mCustomVideoView.setVideoSize(width, height);
                // 设置按钮状态为全屏
                switchScreen.setBackgroundResource(R.drawable.switch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }
    
    /**
     * 广播接收器
     */
    class MyReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收到广播发出的电量等级
            int level = intent.getIntExtra(BATTERY_LEVEL, 0);// 电量等级0~100
            Log.d(TAG, "当前电量为" + level + "%");
            // 设置电池图标
            setBattery(level);
        }
    }
    
    /**
     * 设置电池图标
     *
     * @param level 当前电量等级
     */
    private void setBattery(int level) {
        if (level <= 20) {
            battery.setImageResource(R.drawable.ic_battery_20_pink_a100_24dp);
        } else if (level <= 30) {
            battery.setImageResource(R.drawable.ic_battery_30_pink_a100_24dp);
        } else if (level <= 50) {
            battery.setImageResource(R.drawable.ic_battery_50_blue_400_24dp);
        } else if (level <= 60) {
            battery.setImageResource(R.drawable.ic_battery_60_blue_400_24dp);
        } else if (level <= 80) {
            battery.setImageResource(R.drawable.ic_battery_80_teal_a700_24dp);
        } else if (level <= 90) {
            battery.setImageResource(R.drawable.ic_battery_90_teal_a700_24dp);
        } else {
            battery.setImageResource(R.drawable.ic_battery_full_teal_a400_24dp);
        }
    }
    
    /**
     * 设置监听
     */
    private void setListener() {
        
        // 设置准备好的监听
        mCustomVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // 当底层解码准备好的时候
            @Override
            public void onPrepared(MediaPlayer mp) {
                
                // 获取到视频的真实高宽
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                
                int duration = mCustomVideoView.getDuration();// 得到视频的总时长
                
                seekbarVideo.setMax(duration);// 关联SeekBar的总长度
                
                viedoDuration.setText(TimeConvertUtil.stringForTime(duration));// 设置视频的总时长
                
                hideMediaController();// 默认隐藏视频控制器
                
                handler.sendEmptyMessage(PROGRRESS);// 发送消息改变UI
                
                setVideoType(DEFAULT_SCREEN);// 默认屏幕尺寸播放
                
                mCustomVideoView.start();// 开始播放
                
                // 视频准备好播放后隐藏加载页面
                loadingFace.setVisibility(View.GONE);
                
            }
        });
        
        // 设置播放出错的监听
        mCustomVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlayerActivity.this,
                        "播放出错了", Toast.LENGTH_SHORT).show();
                return false;// 返回false弹出提示对话框，反之不弹
            }
        });
        
        // 设置播放完成的监听
        mCustomVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VideoPlayerActivity.this,
                        "播放完成了", Toast.LENGTH_SHORT).show();
                // 播放结束将按钮设置为播放
                videoPause.setBackgroundResource(R.drawable.video_start_selector);
            }
        });
        
        // 设置视频SeekBar状态变化的监听
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 当手指滑动时，会引起Seekbar进度变化，回调此方法
             * @param fromUser 用户引起的为true，自动更新为false
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCustomVideoView.seekTo(progress);
                }
            }
            
            /**
             * 当手指触碰的时候回调此方法
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 移除隐藏消息
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            }
            
            /**
             * 当手指离开的时候回调此方法
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 发送隐藏消息
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
                
            }
        });
        
        // 设置声音SeekBar状态变化的监听
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 当手指滑动时，会引起Seekbar进度变化，回调此方法
             * @param fromUser 用户引起的为true，自动更新为false
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isMute = progress == 0;
                    updateVoice(progress, isMute);
                }
            }
            
            /**
             * 当手指触碰的时候回调此方法
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 移除隐藏消息
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            }
            
            /**
             * 当手指离开的时候回调此方法
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 发送隐藏消息
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
            }
            
        });
        
        // 设置视频播放卡的监听
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isUseSystem = true;
            mCustomVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:// 视频开始卡顿
                            netBufferFace.setVisibility(View.VISIBLE);// 显示加载
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:// 卡顿结束
                            netBufferFace.setVisibility(View.GONE);// 隐藏加载
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        } else {
            isUseSystem = false;
        }
    }
    
    /**
     * 更新音量
     *
     * @param progress 要设置的音量
     * @param isMute   是否静音
     */
    private void updateVoice(int progress, boolean isMute) {
        if (isMute) {
            // 静音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
            // 设置按钮为静音
            videoVoice.setBackgroundResource(R.drawable.mute_selector);
        } else {
            // 设置音量 （类型，音量，flags为1调起系统的音量调节器，为0则不调）
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
            // 设置按钮为声音
            videoVoice.setBackgroundResource(R.drawable.voice_selector);
        }
    }
    
    /**
     * 处理手势事件（上下滑动调节音量）
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        // 把手势事件传递给手势识别器
        mGesture.onTouchEvent(event);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                // 记录按下时点坐标的值
                startY = event.getY();
                // 获取到当前音量值
                mVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // 获取到屏幕高度
                touchRang = Math.min(screenWidth, screenHeight);// screenHeight
                // 移除消息
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                // 手指滑动到某个位置的坐标
                float endY = event.getY();
                float endX = event.getX();
                // 滑动的距离
                float distanceY = startY - endY;
                
                if (endX < screenWidth / 2) {
                    // 左屏幕，调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > 20) {
                        if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                            setBrightness(6);
                        }
                        if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                            setBrightness(-6);
                        }
                        brightnessTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 右屏幕，调节声音
                    if (distanceY > 20) {
                        // 滑动屏幕的距离：总距离 = 改变的音量：最大音量
                        // 改变的音量 （改变的音量 = (滑动屏幕距离 / 总距离) * 最大音量）
                        float deltaVoice = (distanceY / touchRang) * maxVoice;
                        // 最终音量 （最终音量 = 原来的音量 + 改变的音量）
                        int finalVoice = (int) Math.min(Math.max(mVoice + deltaVoice, 0), maxVoice);
                        // 只有当改变的音量不为0时，才进行更新音量操作
                        if (deltaVoice != 0) {
                            updateVoice(finalVoice, false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP: // 拿起
                // 发送消息
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
                brightnessTextView.setVisibility(View.GONE);
                break;
        }
        return super.onTouchEvent(event);
    }
    
    /**
     * 设置屏幕亮度<br />
     * 0 最暗<br />
     * 1 最亮
     *
     * @param brightness 调节速度
     */
    @SuppressLint("SetTextI18n")
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = params.screenBrightness + brightness / 255.0f;
        if (params.screenBrightness > 1) {
            // 最高亮度
            params.screenBrightness = 1;
        } else if (params.screenBrightness < 0) {
            // 最低亮度
            params.screenBrightness = (float) 0;
        }
        getWindow().setAttributes(params);
        float bright = params.screenBrightness;
        brightnessTextView.setText((int) Math.ceil(bright * 100) + "%");
    }
    
    /**
     * 显示视频控制器
     */
    private void showMediaController() {
        mediaController.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }
    
    /**
     * 隐藏视频控制器
     */
    private void hideMediaController() {
        mediaController.setVisibility(View.GONE);
        isShowMediaController = false;
    }
    
    /**
     * 物理按键监听
     * 实现关联Voice的SeekBar与系统音量调节
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当为音量+键时
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
            return true;// 返回true表示不显示系统音量调节，返回false或不返回反之
            // 当为音量-时
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
            return true;// 返回true表示不显示系统音量调节，返回false或不返回反之
        }
        return super.onKeyDown(keyCode, event);
    }
}
