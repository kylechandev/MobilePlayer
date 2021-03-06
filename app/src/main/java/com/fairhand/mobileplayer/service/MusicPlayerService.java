package com.fairhand.mobileplayer.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fairhand.mobileplayer.IMusicPlayerService;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.AudioPlayerActivity;
import com.fairhand.mobileplayer.activity.MainActivity;
import com.fairhand.mobileplayer.entity.MediaItem;
import com.fairhand.mobileplayer.utils.MusicUtil;
import com.fairhand.mobileplayer.utils.SaveCacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * 音乐服务类
 *
 * @author FairHand
 */
public class MusicPlayerService extends Service {
    
    private static final String TAG = MusicPlayerService.class.getSimpleName();
    
    /**
     * 广播发送 ACTION 播放完成自动切歌通知bar更新信息
     */
    public static final String UPDATE_VIEW_INFO = "com.fairhand.mobileplayer.OPENAUDIO";
    
    /**
     * 广播发送 ACTION 同步通知、播放器、bar的播放按钮状态
     */
    public static final String SYNC_BUTTON_STATE = "com.fairhand.mobileplayer.SYNC_BUTTON_STATE";
    
    /**
     * 广播发送 ACTION 通知的播放按钮点击
     */
    public static String STATUS_BAR_PLAY_CLICK_ACTION = "STATUS_BAR_COVER_CLICK_ACTION";
    
    /**
     * 广播发送 ACTION 通知的下一首按钮点击
     */
    public static String STATUS_BAR_NEXT_CLICK_ACTION = "STATUS_BAR_PLAY_CLICK_ACTION";
    
    /**
     * 广播发送 ACTION 通知的上一首按钮点击
     */
    public static String STATUS_BAR_PRE_CLICK_ACTION = "STATUS_BAR_PRE_CLICK_ACTION";
    
    /**
     * 广播发送 传递值KEY 通知的切歌
     */
    public static String STATUS_BAR_CHANGED_KEY = "STATUS_BAR_PRE_CLICK_KEY";
    
    /**
     * 广播发送 传递值KEY 是否仅仅同步按钮状态
     */
    public static String IS_ONLY_SYNC_BUTTON_KEY = "IS_ONLY_SYNC_BUTTON_KEY";
    
    /**
     * 更新通知信息MessageWhat
     */
    private static final int UPDATE_NOTIFICATION = 9;
    
    /**
     * 歌曲列表
     */
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 列表中点击音频的位置
     */
    public static int currentPosition;
    
    /**
     * 当前播放的音频文件对象
     */
    private MediaItem mediaItem;
    
    /**
     * 用于播放音乐
     */
    private MediaPlayer mMediaPlayer;
    
    /**
     * 自定义广播
     */
    private BroadcastReceiver mReceiver;
    
    /**
     * 通知管理类
     */
    private NotificationManager manager;
    
    /**
     * 用以自定义通知样式
     */
    private RemoteViews normalView;
    private RemoteViews bigView;
    
    /**
     * 构造通知
     */
    private NotificationCompat.Builder notification;
    
    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 1;
    
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 2;
    
    /**
     * 随机播放
     */
    public static final int REPEAT_RAND = 3;
    
    /**
     * 播放模式（默认为全部循环）
     */
    private int PLAY_MODE = REPEAT_ALL;
    
    /**
     * 重新播放
     */
    private boolean mResumeAfterCall = false;
    
