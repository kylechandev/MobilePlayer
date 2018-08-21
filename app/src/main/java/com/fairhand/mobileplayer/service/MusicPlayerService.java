package com.fairhand.mobileplayer.service;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.fairhand.mobileplayer.IMusicPlayerService;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.AudioPlayerActivity;
import com.fairhand.mobileplayer.domain.MediaItem;
import com.fairhand.mobileplayer.utils.SaveCacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * 音乐服务类
 */
public class MusicPlayerService extends Service {
    
    /**
     * 广播发送KEY
     */
    // public static final String OPENAUDIO = "com.fairhand.mobileplayer.OPENAUDIO";
    
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
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        PLAY_MODE = SaveCacheUtil.getPlayMode(this, "PLAY_MODE");// 获取到保存的播放模式
        
        // 安卓8.0以上通知加入了 渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "music";// 渠道id
            String channelName = "音乐通知";// 显示给用户看的通知类型
            int importance = NotificationManager.IMPORTANCE_LOW;// 通知重要性
            
            // 实例化一个NotificationChannel
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);// 创建通知渠道
            
        }
    }
    
    /**
     * 每次服务启动时调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        getDataFromLocal();
        
        return super.onStartCommand(intent, flags, startId);
    }
    
    /**
     * 获取本地数据
     * 1)遍历sdcard，后缀名
     * 2)从内容提供器中获取音频
     * 3)若为6.0以上，需动态读取sdcard的权限
     */
    private void getDataFromLocal() {
        mediaItems = new ArrayList<>();
        
        new Thread() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                super.run();
                
                // 获取一个ContentResolver
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.TITLE,// 歌名
                        MediaStore.Audio.Media.DURATION,// 音乐总时长
                        MediaStore.Audio.Media.SIZE,// 音乐的文件大小
                        MediaStore.Audio.Media.DATA,// 音乐的绝对地址
                        MediaStore.Audio.Media.ARTIST,// 歌手
                        MediaStore.Audio.Media.ALBUM_ID// 专辑图片ID
                };
                Cursor cursor = resolver.query(uri, objs, null,
                        null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);
                        
                        String name = cursor.getString(0);// 音乐的名称
                        mediaItem.setMediaName(name);
                        
                        long duration = cursor.getLong(1);// 音乐的时长
                        mediaItem.setDuration(duration);
                        
                        long size = cursor.getLong(2);// 音乐的大小
                        mediaItem.setSize(size);
                        
                        String data = cursor.getString(3);// 音乐的播放地址
                        mediaItem.setData(data);
                        
                        String musicArtist = cursor.getString(4);// 歌手
                        mediaItem.setMusicArtist(musicArtist);
                        
                        long albumId = cursor.getLong(5);// 专辑图片
                        mediaItem.setAlbumId(albumId);
                        
                    }
                    cursor.close();
                }
                
            }
        }.start();
        
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
                        // notifyAcquireMusicIcon(OPENAUDIO);
                        
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
                        if (PLAY_MODE == MusicPlayerService.REPEAT_ALL) {// 顺序循环播放
                            int addPosition = finalPosition + 1;// 位置+1
                            if (addPosition >= mediaItems.size()) {// 到最后一首时位置置0从头开始
                                addPosition = 0;
                            }
                            openAudio(addPosition);
                        } else if (PLAY_MODE == MusicPlayerService.REPEAT_SINGLE) {// 单曲循环播放
                            startPlayMusic();// 重复再次播放当前歌曲
                        } else if (PLAY_MODE == MusicPlayerService.REPEAT_RAND) {// 随机播放
                            int randPosition = new Random().nextInt(mediaItems.size());// 获取一个随机位置播放
                            openAudio(randPosition);
                        }
                    }
                });
                
                // 设置播放出错的监听
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(MusicPlayerService.this, "播放出错", Toast.LENGTH_SHORT).show();
                        return true;// 返回false弹出提示对话框，反之不弹
                    }
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            Toast.makeText(MusicPlayerService.this, "还没有数据", Toast.LENGTH_SHORT).show();
        }
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
        
        // 设置通知点击跳转到AudioPlayerActivity
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("FROMNOTIFICATION", true);// 标识来自状态栏
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        
        // 使用Builder构造器创建Notification对象
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "music")
                                                          .setContentTitle("我真的没在播放：" + getCurrentPlayAudioName())
                                                          .setContentText(getCurrentPlayAudioArtist())
                                                          .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                                                          .setWhen(0)// 不显示时间
                                                          .setContentIntent(pendingIntent);
        
        // 设置为前台服务
        startForeground(1, notification.build());
        
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
        //noinspection StatementWithEmptyBody
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
        
        // 若获取的位置大于或f等于列表大小，将position置为0
        //noinspection StatementWithEmptyBody
        if (position >= mediaItems.size()) {
        } else {
            // 在处于第一个位置时点了很多次上一首后，点击下一首后直接设置position为2
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
        String[] projection = new String[] { "album_art" };
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
    
}
