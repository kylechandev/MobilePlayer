package com.fairhand.mobileplayer.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fairhand.mobileplayer.IMusicPlayerService;
import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.AudioPlayerActivity;
import com.fairhand.mobileplayer.adapter.AudioPagerAdapter;
import com.fairhand.mobileplayer.entity.MediaItem;
import com.fairhand.mobileplayer.service.MusicPlayerService;
import com.fairhand.mobileplayer.utils.MusicUtil;
import com.fairhand.mobileplayer.utils.SaveCacheUtil;


import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * 音乐Pager
 *
 * @author FairHand
 */
public class AudioPagerFragment extends Fragment {
    
    private static final String TAG = AudioPagerFragment.class.getSimpleName();
    
    private ListView listView;
    
    private TextView noMedia;
    
    private ProgressBar loading;
    
    private TextView barMusicName;
    
    private TextView barMusician;
    
    private ImageView barPlayOrPauseMusic;
    
    private ImageView barMusicImage;
    
    private ImageView barMusicMenu;
    
    /**
     * 传入audio对象序列的KEY
     */
    private static final String AUDIO_LIST = "audiolist";
    
    /**
     * 当前点击音频位置
     */
    private static final String AUDIO_POSITION = "position";
    
    /**
     * 上下文，用以获取到服务
     */
    private Context context;
    
    /**
     * 音乐服务代理类，可通过此代理类调用服务类的方法
     */
    private IMusicPlayerService iMusicPlayerService;
    
    private View rootView;
    
    private BroadcastReceiver mReceiver;
    
    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 搜索
     */
    private SearchView searchView;
    
    /**
     * 当Activity与Fragment创建关联时调用
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        
        // 获取到音乐播放服务
        Intent serviceIntent = new Intent(context, MusicPlayerService.class);
        // 设置动作（表示启动能够响应这个action的活动）
        serviceIntent.setAction("com.fairhand.mobileplayer.OPENAUDIO");
        context.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "本地音乐初始化...");
        
        initData();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        Log.d(TAG, "音乐播放的onCreateView被调用了");
        
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.audio_fragment, container, false);
        }
        
        RelativeLayout musicBar = rootView.findViewById(R.id.music_bar);
        searchView = rootView.findViewById(R.id.searchview);
        listView = rootView.findViewById(R.id.audio_list_view);
        noMedia = rootView.findViewById(R.id.no_music);
        loading = rootView.findViewById(R.id.loading);
        barMusicName = rootView.findViewById(R.id.bar_music_name);
        barMusician = rootView.findViewById(R.id.bar_musician);
        barMusicMenu = rootView.findViewById(R.id.music_bar_menu);
        barMusicImage = rootView.findViewById(R.id.bar_music_image);
        barPlayOrPauseMusic = rootView.findViewById(R.id.bar_play_or_pause_music);
        
        listView.setTextFilterEnabled(true);
        
        // 获取保存的bar信息
        String name = SaveCacheUtil.getMusicBarMusicName(context, "MUSIC_NAME_KEY");
        String artist = SaveCacheUtil.getMusicBarMusicArtist(context, "MUSICIAN_KEY");
        long albumId = SaveCacheUtil.getMusicBarMusicAlbum(context, "ALBUM_KEY");
        
        if ((name != null) || (artist != null) || (albumId != 0)) {
            barMusicName.setText(name);
            barMusician.setText(artist);
            setMusicImage(MusicUtil.getAlbumArt(albumId));
        }
        
        setOnListener(musicBar);
        
        return rootView;
    }
    
    /**
     * 设置监听
     */
    private void setOnListener(RelativeLayout musicBar) {
        
        // 监听点击播放与暂停
        barPlayOrPauseMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iMusicPlayerService != null) {
                    try {
                        if (iMusicPlayerService.isPlaying()) {
                            iMusicPlayerService.pausePlayMusic();
                            barPlayOrPauseMusic.setImageResource(
                                    R.drawable.music_bar_play_selector);
                        } else {
                            iMusicPlayerService.startPlayMusic();
                            barPlayOrPauseMusic.setImageResource(
                                    R.drawable.music_bar_pause_selector);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        // 播放列表的监听
        barMusicMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建出播放列表对话框
                assert getFragmentManager() != null;
                new CustomBottomSheetDialogFragment().show(getFragmentManager(),
                        "Dialog");
            }
        });
        
        // 设置整个bar的监听打开播放界面
        musicBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动播放界面
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                intent.putExtra("FROM_BAR", true);// 标识来自BAR
                
