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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fairhand.mobileplayer.IMusicPlayerService;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.AudioPlayerActivity;
import com.fairhand.mobileplayer.activity.MainActivity;
import com.fairhand.mobileplayer.domain.MediaItem;
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
     * 广播发送 KEY
     */
    public static final String UPDATE_VIEW_INFO = "com.fairhand.mobileplayer.OPENAUDIO";
    
    /**
     * 更新通知信息MessageWhat
     */
    private static final int UPDATENOTIFICATION = 9;
    
    /**
     * 歌曲列表
     */
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 列表中点击音频的位置
     */
    private int position;
    
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
    private MyReceiver receiver;
    
    private NotificationManager manager;
    
    private RemoteViews bigView;
    
    private RemoteViews normalView;
    
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
     * 通知的播放按钮点击
     */
    public static String STATUS_BAR_PLAY_CLICK_ACTION = "status_bar_cover_click_action";
    
    /**
     * 通知的下一首按钮点击
     */
    public static String STATUS_BAR_NEXT_CLICK_ACTION = "status_bar_play_click_action";
    
    /**
     * 通知的上一首按钮点击
     */
    public static String STATUS_BAR_PRE_CLICK_ACTION = "status_bar_pre_click_action";
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "服务onCreate");
        
        // 获取到保存的播放模式
        PLAY_MODE = SaveCacheUtil.getPlayMode(this, "PLAY_MODE");
        
        initData();
        
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
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.STATUS_BAR_PLAY_CLICK_ACTION);
        filter.addAction(MusicPlayerService.STATUS_BAR_PRE_CLICK_ACTION);
        filter.addAction(MusicPlayerService.STATUS_BAR_NEXT_CLICK_ACTION);
        registerReceiver(receiver, filter);
    }
    
    @Override
    public void onDestroy() {
        
        Log.d(TAG, "服务onDestroy");
        // 取消注册广播
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        
        super.onDestroy();
    }
    
    /**
     * 每次服务启动时调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        Log.d(TAG, "服务onStartCommand");
        
        mediaItems = (ArrayList<MediaItem>) intent.getSerializableExtra("TOSERVICE");
        
        Log.d(TAG, "看看你为社么空" + mediaItems);
        
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
        public String getAlbumArt(long album_id) {
            return service.getAlbumArt(album_id);
        }
        
        @Override
        public boolean isCompletion() {
            return service.isCompletion();
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
        this.position = position;
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
                        // 通知activity获取歌曲信息
                        // notifyAcquireMusicIcon(UPDATE_VIEW_INFO);
                        
                        // 发消息
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
        sendBroadcast(intent);
    }
    
    //    /**
    //     * 通过action发送广播给activity更新信息
    //     */
    //    private void notifyAcquireMusicIcon(String action) {
    //        Intent intent = new Intent(action);
    //        sendBroadcast(intent);
    //    }
    
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
        // 去除得到的音频名中的歌手名以及文件的后缀名
        return mediaItem.getMediaName();
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
            position--;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
            position--;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {
            position = new Random().nextInt(mediaItems.size());
        }
        
        // 若获取的位置小于0，将position置为0
        // noinspection StatementWithEmptyBody
        if (position < 0) {
        } else {
            // 在处于最后一个位置时点了很多次下一首后，点击上一首后直接设置position为mediaItems.size() - 2
            if (position >= mediaItems.size()) {
                position = mediaItems.size() - 2;
            }
            openAudio(position);
        }
        
    }
    
    /**
     * 播放下一首音乐
     */
    private void playNextAudio() {
        if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {
            position++;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {
            position++;
        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {
            position = new Random().nextInt(mediaItems.size());
        }
        
        // 若获取的位置大于或等于列表大小，将position置为0
        // noinspection StatementWithEmptyBody
        if (position >= mediaItems.size()) {
        } else {
            // 在处于第一个位置时点了很多次上一首后，点击下一首后直接设置position为1
            if (position < 0) {
                position = 1;
            }
            openAudio(position);
        }
        
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
        return mediaItems.get(position).getAlbumId();
    }
    
    /**
     * 获取到本地音乐的专辑图片存储地址
     */
    private String getAlbumArt(long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cursor = getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Long.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        assert cursor != null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToNext();
            album_art = cursor.getString(0);
        }
        cursor.close();
        return album_art;
        
    }
    
    /**
     * 传入audio对象序列的KEY
     */
    private static final String AUDIO_LIST = "audiolist";
    
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
        
        updatedNotification();
        
    }
    
    /**
     * 创建一个Intent数组
     * 该方法中指定的数组长度可以理解为返回时候的页面数
     * makeRestartActivityTask方法指定了程序的Root Activity
     * 数组的最后一个Intent为第一个显示的Activity，即点击Notification跳转到的页面
     */
    private Intent[] makeIntentsArray() {
        Intent[] intents = new Intent[2];
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(
                this, MainActivity.class));
        
        intents[1] = new Intent(
                this,
                AudioPlayerActivity.class);
        intents[1].putExtra("FROMNOTIFICATION", true);// 标识来自状态栏
        
        Bundle bundle = new Bundle();
        bundle.putSerializable(AUDIO_LIST, mediaItems);
        // 传入audio对象序列
        intents[1].putExtras(bundle);
        return intents;
    }
    
    /**
     * 更新通知信息
     */
    private void updatedNotification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(UPDATENOTIFICATION);
            }
        }).start();
    }
    
    /**
     * 设置音乐专辑图片
     */
    private Bitmap setMusicImage() {
        //  设置专辑图片
        Bitmap bitmap;
        String albumArt = getAlbumArt(getAlbumId());
        if (albumArt == null) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_play_image);
        } else {
            bitmap = BitmapFactory.decodeFile(albumArt);
        }
        return bitmap;
    }
    
    /**
     * 广播类
     * 处理来自通知的按钮点击事件
     */
    private class MyReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            assert action != null;
            // 处理通知的播放按钮点击事件
            if (action.equals(STATUS_BAR_PLAY_CLICK_ACTION)) {
                if (isPlaying()) {
                    pausePlayMusic();
                } else {
                    startPlayMusic();
                }
                // 处理通知的上一首按钮点击事件
            } else if (action.equals(STATUS_BAR_PRE_CLICK_ACTION)) {
                playPreviousAudio();
                // 处理通知的下一首按钮点击事件
            } else if (action.equals(STATUS_BAR_NEXT_CLICK_ACTION)) {
                playNextAudio();
            }
            updatedNotification();
        }
    }
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATENOTIFICATION:// 更新通知信息
                    if (isPlaying()) {
                        normalView.setImageViewResource(
                                R.id.music_play, R.drawable.ic_play_btn_pause);
                        bigView.setImageViewResource(
                                R.id.play, R.drawable.ic_play_btn_pause);
                        Log.d(TAG, "设置按钮为暂停");
                    } else {
                        normalView.setImageViewResource(
                                R.id.music_play, R.drawable.ic_play_btn_play);
                        bigView.setImageViewResource(
                                R.id.play, R.drawable.ic_play_btn_play);
                        Log.d(TAG, "设置按钮为播放");
                    }
                    
                    manager.notify(1, notification.build());
                    break;
                
                default:
                    break;
            }
        }
    };
    
}
