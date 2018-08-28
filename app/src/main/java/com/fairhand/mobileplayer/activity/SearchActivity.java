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

/**
 * 音乐搜索类<br />
 * Create by FairHand on 2018/8/27.
 *
 * @author FairHand
 */
public class SearchActivity extends AppCompatActivity {
    
    private static final String TAG = SearchActivity.class.getSimpleName();
    
    private ListView searchListView;
    
    private TextView notFindMusic;
    
    /**
     * 当前点击音频位置
     */
    private static final String AUDIO_POSITION = "position";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        setToolBar();
        
        initView();
        
        initData();
        
    }
    
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
        // 设置适配器
        searchListView.setAdapter(new AudioPagerAdapter(this));
        // 开启ListView的过滤功能
        searchListView.setTextFilterEnabled(true);
        
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                MusicUtil.saveMusicInfo(position);
                
                Intent intent = new Intent(SearchActivity.this, AudioPlayerActivity.class);
                // 传入位置
                intent.putExtra(AUDIO_POSITION, position);
                
                startActivity(intent);
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
                    Log.d(TAG, "空了！！！！！！！！！");
                    searchListView.clearTextFilter();  // 清除ListView的过滤
                    notFindMusic.setVisibility(View.GONE);// 隐藏提示文本
                    searchListView.setVisibility(View.INVISIBLE);// 隐藏搜索列表
                    MusicUtil.getDataFromLocal();// 还原数据
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
                MusicUtil.getDataFromLocal();// 还原数据
                finish();
                break;
            default:
                break;
        }
        return true;
    }
    
    @Override
    public void onBackPressed() {
        MusicUtil.getDataFromLocal();// 还原数据
        super.onBackPressed();
    }
}