                Bundle bundle = new Bundle();
                bundle.putSerializable(AUDIO_LIST, mediaItems);
                // 传入audio对象序列
                intent.putExtras(bundle);
                startActivityForResult(intent, 999);
            }
        });
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                
                // 获取到点击位置的音乐文件
                MediaItem mediaItem = mediaItems.get(position);
                
                // 获取到歌名 歌手 专辑图片路径
                String name = mediaItem.getMediaName();
                String artist = mediaItem.getMusicArtist();
                long albumId = mediaItem.getAlbumId();
                
                // 保存bar信息
                SaveCacheUtil.putMusicBarInfo(context,
                        "MUSIC_NAME_KEY", "MUSICIAN_KEY",
                        "POSITION_KEY", "ALBUM_KEY",
                        name, artist, position, albumId);
                
                // 传递数据列表 对象 序列化
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                
                Bundle bundle = new Bundle();
                bundle.putSerializable(AUDIO_LIST, mediaItems);
                // 传入audio对象序列
                intent.putExtras(bundle);
                // 传入位置
                intent.putExtra(AUDIO_POSITION, position);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(intent, 999);
                } else {
                    Toast.makeText(context, "当前手机不支持播放",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    listView.clearTextFilter();  // 清除ListView的过滤
                } else {
                    Log.d(TAG, "开始过滤");
                    listView.setFilterText(s); // 设置ListView的过滤关键词
                    
                }
                return false;
            }
        });
    }
    
    @Override
    public void onDestroy() {
        
        // 取消注册广播
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        
        // 解绑服务
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        
        // 移除所有消息
        handler.removeCallbacksAndMessages(null);
        
        super.onDestroy();
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 申请获取读取sdcard权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                getDataFromLocal();
            }
        }
        
        // 注册广播
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.UPDATE_VIEW_INFO);
        filter.addAction(MusicPlayerService.SYNC_BUTTON_STATE);
        context.registerReceiver(mReceiver, filter);
    }
    
    /**
     * 绑定服务后的回调接口
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 连接成功
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMusicPlayerService = IMusicPlayerService.Stub.asInterface(service);
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
        
        }
    };
    
    /**
     * 消息处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            if ((mediaItems != null) && (mediaItems.size() > 0)) {
                // 有数据 设置设配器 提示文本隐藏
                AudioPagerAdapter audioPagerAdapter = new AudioPagerAdapter(context,
                        mediaItems);
                listView.setAdapter(audioPagerAdapter);
                noMedia.setVisibility(View.GONE);
            } else {
                // 没有数据 提示文本显示
                noMedia.setVisibility(View.VISIBLE);
                noMedia.setText("没有发现本地音乐...");
            }
            loading.setVisibility(View.GONE);
        }
    };
    
    /**
     * 我的广播类
     *
     * @author FairHand
     * @describe 接收MusicPlayerService发送的广播更新bar的信息
     */
    private class MyReceiver extends BroadcastReceiver {
        
        @SuppressLint("NewApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (Objects.requireNonNull(intent.getAction()).equals(MusicPlayerService.UPDATE_VIEW_INFO)) {
                    // 获取到下一首歌的信息并更新bar的信息
                    String name = iMusicPlayerService.getCurrentPlayAudioName();
                    String artist = iMusicPlayerService.getCurrentPlayAudioArtist();
                    String album = MusicUtil.getAlbumArt(iMusicPlayerService.getAlbumId());
                    barMusicName.setText(name);
                    barMusician.setText(artist);
                    setMusicImage(album);
                } else if (intent.getAction().equals(MusicPlayerService.SYNC_BUTTON_STATE)) {
                    // 同步bar的播放按钮
                    if (iMusicPlayerService.isPlaying()) {
                        barPlayOrPauseMusic.setImageResource(
                                R.drawable.music_bar_pause_selector);
                    } else {
                        barPlayOrPauseMusic.setImageResource(
                                R.drawable.music_bar_play_selector);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    /**
     * 设置bar的专辑图片信息
     */
    private void setMusicImage(String albumArt) {
        Bitmap bitmap;
        RoundedBitmapDrawable roundedBitmap;
        if (albumArt == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_play_image);
            roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmap.setAntiAlias(true);// 抗锯齿
            roundedBitmap.setCornerRadius(52);// 圆角度数
            Glide.with(this).load(roundedBitmap).into(barMusicImage);
        } else {
            bitmap = BitmapFactory.decodeFile(albumArt);
            roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmap.setAntiAlias(true);// 抗锯齿
            roundedBitmap.setCornerRadius(52);// 圆角度数
            Glide.with(this).load(roundedBitmap).into(barMusicImage);
        }
    }
    
    /**
     * 返回数据回调方法（目的设置bar的播放暂停图标）
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 999:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "返回数据已收到！！！！！！！！！！！！");
                    boolean isPlaying = data.getBooleanExtra("IS_PLAYING", false);
                    
                    if (isPlaying) {
                        barPlayOrPauseMusic.setImageResource(R.drawable.music_bar_pause_selector);
                    } else {
                        barPlayOrPauseMusic.setImageResource(R.drawable.music_bar_play_selector);
                    }
                    
                    // 更新bar的歌曲信息
                    String name = data.getStringExtra("MUSIC_NAME");
                    String artist = data.getStringExtra("MUSIC_ARTIST");
                    String albumart = data.getStringExtra("ALBUM_ART");
                    barMusicName.setText(name);
                    barMusician.setText(artist);
                    setMusicImage(albumart);
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * 申请获取权限回调方法
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDataFromLocal();
                } else {
                    Toast.makeText(context, "无法获取本地数据读取权限", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * 获取本地数据
     * 1)遍历sdcard，后缀名
     * 2)从内容提供器中获取视频
     * 3)若为6.0以上，需动态读取sdcard的权限
     */
    private void getDataFromLocal() {
        mediaItems = new ArrayList<>();
        
        new Thread() {
            @Override
            public void run() {
                super.run();
                
                // 获取一个ContentResolver
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.TITLE,// 歌名
                        MediaStore.Audio.Media.DURATION,// 音乐总时长
                        MediaStore.Audio.Media.SIZE,// 音乐的文件大小
                        MediaStore.Audio.Media.DATA,// 音乐的绝对地址
                        MediaStore.Audio.Media.ARTIST,// 歌手
                        MediaStore.Audio.Media.ALBUM_ID,// 专辑图片ID
                        MediaStore.Audio.Media.ALBUM// 专辑名
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
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
                        
                        String artist = cursor.getString(4);// 歌手
                        mediaItem.setMusicArtist(artist);
                        
                        long albumId = cursor.getLong(5);// 专辑图片
                        mediaItem.setAlbumId(albumId);
                        
                        String album = cursor.getString(6);// 专辑名
                        mediaItem.setAlbum(album);
                        
                    }
                    cursor.close();
                }
                
                // handler发消息
                handler.sendEmptyMessage(0);
                
            }
        }.start();
        
    }
    
}