    /**
     * 监听打电话自动暂停播放，打完继续播放
     */
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                AudioManager audioManager =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                assert audioManager != null;
                int ringVolume =
                        audioManager.getStreamVolume(AudioManager.STREAM_RING);
                if (ringVolume > 0) {
                    mResumeAfterCall = (isPlaying() || mResumeAfterCall);
                    pausePlayMusic();
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                mResumeAfterCall = (isPlaying() || mResumeAfterCall);
                pausePlayMusic();
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (mResumeAfterCall) {
                    startPlayMusic();
                    mResumeAfterCall = false;
                }
            }
        }
    };
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "服务onCreate");
        
        // 获取到保存的播放模式
        PLAY_MODE = SaveCacheUtil.getPlayMode(this, "PLAY_MODE");
        
        initData();
        
        // 电话服务
        TelephonyManager manager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        manager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 安卓8.0以上通知加入了 渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "music";// 渠道id
            String channelName = "音乐通知";// 显示给用户看的通知类型
            int importance = NotificationManager.IMPORTANCE_LOW;// 通知重要性
            
            // 实例化一个NotificationChannel
            NotificationChannel channel =
                    new NotificationChannel(channelId, channelName, importance);
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);// 创建通知渠道
            
        }
        
        // 注册广播
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.STATUS_BAR_PLAY_CLICK_ACTION);
        filter.addAction(MusicPlayerService.STATUS_BAR_PRE_CLICK_ACTION);
        filter.addAction(MusicPlayerService.STATUS_BAR_NEXT_CLICK_ACTION);
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    public void onDestroy() {
        
        Log.d(TAG, "服务onDestroy");
        // 取消注册广播
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        
        // 电话服务
        TelephonyManager manager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        manager.listen(mPhoneStateListener, 0);
    
        super.onDestroy();
    }
    
    /**
     * 每次服务启动时调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        Log.d(TAG, "服务onStartCommand");
        
        mediaItems = MusicUtil.mediaItems;
        
        return Service.START_STICKY;
    }
    
    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        
        // 获取到MusicPlayerService（注意不能new MusicPlayerService()！！！）
        MusicPlayerService service = MusicPlayerService.this;
        
        @Override
        public void openAudio(int position) {
            service.openAudio(position);
        }
        
        @Override
        public void startPlayMusic() {
            service.startPlayMusic();
        }
        
        @Override
        public void pausePlayMusic() {
            service.pausePlayMusic();
        }
        
        @Override
        public void stopPlayMusic() {
            service.stopPlayMusic();
        }
        
        @Override
        public int getCurrentPlayProgress() {
            return service.getCurrentPlayProgress();
        }
        
        @Override
        public int getCurrentAudioDuration() {
            return service.getCurrentAudioDuration();
        }
        
        @Override
        public String getCurrentPlayAudioName() {
            return service.getCurrentPlayAudioName();
        }
        
        @Override
        public String getCurrentPlayAudioArtist() {
            return service.getCurrentPlayAudioArtist();
        }
        
        @Override
        public String getPreparePlayAudioDataPath() {
            return service.getPreparePlayAudioDataPath();
        }
        
        @Override
        public void playPreviousAudio() {
            service.playPreviousAudio();
        }
        
        @Override
        public void playNextAudio() {
            service.playNextAudio();
        }
        
        @Override
        public void setPlayMode(int PLAY_MODE) {
            service.setPlayMode(PLAY_MODE);
        }
        
        @Override
        public int getPlayMode() {
            return service.getPlayMode();
        }
        
        @Override
        public boolean isPlaying() {
            return service.isPlaying();
        }
        
        @Override
        public void seekTo(int position) {
            service.seekTo(position);
        }
        
        @Override
        public long getAlbumId() {
            return service.getAlbumId();
        }
        
        @Override
        public boolean isCompletion() {
            return service.isCompletion();
        }
        
        @Override
        public String getPlayAudioNameForPosition(int position) {
            return service.getPlayAudioNameForPosition(position);
        }
    };
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }
    
    /**
     * 根据位置打开相应的音频文件
     */
    private void openAudio(int position) {
        currentPosition = position;
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            
            if ((position >= 0) && (position < mediaItems.size())) {
                mediaItem = mediaItems.get(position);
            } else {
                mediaItem = mediaItems.get(0);
            }
            
            if (mMediaPlayer != null) {
                // 重置MediaPlayer（以便下次点击播放另一首歌）
                mMediaPlayer.reset();
            }
            
            try {
                mMediaPlayer = new MediaPlayer();
                // 设置播放地址
                mMediaPlayer.setDataSource(mediaItem.getData());
                // 让MediaPlayer进入到准备状态（异步）
                mMediaPlayer.prepareAsync();
                // 设置播放准备好的监听
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        
                        // 发消息 通知Activity获取歌曲信息
                        EventBus.getDefault().post("MESSAGE_EVENT");
                        
                        startPlayMusic();// 准备好后开始播放音乐
                    }
                });
                
                // 设置播放完成的监听
                final int finalPosition = position;
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // 顺序循环播放
                        if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {
                            int addPosition = finalPosition + 1;// 位置+1
                            // 到最后一首时位置置0从头开始
                            if (addPosition >= mediaItems.size()) {
                                addPosition = 0;
                            }
                            openAudio(addPosition);
                            // 单曲循环播放
                        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
                            startPlayMusic();// 重复再次播放当前歌曲
                        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {// 随机播放
                            // 获取一个随机位置播放
                            int randPosition = new Random().nextInt(mediaItems.size());
                            openAudio(randPosition);
                        }
                        notifyPlayCompletion(UPDATE_VIEW_INFO);
                    }
                });
                
                // 设置播放出错的监听
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(MusicPlayerService.this,
                                "播放出错", Toast.LENGTH_SHORT).show();
                        return true;// 返回false弹出提示对话框，反之不弹
                    }
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            Toast.makeText(MusicPlayerService.this,
                    "还没有数据", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    /**
     * 通过action发送广播给AudioPagerFragment更新bar的信息
     */
    private void notifyPlayCompletion(String action) {
        Intent intent = new Intent(action);
        SaveCacheUtil.putCurrentPosition(this, "POSITION_KEY", currentPosition);
        sendBroadcast(intent);
    }
    
    /**
     * 播放音乐
     */
    private void startPlayMusic() {
        mMediaPlayer.start();
        
        createNotification();
    }
    
    /**
     * 暂停音乐
     */
    private void pausePlayMusic() {
        updatedNotification();
        mMediaPlayer.pause();
    }
    
    /**
     * 停止音乐
     */
    private void stopPlayMusic() {
        mMediaPlayer.stop();
    }
    
    /**
     * 获取到当前的播放进度
     */
    private int getCurrentPlayProgress() {
        return mMediaPlayer.getCurrentPosition();
    }
    
    /**
     * 获取到当前播放的音频的总时长
     */
    private int getCurrentAudioDuration() {
        return mMediaPlayer.getDuration();
    }
    
    /**
     * 获取到当前播放的音频的名字
     */
    private String getCurrentPlayAudioName() {
        return mediaItem.getMediaName();
    }
    
    /**
     * 通过传入的位置获取到播放的音频的名字
     */
    private String getPlayAudioNameForPosition(int position) {
        MediaItem currentMediaItem = mediaItems.get(position);
        return currentMediaItem.getMediaName();
    }
    
    /**
     * 获取到当前播放的音频的歌手
     */
    private String getCurrentPlayAudioArtist() {
        return mediaItem.getMusicArtist();
    }
    
    /**
     * 获取到准备播放的音频的路径
     */
    private String getPreparePlayAudioDataPath() {
        return mediaItem.getData();
    }
    
    /**
     * 播放上一首音乐
     */
    private void playPreviousAudio() {
        if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {
            currentPosition--;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
            currentPosition--;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {
            currentPosition = new Random().nextInt(mediaItems.size());
        }
        
        // 若获取的位置小于0，将position置为列表最后一个
        if (currentPosition < 0) {
            currentPosition = mediaItems.size() - 1;
        } else if (currentPosition >= mediaItems.size()) {
            // 在处于最后一个位置时点了很多次下一首后,
            // 点击上一首后直接设置position为mediaItems.size() - 2
            currentPosition = mediaItems.size() - 2;
        }
        openAudio(currentPosition);
        updatedNotification();
    }
    
    /**
     * 播放下一首音乐
     */
    private void playNextAudio() {
        if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {
            currentPosition++;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
            currentPosition++;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {
            currentPosition = new Random().nextInt(mediaItems.size());
        }
        
        // 若获取的位置大于或等于列表大小，将position置为0
        if (currentPosition >= mediaItems.size()) {
            currentPosition = 0;
        } else if (currentPosition < 0) {
            // 在处于第一个位置时点了很多次上一首后，点击下一首后直接设置position为1
            currentPosition = 1;
        }
        openAudio(currentPosition);
        updatedNotification();
    }
    
    /**
     * 设置播放模式
     */
    private void setPlayMode(int PLAY_MODE) {
        this.PLAY_MODE = PLAY_MODE;
        SaveCacheUtil.putPlayMode(this, "PLAY_MODE", PLAY_MODE);// 保存播放模式
    }
    
    /**
     * 获取到播放模式
     */
    private int getPlayMode() {
        return PLAY_MODE;
    }
    
    /**
     * 判断是否正在播放
     */
    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }
    
    /**
     * 设置音频进度条拖动播放
     *
     * @param position 拖动到的位置
     */
    private void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }
    
    /**
     * 判断是否播放完毕
     */
    private boolean isCompletion() {
        return false;
    }
    
    /**
     * 获取到ALBUM_ID
     */
    private long getAlbumId() {
        return mediaItem.getAlbumId();
    }
    
    /**
     * 创建通知
     */
    private void createNotification() {
        
        // 设置通知点击跳转到AudioPlayerActivity
        PendingIntent pendingIntent =
                PendingIntent.getActivities(this, 0,
                        makeIntentsArray(), PendingIntent.FLAG_CANCEL_CURRENT);
        
        // 自定义默认通知样式
        normalView = new RemoteViews(getPackageName(),
                R.layout.notification_layout);
        normalView.setImageViewBitmap(R.id.album_image, setMusicImage());
        normalView.setTextViewText(R.id.music_title, getCurrentPlayAudioName());
        normalView.setTextViewText(R.id.music_artist,
                getCurrentPlayAudioArtist() + " - " + mediaItem.getAlbum());
        
        // 默认通知的播放按钮点击事件
        Intent playIntent = new Intent(STATUS_BAR_PLAY_CLICK_ACTION);
        PendingIntent pendingPlay = PendingIntent.getBroadcast(this,
                0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        normalView.setOnClickPendingIntent(R.id.music_play, pendingPlay);
        
        // 默认通知的下一首按钮点击事件
        Intent nextIntent = new Intent(STATUS_BAR_NEXT_CLICK_ACTION);
        PendingIntent pendingNext = PendingIntent.getBroadcast(this,
                0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        normalView.setOnClickPendingIntent(R.id.music_next, pendingNext);
        
        // 自定义大通知样式
        bigView = new RemoteViews(getPackageName(), R.layout.big_notification);
        bigView.setImageViewBitmap(R.id.album_music, setMusicImage());
        bigView.setTextViewText(R.id.song_name, getCurrentPlayAudioName());
        bigView.setTextViewText(R.id.song_singer,
                getCurrentPlayAudioArtist() + " - " + mediaItem.getAlbum());
        
        // 大通知的上一首按钮点击事件
        Intent preIntent = new Intent(STATUS_BAR_PRE_CLICK_ACTION);
        PendingIntent pendingPre = PendingIntent.getBroadcast(this,
                0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        // 大通知按钮的点击事件
        bigView.setOnClickPendingIntent(R.id.pre, pendingPre);
        bigView.setOnClickPendingIntent(R.id.play, pendingPlay);
        bigView.setOnClickPendingIntent(R.id.next, pendingNext);
        
        // 使用Builder构造器创建Notification对象
        notification = new NotificationCompat.Builder(this, "music")
                               .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                               .setWhen(0)// 不显示时间
                               .setContent(normalView)
                               .setCustomBigContentView(bigView)
                               .setAutoCancel(false)
                               .setContentIntent(pendingIntent);
        
        manager.notify(1, notification.build());
        
    }
    
    /**
     * 创建一个Intent数组<br />
     * 该方法中指定的数组长度可以理解为返回时候的页面数<br />
     * makeRestartActivityTask方法指定了程序的Root Activity<br />
     * 数组的最后一个Intent为第一个显示的Activity，即点击Notification跳转到的页面
     */
    private Intent[] makeIntentsArray() {
        Intent[] intents = new Intent[2];
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(
                this, MainActivity.class));
        intents[0].setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        intents[1] = new Intent(
                this,
                AudioPlayerActivity.class);
        intents[1].putExtra("FROM_NOTIFICATION", true);// 标识来自状态栏
        
        return intents;
    }
    
    /**
     * 更新通知信息
     */
    private void updatedNotification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(UPDATE_NOTIFICATION);
            }
        }).start();
    }
    
    /**
     * 设置音乐专辑图片
     */
    private Bitmap setMusicImage() {
        //  设置专辑图片
        Bitmap bitmap;
        String albumArt = MusicUtil.getAlbumArt(getAlbumId());
        if (albumArt == null) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_play_image);
        } else {
            bitmap = BitmapFactory.decodeFile(albumArt);
        }
        return bitmap;
    }
    
    /**
     * 广播类<br />
     * 处理来自通知的按钮点击事件
     */
    private class MyReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            assert action != null;
            if (action.equals(STATUS_BAR_PLAY_CLICK_ACTION)) {
                // 处理通知的播放按钮点击事件
                if (isPlaying()) {
                    pausePlayMusic();
                } else {
                    startPlayMusic();
                }
                notifySyncButtonStateAndBarInfo(SYNC_BUTTON_STATE, true);
            } else if (action.equals(STATUS_BAR_PRE_CLICK_ACTION)) {
                // 处理通知的上一首按钮点击事件
                playPreviousAudio();
                notifySyncButtonStateAndBarInfo(SYNC_BUTTON_STATE, false);
            } else if (action.equals(STATUS_BAR_NEXT_CLICK_ACTION)) {
                // 处理通知的下一首按钮点击事件
                playNextAudio();
                notifySyncButtonStateAndBarInfo(SYNC_BUTTON_STATE, false);
            }
            updatedNotification();
            
        }
    }
    
    /**
     * 通知同步按钮状态并更新bar的信息
     * @param what 是否仅仅同步按钮状态
     */
    private void notifySyncButtonStateAndBarInfo(String action, boolean what) {
        Intent syncInent = new Intent(action);
        syncInent.putExtra(STATUS_BAR_CHANGED_KEY, currentPosition);
        syncInent.putExtra(IS_ONLY_SYNC_BUTTON_KEY, what);
        sendBroadcast(syncInent);
    }
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_NOTIFICATION:// 更新通知信息
                    if (isPlaying()) {
                        normalView.setImageViewResource(
                                R.id.music_play, R.drawable.ic_play_btn_pause);
                        bigView.setImageViewResource(
                                R.id.play, R.drawable.ic_play_btn_pause);
                    } else {
                        normalView.setImageViewResource(
                                R.id.music_play, R.drawable.ic_play_btn_play);
                        bigView.setImageViewResource(
                                R.id.play, R.drawable.ic_play_btn_play);
                    }
                    
                    manager.notify(1, notification.build());
                    SaveCacheUtil.putCurrentPosition(MusicPlayerService.this,
                            "POSITION_KEY", currentPosition);
                    break;
                
                default:
                    break;
            }
        }
    };
    
}
