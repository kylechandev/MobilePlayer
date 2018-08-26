package com.fairhand.mobileplayer.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.VideoPlayerActivity;
import com.fairhand.mobileplayer.adapter.VideoPagerAdapter;
import com.fairhand.mobileplayer.domain.MediaItem;


import java.util.ArrayList;
import java.util.Objects;

public class VideoPagerFragment extends Fragment {
    
    /**
     * 读取sdcard请求码
     */
    private static final int REQUEST_READ_SDCARD = 1;
    
    /**
     * 传入video对象序列的KEY
     */
    private static final String VIDEO_LIST = "videolist";
    
    /**
     * 当前点击视频位置
     */
    private static final String VIDEO_POSITION = "position";
    
    private ListView listView;
    
    private TextView noMedia;
    
    private ProgressBar loading;
    
    private View rootView;
    
    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mediaItems;
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            if ((mediaItems != null) && (mediaItems.size() > 0)) {
                // 有数据 设置设配器 提示文本隐藏
                VideoPagerAdapter vedioPagerAdapter = new VideoPagerAdapter(getContext(),
                        mediaItems);
                listView.setAdapter(vedioPagerAdapter);
                noMedia.setVisibility(View.GONE);
            } else {
                // 没有数据 提示文本显示
                noMedia.setVisibility(View.VISIBLE);
            }
            loading.setVisibility(View.GONE);
        }
    };
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.video_fragment, container, false);
        }
        
        listView = rootView.findViewById(R.id.video_list_view);
        noMedia = rootView.findViewById(R.id.no_media);
        loading = rootView.findViewById(R.id.loading);
        
        // 申请获取读取sdcard权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_SDCARD);
            } else {
                getDataFromLocal();
            }
        }
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                // MediaItem mediaItem = mediaItems.get(position);// 获取当前位置点击的视频
                
                // 调起系统所有的播放器（隐式Intent）
                /*Intent intent = new Intent();
                intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
                getContext().startActivity(intent);*/
                
                // 调用自己写的播放器（显示Intent）
                /*Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                // 设置数据和类型
                intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
                getContext().startActivity(intent);*/
                
                // 传递数据列表 对象 序列化
                Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                
                Bundle bundle = new Bundle();
                bundle.putSerializable(VIDEO_LIST, mediaItems);
                // 传入video对象序列
                intent.putExtras(bundle);
                // 传入位置
                intent.putExtra(VIDEO_POSITION, position);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getContext().startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "当前手机不支持播放", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        return rootView;
    }
    
    /**
     * 申请获取权限回调方法
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_SDCARD:
                if ((grantResults.length > 0) && (
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getDataFromLocal();// 开始获取本地数据
                } else {
                    Toast.makeText(getContext(), "无法获取权限", Toast.LENGTH_SHORT).show();
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
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                super.run();
                
                // 获取一个ContentResolver
                ContentResolver resolver = Objects.requireNonNull(getContext()).getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,// 视频文件在上的sdcard的名字
                        MediaStore.Video.Media.DURATION,// 视频总时长
                        MediaStore.Video.Media.SIZE,// 视频的文件大小
                        MediaStore.Video.Media.DATA,// 视频的绝对地址
                };
                Cursor cursor = resolver.query(uri, objs, null,
                        null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);
                        
                        String name = cursor.getString(0);// 视频的名称
                        mediaItem.setMediaName(name);
                        
                        long duration = cursor.getLong(1);// 视频的时长
                        mediaItem.setDuration(duration);
                        
                        long size = cursor.getLong(2);// 视频的大小
                        mediaItem.setSize(size);
                        
                        String data = cursor.getString(3);// 视频的播放地址
                        mediaItem.setData(data);
                        
                    }
                    cursor.close();
                }
                
                // handler发消息
                handler.sendEmptyMessage(0);
                
            }
        }.start();
        
    }
    
    // /**
    // * 获取视频缩略图
    // * @param videoPath 视频地址
    // */
    /*private static Bitmap getVideoThumb(String videoPath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (videoPath != null) {
            mediaMetadataRetriever.setDataSource(videoPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return mediaMetadataRetriever.getScaledFrameAtTime(12138,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 100, 100);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }*/
}
