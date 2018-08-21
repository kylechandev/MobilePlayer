package com.fairhand.mobileplayer.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.pager.AudioPagerFragment;
import com.fairhand.mobileplayer.pager.NetVedioPagerFragment;
import com.fairhand.mobileplayer.pager.VideoPagerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    
    private ViewPager mViewPager;
    
    private DrawerLayout mDrawerLayout;
    
    private TabLayout mTabLayout;
    
    /**
     * 三个界面Fragment
     */
    private List<Fragment> mFragments;
    
    private Button navigation;
    
    private Button search;
    
    /**
     * 存放Tab的标题
     */
    private String[] tabNames;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        initViews();// 初始化控件
        
        initEvents();// 处理监听逻辑事件
    
        initViewPages(); // 初始化ViewPager
        
    }
    
    /**
     * 初始化控件
     */
    private void initViews() {
    
        // 融合状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (Build.VERSION.SDK_INT >= 23) {
                decorView.setSystemUiVisibility(option | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(option);
            }
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        
        mTabLayout = findViewById(R.id.tablayout);
        
        mViewPager = findViewById(R.id.activity_content_pager_view);
        
        // 获取到DrawerLayout的实例
        mDrawerLayout = findViewById(R.id.drawer_layout);
        
        navigation = findViewById(R.id.navigation);
        
        search = findViewById(R.id.search);
        
        mFragments = new ArrayList<>();
        
        tabNames = new String[] { "视频", "音乐", "网络"};
        
    }
    
    /**
     * 监听逻辑事件
     */
    private void initEvents() {
    
        navigation.setOnClickListener(this);
        search.setOnClickListener(this);
        
        // TabLayout的监听
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
    
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
        
            }
    
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
        
            }
        });
        
        // 与ViewPager建立关联
        mTabLayout.setupWithViewPager(mViewPager);
        
    }
    
    /**
     * 初始化ViewPager
     */
    private void initViewPages() {
        
        // 初始化四个布局
        Fragment videoFragment = new VideoPagerFragment();
        Fragment audioFragment = new AudioPagerFragment();
        Fragment netVideoFragment = new NetVedioPagerFragment();
        // 添加到组中
        mFragments.add(videoFragment);
        mFragments.add(audioFragment);
        mFragments.add(netVideoFragment);
    
        // 为ViewPager设置适配器
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }
        
            @Override
            public int getCount() {
                return mFragments.size();
            }
    
            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabNames[position];
            }
    
            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            
            }
        });
    }
    
    /**
     * 处理点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation :
                mDrawerLayout.openDrawer(GravityCompat.START);// 点击打开导航菜单
                break;
            case R.id.search :
                Toast.makeText(MainActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                break;
            default :
                break;
        }
    }
}
