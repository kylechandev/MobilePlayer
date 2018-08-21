package com.fairhand.mobileplayer.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fairhand.mobileplayer.IMusicPlayerService;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.service.MusicPlayerService;
import com.fairhand.mobileplayer.utils.TimeConvertUtil;
import com.fairhand.mobileplayer.widget.CustomLyricView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 音乐播放器
 * @author FairHand
 */
public class AudioPlayerActivity extends BaseActivity implements View.OnClickListener {
    
    private static final String TAG = AudioPlayerActivity.class.getSimpleName();
    
    /**
     * 播放进度更新Message What
     */
    private static final int PROGRESS = 1;
    
    /**
     * 显示歌词Message What
     */
    private static final int SHOW_LYRIC = 2;
    
    /**
     * 更新控制器信息Message What
     */
    private static final int UPDATE_CONTROLLER_UI = 3;
    
    /**
     * 更新歌曲信息Message What
     */
    private static final int UPDATE_MUSIC_UI = 4;
    
    /**
     * 声音管理器（调节声音）
     */
    private AudioManager mAudioManager;
    
    /**
     * 当前音量（设置SeekBar使用）
     */
    private int currentVoice;
    
    /**
     * 音乐服务代理类，可通过此代理类调用服务类的方法
     */
    private IMusicPlayerService iMusicPlayerService;
    
    /**
     * 当前点击音频位置KEY
     */
    private static final String AUDIO_POSITION = "position";
    
    /**
     * 点击音频的位置
     */
    private int position;
    
    // private MyReceiver receiver;
    
    /**
     * 动画
     */
    private ObjectAnimator objectAnimator;
    
    /**
     * 标识是否来自Bar
     */
    private boolean isFromBar;
    
    /**
     * 标识是否来自通知
     */
    private boolean isFromNotification;
    
    /**
     * 标识是否正在播放
     */
    private boolean isPlaying = true;
    
    /**
     * 判断是否按过上一首或下一首
     */
    private boolean isPressPreOrNext = false;
    
    /**
     * 布局组件
     */
    private LinearLayout musicVolumnCotroller;
    private RelativeLayout musicInfoAndLyric;
    private CircleImageView musicImage;
    private CustomLyricView customLyricView;
    private ImageView musicFaceBackButton;
    private ImageView musicShareButton;
    private TextView musicName;
    private TextView musicArtist;
    private TextView musicDuration;
    private TextView currentTime;
    private SeekBar seekbarVoice;
    private SeekBar seekbarMusic;
    private Button musicPlayMode;
    private Button previousMusic;
    private Button pauseOrPlayMusic;
    private Button nextMusic;
    private Button musicMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        
        initView();// 初始化组件
        
        initData();// 初始化数据
        
        getData();// 获取数据
        
        bindAndStartService();// 绑定并启动服务
        
        setListener();// 设置监听
        
