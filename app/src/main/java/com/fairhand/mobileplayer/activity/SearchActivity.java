package com.fairhand.mobileplayer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.adapter.AudioPagerAdapter;
import com.fairhand.mobileplayer.utils.MusicUtil;
import com.fairhand.mobileplayer.utils.SaveCacheUtil;

/**
 * 音乐搜索类<br />
 * Created by FairHand on 2018/8/27.
 *
 * @author FairHand
 */
public class SearchActivity extends AppCompatActivity {
    
    private static final String TAG = SearchActivity.class.getSimpleName();
    
    /**
     * 发送同步bar广播
     */
    public static final String SYNC_BAR_INFO = "SYNC_BAR_INFO_ACTION";
    
    /**
     * 通过搜索点击的音乐位置
     */
    public static final String POSITION_FOR_SEARCH = "POSITION_FOR_SEARCH_VALUES";
    
    /**
     * 搜索到的item
     */
    private ListView searchListView;
    
    /**
     * 提示性文本
     */
    private TextView notFindMusic;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        setToolBar();
        
        initView();
        
        initData();
        
    }
    
    @Override
    protected void onDestroy() {
        SaveCacheUtil.putCurrentPosition(SearchActivity.this,
                "POSITION_KEY", -1);// 重置当前播放位置
        super.onDestroy();
    }
    
    /**
     * 当前点击音频位置
     */
    private static final String AUDIO_POSITION = "position";
    
    /**
     * 设置ToolBar
     */
    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    /**
     * 初始化
     */
    private void initView() {
        
        searchListView = findViewById(R.id.search_list_view);
        notFindMusic = findViewById(R.id.not_find_music);
        
        // 融合状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (Build.VERSION.SDK_INT >= 23) {
                decorView.setSystemUiVisibility(
                        option | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(option);
            }
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);// 隐藏标题
            actionBar.setDisplayHomeAsUpEnabled(true);// 显示返回键
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        SaveCacheUtil.putCurrentPosition(SearchActivity.this,
                "POSITION_KEY", -1);// 重置当前播放位置
        
        // 设置适配器
        searchListView.setAdapter(new AudioPagerAdapter(this));
        // 开启ListView的过滤功能
        searchListView.setTextFilterEnabled(true);
        
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                Intent intent = new Intent(SearchActivity.this,
                        AudioPlayerActivity.class);
                
                // 传入位置
                intent.putExtra(AUDIO_POSITION, position);
                
                startActivity(intent);
                
                // 暂且先重置位置（日后处理）
                SaveCacheUtil.putCurrentPosition(SearchActivity.this,
                        "POSITION_KEY", -1);
                
                // 发送广播同步Bar音乐信息
                Intent syncIntent = new Intent(SYNC_BAR_INFO);
                syncIntent.putExtra(POSITION_FOR_SEARCH, position);
                sendBroadcast(syncIntent);
            }
        });
    }
    
    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        // 通过MenuItem得到SearchView
        // noinspection deprecation
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // 设置默认展开
        mSearchView.onActionViewExpanded();
        //设置输入框提示语
        mSearchView.setQueryHint("搜索本地音乐");
        
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    searchListView.clearTextFilter();  // 清除ListView的过滤
                    notFindMusic.setVisibility(View.GONE);// 隐藏提示文本
                    searchListView.setVisibility(View.INVISIBLE);// 隐藏搜索列表
                } else {
                    searchListView.setFilterText(s); // 设置ListView的过滤关键词
                    searchListView.dispatchDisplayHint(View.INVISIBLE);// 隐藏弹出的搜索关键字悬浮窗
                    if (AudioPagerAdapter.count > 0) {
                        notFindMusic.setVisibility(View.GONE);// 隐藏提示文本
                        searchListView.setVisibility(View.VISIBLE);// 显示搜索列表
                    } else {
                        notFindMusic.setVisibility(View.VISIBLE);// 显示提示文本
                        searchListView.setVisibility(View.INVISIBLE);// 隐藏搜索列表
                        notFindMusic.setText(getString(R.string.not_found_music, s));// 设置提示信息
                    }
                }
                return false;
            }
        });
        
        return true;
    }
    
    /**
     * 菜单项点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 返回
                MusicUtil.getDataFromLocal();
                finish();
                break;
            default:
                break;
        }
        return true;
    }
    
    @Override
    public void onBackPressed() {
        MusicUtil.getDataFromLocal();
        super.onBackPressed();
    }
}
