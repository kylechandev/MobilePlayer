package com.fairhand.mobileplayer.pager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.activity.VideoPlayerActivity;
import com.fairhand.mobileplayer.adapter.NetVideoPagerAdapter;
import com.fairhand.mobileplayer.entity.MediaItem;
import com.fairhand.mobileplayer.utils.FindNetVideoUtil;
import com.fairhand.mobileplayer.widget.XListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class NetVideoPagerFragment extends Fragment {
    
    private static final String TAG = NetVideoPagerFragment.class.getSimpleName();
    
    @ViewInject(R.id.netvideo_list_view)
    private XListView mXListView;
    
    @ViewInject(R.id.no_net)
    private TextView noNet;
    
    @ViewInject(R.id.loading)
    private ProgressBar loading;
    
    private ArrayList<MediaItem> mediaItems;
    
    private NetVideoPagerAdapter netVideoPagerAdapter;
    
    /**
     * 联网请求到的json数据
     */
    private String publicResult;
    
    /**
     * 传入video对象序列的KEY
     */
    private static final String VIDEO_LIST = "videolist";
    
    /**
     * 当前点击视频位置
     */
    private static final String VIDEO_POSITION = "position";
    
    /**
     * 判断是否有网络连接
     */
    private boolean havaNet;
    
    private View rootView;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "网络视频初始化...");
        
        initData();// 初始化数据
        
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.net_video_fragment, container, false);
        }
        // 第一个参数为NetVedioPagerFragment.this，第二个参数为布局
        // 作用：将布局view与NetVedioPagerFragment关联
        x.view().inject(this, rootView);
        
        // 设置点击事件
        mXListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 传递数据列表 对象 序列化
                Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                
                Bundle bundle = new Bundle();
                bundle.putSerializable(VIDEO_LIST, mediaItems);
                // 传入video对象序列
                intent.putExtras(bundle);
                // 传入位置
                intent.putExtra(VIDEO_POSITION, position - 1);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(getContext()).startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "当前手机不支持播放", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Enable pull up load more feature.
        mXListView.setPullLoadEnable(true);
        
        // 设置刷新监听
        mXListView.setXListViewListener(new XListView.IXListViewListener() {
            /**
             * 刷新
             */
            @Override
            public void onRefresh() {
                getDataFromNet();
            }
            
            /**
             * 加载更多
             */
            @Override
            public void onLoadMore() {
                if (havaNet) {
                    mediaItems.addAll(parseJson(publicResult));
                    netVideoPagerAdapter.notifyDataSetChanged();
                    onLoad();
                }
            }
        });
        
        return rootView;
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        /*String cacheData = getContext().getSharedPreferences("video",
                        Context.MODE_PRIVATE).getString(FindNetVideoUtil.NET_URL, null);
        if (cacheData != null) {
            Log.d(TAG, "缓存数据：" + cacheData);
            processData(cacheData);
        } else {
            getDataFromNet();
        }*/
        getDataFromNet();
    }
    
    /**
     * 联网请求数据
     */
    private void getDataFromNet() {
        // 联网
        // 向FindNetVideoUtil.NET_URL请求数据
        RequestParams params = new RequestParams(FindNetVideoUtil.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            
            /**
             * 联网成功
             */
            @Override
            public void onSuccess(String result) {
                publicResult = result;
    
                /*SharedPreferences.Editor editor =
                        getContext().getSharedPreferences("video", Context.MODE_PRIVATE).edit();
                editor.putString(FindNetVideoUtil.NET_URL, publicResult);
                editor.apply();*/
                
                havaNet = true;
                
                processData(publicResult);// 处理数据
            }
            
            /**
             * 联网失败
             */
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                // 出错显示提示文本
                noNet.setVisibility(View.VISIBLE);
                // 隐藏ProgressBar
                loading.setVisibility(View.GONE);
                
                havaNet = false;
            }
            
            @Override
            public void onCancelled(CancelledException cex) {
            }
            
            @Override
            public void onFinished() {
            }
        });
    }
    
    /**
     * 处理请求到的json数据
     */
    private void processData(String jsonData) {
        
        mediaItems = parseJson(jsonData);
        // 设置适配器
        if ((mediaItems != null) && (mediaItems.size() > 0)) {
            // 有数据设置适配器
            netVideoPagerAdapter = new NetVideoPagerAdapter(getContext(), mediaItems);
            if (mXListView != null) {
                mXListView.setAdapter(netVideoPagerAdapter);
            }
            onLoad();
            // 隐藏提示文本
            noNet.setVisibility(View.GONE);
        } else {
            // 没有数据显示提示文本
            noNet.setVisibility(View.VISIBLE);
        }
        // 隐藏ProgressBar
        loading.setVisibility(View.GONE);
        
    }
    
    /**
     * 解析json数据
     */
    private ArrayList<MediaItem> parseJson(String jsonData) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            // 得到一个JSON数组
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if ((jsonArray != null) && (jsonArray.length() > 0)) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objectItem = (JSONObject) jsonArray.get(i);
                    if (objectItem != null) {
                        // 创建一个视频对象
                        MediaItem mediaItem = new MediaItem();
                        
                        // 添加到视频集合中
                        mediaItems.add(mediaItem);
                        
                        // 获取并设置视频的名字
                        String videoName = objectItem.optString("movieName");
                        mediaItem.setMediaName(videoName);
                        
                        // 获取并设置视频的描述
                        String videoDescribe = objectItem.optString("videoTitle");
                        mediaItem.setDescribe(videoDescribe);
                        
                        // 获取并设置视频的图片
                        String imageUrl = objectItem.optString("coverImg");
                        mediaItem.setImageUrl(imageUrl);
                        
                        // 获取并设置视频的路径
                        String dataUrl = objectItem.optString("hightUrl");
                        mediaItem.setData(dataUrl);
                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return mediaItems;
    }
    
    /**
     * 刷新完成
     */
    private void onLoad() {
        mXListView.stopRefresh();// 停止刷新
        mXListView.stopLoadMore();// 停止加载
        mXListView.setRefreshTime(getSystemTime());// 设置刷新时间
    }
    
    /**
     * 获取系统时间
     */
    private String getSystemTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(new Date());
    }
    
}