        // 初始化动画
        objectAnimator = ObjectAnimator.ofFloat(musicImage,
                "rotation", 0f, 360f);// 旋转动画旋转中心为view的中心
        objectAnimator.setDuration(24000);// 持续24秒
        objectAnimator.setInterpolator(new LinearInterpolator());// 设置动画匀速
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);// 无限播放
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);// 重复播放模式
        objectAnimator.start();// 启动动画
        
    }
    
    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-08-14 17:48:51 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     * 初始化组件
     */
    private void initView() {
        musicVolumnCotroller = findViewById(R.id.music_volumn_cotroller);
        musicImage = findViewById(R.id.show_music_image);
        musicInfoAndLyric = findViewById(R.id.music_info_and_lyric);
        customLyricView = findViewById(R.id.show_lyric);
        musicFaceBackButton = findViewById(R.id.music_face_back_button);
        musicName = findViewById(R.id.music_name);
        musicArtist = findViewById(R.id.music_artist);
        musicShareButton = findViewById(R.id.music_share_button);
        seekbarVoice = findViewById(R.id.seekbar_voice);
        currentTime = findViewById(R.id.current_time);
        seekbarMusic = findViewById(R.id.seekbar_music);
        musicDuration = findViewById(R.id.music_duration);
        musicPlayMode = findViewById(R.id.music_play_mode);
        previousMusic = findViewById(R.id.previous_music);
        pauseOrPlayMusic = findViewById(R.id.pause_or_play_music);
        nextMusic = findViewById(R.id.next_music);
        musicMenu = findViewById(R.id.music_menu);
        
        musicInfoAndLyric.setOnClickListener(this);
        musicPlayMode.setOnClickListener(this);
        previousMusic.setOnClickListener(this);
        pauseOrPlayMusic.setOnClickListener(this);
        nextMusic.setOnClickListener(this);
        musicMenu.setOnClickListener(this);
        musicFaceBackButton.setOnClickListener(this);
        musicShareButton.setOnClickListener(this);
        
    }
    
    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-08-14 17:48:51 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     * 设置点击事件
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play_mode:
                setPlayMode();// 设置播放模式
                break;
            case R.id.previous_music:
                if (iMusicPlayerService != null) {
                    try {
                        // 上一曲
                        isPressPreOrNext = true;
                        iMusicPlayerService.playPreviousAudio();
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_pause_selector);
                        backData(true);
                        // 重新播放动画
                        objectAnimator.cancel();
                        setMusicImage();
                        objectAnimator.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.pause_or_play_music:
                pauseOrPlayMusic(false);// 处理暂停或是播放音乐
                try {
                    isPlaying = iMusicPlayerService.isPlaying();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                backData(isPlaying);
                break;
            case R.id.next_music:
                if (iMusicPlayerService != null) {
                    try {
                        // 下一曲
                        isPressPreOrNext = true;
                        iMusicPlayerService.playNextAudio();
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_pause_selector);
                        backData(true);
                        // 重新播放动画
                        objectAnimator.cancel();
                        setMusicImage();
                        objectAnimator.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.music_menu:
                break;
            case R.id.music_share_button:
                shareMusic();// 分享歌曲
                break;
            case R.id.music_face_back_button:
                try {
                    backData(iMusicPlayerService.isPlaying());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                finish();// 点击返回到上一个活动
                break;
            case R.id.music_info_and_lyric:
                if (customLyricView.getVisibility() == View.GONE) {
                    // 显示歌词，隐藏音乐转盘，显示音量控制
                    customLyricView.setVisibility(View.VISIBLE);
                    musicImage.setVisibility(View.GONE);
                    musicVolumnCotroller.setVisibility(View.VISIBLE);
                } else {
                    // 隐藏歌词，显示音乐转盘，隐藏音量控制
                    customLyricView.setVisibility(View.GONE);
                    musicImage.setVisibility(View.VISIBLE);
                    musicVolumnCotroller.setVisibility(View.GONE);
                }
                break;
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        /*// 注册广播
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.OPENAUDIO);
        registerReceiver(receiver, filter);*/
        
        // 注册EventBus
        EventBus.getDefault().register(this);
        
        handler.sendEmptyMessage(UPDATE_CONTROLLER_UI);
        
    }
    
    /**
     * 获取数据
     */
    public void getData() {
        isFromBar = getIntent().getBooleanExtra("FROMBAR", false);
        isFromNotification = getIntent().getBooleanExtra("FROMNOTIFICATION", false);
        
        if (!isFromBar || !isFromNotification) {
            position = getIntent().getIntExtra(AUDIO_POSITION, 0);
        }
        
    }
    
    /**
     * 绑定并启动服务
     */
    private void bindAndStartService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        // 设置动作（表示启动能够响应这个action的活动）
        serviceIntent.setAction("com.fairhand.mobileplayer.OPENAUDIO");
        // 绑定服务
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);// 避免实例化多个服务
    }
    
    /**
     * 绑定服务后的回调接口
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 当连接成功时回调
         */
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMusicPlayerService = IMusicPlayerService.Stub.asInterface(service);
            if (iMusicPlayerService != null) {
                try {
                    if (!isFromNotification && !isFromBar) {
                        // 从列表进入
                        iMusicPlayerService.openAudio(position);
                    } else {
                        // 从通知栏或bar进入
                        showViewData();
                        checkPlayMode();
                        pauseOrPlayMusic(true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * 当断开连接时回调
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (iMusicPlayerService != null) {
                try {
                    iMusicPlayerService.stopPlayMusic();
                    iMusicPlayerService = null;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    
    /**
     * 设置监听
     */
    private void setListener() {
        
        // 设置音频SeekBar状态变化的监听
        seekbarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 当手指滑动时，会引起Seekbar进度变化，回调此方法
             * @param fromUser 用户引起的为true，自动更新为false
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        iMusicPlayerService.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            /**
             * 当手指触碰的时候回调此方法
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            
            }
            
            /**
             * 当手指离开的时候回调此方法
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            
            }
        });
        
        // 声音的监听
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 当手指滑动时，会引起Seekbar进度变化，回调此方法
             * @param fromUser 用户引起的为true，自动更新为false
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateVoice(progress);
                }
            }
            
            /**
             * 当手指触碰的时候回调此方法
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            
            }
            
            /**
             * 当手指离开的时候回调此方法
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            
            }
        });
    }
    
    /**
     * 消息处理事件
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:// 更新播放进度条
                    try {
                        // 得到当前播放进度
                        int currentPosition = iMusicPlayerService.getCurrentPlayProgress();
                        // 设置SeekBar的进度
                        seekbarMusic.setProgress(currentPosition);
                        // 时间进度更新
                        currentTime.setText(TimeConvertUtil.stringForTime(currentPosition));
                        
                        // 每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                        
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                
                case SHOW_LYRIC:// 显示歌词
                    try {
                        // 得到当前的播放进度
                        int currentPosition = iMusicPlayerService.getCurrentPlayProgress();
                        // 将进度传入CustomLyricView控件，并计算下一句高亮歌词
                        customLyricView.setShowNextLyric(currentPosition);
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                
                case UPDATE_CONTROLLER_UI:// 更新UI
                    // 获取Audio系统服务
                    mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    assert mAudioManager != null;
                    // 得到当前音量
                    currentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    // 得到最大音量
                    // 最大音量 0~15个等级
                    int maxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    // 设置SeekBar关联最大音量
                    seekbarVoice.setMax(maxVoice);
                    // 设置SeekBar关联当前音量
                    seekbarVoice.setProgress(currentVoice);
                    // 设置SeekBar滑块颜色
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        seekbarVoice.getThumb().setColorFilter(ContextCompat.getColor(getBaseContext(),
                                R.color.progress_thumb), PorterDuff.Mode.SRC_ATOP);
                        seekbarMusic.getThumb().setColorFilter(ContextCompat.getColor(getBaseContext(),
                                R.color.progress_thumb), PorterDuff.Mode.SRC_ATOP);
                    }
                    
                    break;
                
                case UPDATE_MUSIC_UI:
                    try {
                        // 设置歌手
                        musicArtist.setText(iMusicPlayerService.getCurrentPlayAudioArtist());
                        // 设置歌名
                        musicName.setText(iMusicPlayerService.getCurrentPlayAudioName());
                        // 设置歌曲总时长
                        musicDuration.setText(TimeConvertUtil.stringForTime(iMusicPlayerService.getCurrentAudioDuration()));
                        // 设置进度条的最大值
                        seekbarMusic.setMax(iMusicPlayerService.getCurrentAudioDuration());
                        
                        if (!isPressPreOrNext) {
                            setMusicImage();
                        }
                        
                        // 发送消息通知SeekBar更新播放进度
                        handler.sendEmptyMessage(PROGRESS);
                        
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                    
                default:
                    break;
            }
        }
    };
    
    //    /**
    //     * 广播类
    //     */
    //    private class MyReceiver extends BroadcastReceiver {
    //
    //        @Override
    //        public void onReceive(Context context, Intent intent) {
    //            Log.d(TAG, "当前线程==" + Thread.currentThread());
    //            showData();
    //        }
    //    }
    
    /**
     * EventBus订阅方法
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showData(String messageEvent) {
        checkPlayMode();
        showViewData();
        handler.sendEmptyMessage(SHOW_LYRIC);// 发送消息显示并同步歌词
    }
    
    /**
     * 显示歌曲信息
     */
    private void showViewData() {
        handler.sendEmptyMessage(UPDATE_MUSIC_UI);
    }
    
    /**
     * 处理暂停或是播放音乐
     *
     * @param isFromOther 标识是否来自状态栏
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void pauseOrPlayMusic(boolean isFromOther) {
        if (iMusicPlayerService != null) {
            try {
                if (iMusicPlayerService.isPlaying()) {
                    if (isFromOther) {
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_pause_selector);
                        objectAnimator.resume();
                    } else {
                        // 音乐正在播放，点击暂停，设置按钮播放
                        iMusicPlayerService.pausePlayMusic();
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_play_selector);
                        objectAnimator.pause();// 暂停动画
                    }
                } else {
                    if (isFromOther) {
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_play_selector);
                        objectAnimator.pause();
                    } else {
                        // 音乐处于暂停，点击播放，设置按钮暂停
                        iMusicPlayerService.startPlayMusic();
                        pauseOrPlayMusic.setBackgroundResource(R.drawable.music_pause_selector);
                        objectAnimator.resume();// 恢复动画
                    }
                    
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    /**
     * 设置播放模式
     */
    private void setPlayMode() {
        try {
            int PLAY_MODE = iMusicPlayerService.getPlayMode();// 获取到播放模式
            if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {// 如果是全部，切换为单曲循环
                PLAY_MODE = MusicPlayerService.REPEAT_SINGLE;
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_single_mode_selector);
                Toast.makeText(AudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {// 如果是单曲，切换为随机播放
                PLAY_MODE = MusicPlayerService.REPEAT_RAND;
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_random_mode_selector);
                Toast.makeText(AudioPlayerActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
            } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {// 如果是随机，切换为全部循环
                PLAY_MODE = MusicPlayerService.REPEAT_ALL;
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_all_mode_selector);
                Toast.makeText(AudioPlayerActivity.this, "全部循环", Toast.LENGTH_SHORT).show();
            }
            
            // 保存播放模式
            iMusicPlayerService.setPlayMode(PLAY_MODE);
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 从列表点击另一首歌时校准播放模式
     */
    private void checkPlayMode() {
        try {
            int PLAY_MODE = iMusicPlayerService.getPlayMode();// 获取到播放模式
            if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_all_mode_selector);
            } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_single_mode_selector);
            } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {
                musicPlayMode.setBackgroundResource(R.drawable.music_repeat_random_mode_selector);
            }
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 设置音乐专辑图片
     */
    private void setMusicImage() throws RemoteException {
        //  设置专辑图片
        Bitmap bitmap;
        String albumArt = iMusicPlayerService.getAlbumArt(iMusicPlayerService.getAlbumId());
        if (albumArt == null) {
            Glide.with(getBaseContext()).load(R.drawable.default_play_image).into(musicImage);
            musicImage.setVisibility(View.VISIBLE);
        } else {
            bitmap = BitmapFactory.decodeFile(albumArt);
            Glide.with(getBaseContext()).load(bitmap).into(musicImage);
            musicImage.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 分享歌曲
     */
    private void shareMusic() {
        ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this);
        intentBuilder.setType("text/plain");
        try {
            intentBuilder.setText("分享" + iMusicPlayerService.getCurrentPlayAudioArtist() + "的单曲《"
                                          + iMusicPlayerService.getCurrentPlayAudioName() + "》");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        intentBuilder.setSubject("我不是音乐");
        intentBuilder.startChooser();
    }
    
    /**
     * 更新音量
     *
     * @param progress 要设置的音量
     */
    private void updateVoice(int progress) {
        // 设置音量 （类型，音量，flags为1调起系统的音量调节器，为0则不调）
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        seekbarVoice.setProgress(progress);
        currentVoice = progress;
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
            updateVoice(currentVoice);
            return false;// 返回true表示不显示系统音量调节，返回false或不返回反之
            // 当为音量-时
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice);
            return false;// 返回true表示不显示系统音量调节，返回false或不返回反之
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * 返回数据给AudioPagerFragment
     *
     * @param isPlaying 是否在播放
     */
    private void backData(boolean isPlaying) {
        Log.d(TAG, "准备返回数据啦啦啦啦啦啦");
        Intent returnData = new Intent();
        returnData.putExtra("ISPLAYING", isPlaying);
        returnData.putExtra("ISPRESSPREORNEXT", isPressPreOrNext);
        
        try {
            String name = iMusicPlayerService.getCurrentPlayAudioName();
            String artist = iMusicPlayerService.getCurrentPlayAudioArtist();
            String albumArt = iMusicPlayerService.getAlbumArt(iMusicPlayerService.getAlbumId());
            returnData.putExtra("MUSICNAME", name);
            returnData.putExtra("MUSICARTIST", artist);
            returnData.putExtra("ALBUMART", albumArt);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        
        setResult(RESULT_OK, returnData);
    }
    
    /**
     * 物理返回键监听
     */
    @Override
    public void onBackPressed() {
        try {
            backData(iMusicPlayerService.isPlaying());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        finish();
    }
    
    @Override
    protected void onDestroy() {
        /*// 取消注册广播
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }*/
        
        // 取消注册EventBus
        EventBus.getDefault().unregister(this);
        
        // 移除所有的消息
        handler.removeCallbacksAndMessages(null);
        
        // 解绑服务
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        
        super.onDestroy();
    }
    
}
