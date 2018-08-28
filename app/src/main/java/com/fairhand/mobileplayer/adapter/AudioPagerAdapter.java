package com.fairhand.mobileplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.entity.MediaItem;
import com.fairhand.mobileplayer.utils.MusicUtil;

import java.util.ArrayList;

/**
 * AudioPagerFragment的适配器
 */
public class AudioPagerAdapter extends BaseAdapter implements Filterable {
    
    /**
     * 过滤完后数据的个数<br />
     * 给与搜索使用
     */
    public static int count = 0;
    
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 备份ListView中的数据，用于过滤
     */
    private ArrayList<MediaItem> backupsMediaItems;
    
    private Context context;
    
    /**
     * 自定义过滤器
     */
    private MyFilter mFilter;
    
    /**
     * 搜索的歌名关键字
     */
    private String searchTextName;
    
    /**
     * 搜索的歌手关键字
     */
    private String searchTextArtist;
    
    /**
     * @param context 上下文
     */
    public AudioPagerAdapter(Context context) {
        this.context = context;
        this.mediaItems = MusicUtil.mediaItems;
        this.backupsMediaItems = mediaItems;
    }
    
    @Override
    public int getCount() {
        return mediaItems.size();
    }
    
    @Override
    public Object getItem(int position) {
        return mediaItems.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_music_pager, null);
            holder = new ViewHolder();
            holder.musicName = convertView.findViewById(R.id.music_name);
            holder.musicArtist = convertView.findViewById(R.id.musician);
            holder.currentPlayingMusicImage = convertView.findViewById(R.id.current_playing_music_image);
            
            // 与ViewHolder关联
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        
        // 获取歌名和歌手
        String musicName = mediaItem.getMediaName();
        String musicArtist = mediaItem.getMusicArtist();
        
        // 高亮搜索关键字
        // 当搜索歌名关键字不为空并且有歌名包含搜索关键字
        if ((searchTextName != null) && (musicName.contains(searchTextName))) {
            // SpannableStringBuilder 一个内容和标记都可更改的文本类 可拼接 类似StringBuilder
            SpannableStringBuilder spannableName = new SpannableStringBuilder();
            spannableName.append(musicName);
            // 为SpannableStringBuilder设置关键字前景色
            ForegroundColorSpan nameColorSpan
                    = new ForegroundColorSpan(Color.parseColor("#009AD6"));
            // 设置类型，作用开始位置，作用结束位置，前后包含
            spannableName.setSpan(nameColorSpan,
                    musicName.indexOf(searchTextName),
                    musicName.indexOf(searchTextName) + searchTextName.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.musicName.setText(spannableName);
        } else {
            // 没有匹配到关键字，直接设置音乐名称
            holder.musicName.setText(musicName);
        }
        
        // 当搜索歌手关键字不为空并且有歌手名包含搜索关键字
        if ((searchTextArtist != null) && (musicArtist.contains(searchTextArtist))) {
            // SpannableString 一个内容和标记都可更改的文本类 不可拼接 类似String
            SpannableString spannableArtist = new SpannableString(musicArtist);
            // 为SpannableString设置关键字前景色
            ForegroundColorSpan artistColorSpan
                    = new ForegroundColorSpan(Color.parseColor("#009AD6"));
            // 设置类型，作用开始位置，作用结束位置，前后包含
            spannableArtist.setSpan(artistColorSpan,
                    musicArtist.indexOf(searchTextArtist),
                    musicArtist.indexOf(searchTextArtist) + searchTextArtist.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.musicArtist.setText(spannableArtist);
        } else {
            // 没有匹配到关键字，直接设置歌手
            holder.musicArtist.setText(musicArtist);
        }
        
        return convertView;
    }
    
    /**
     * 当ListView调用setTextFilter()方法的时候回调该方法
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }
    
    /**
     * 自定义一个过滤器的类来定义过滤规则
     */
    class MyFilter extends Filter {
        
        /**
         * 定义过滤规则
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<MediaItem> filterMediaItems;// 过滤完成的数据
            if (TextUtils.isEmpty(constraint)) {
                // 当过滤的关键字为空的时候，显示所有的数据
                filterMediaItems = backupsMediaItems;
            } else {
                // 否则把符合条件的数据添加到集合中
                filterMediaItems = new ArrayList<>();
                for (MediaItem mediaItem : backupsMediaItems) {
                    if (mediaItem.getMediaName().contains(constraint)) {
                        searchTextName = (String) constraint;
                        filterMediaItems.add(mediaItem);
                    } else if (mediaItem.getMusicArtist().contains(constraint)) {
                        searchTextArtist = (String) constraint;
                        filterMediaItems.add(mediaItem);
                    }
                }
            }
            
            // 将过滤完的数据保存到FilterResults的value变量中
            results.values = filterMediaItems;
            // 最后将过滤完数据的大小保存到FilterResults的count变量中
            results.count = filterMediaItems.size();
            
            count = results.count;
            
            return results;
        }
        
        /**
         * 告诉适配器更新界面
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // noinspection unchecked
            mediaItems = (ArrayList<MediaItem>) results.values;// 设置过滤过的数据
            if (results.count > 0) {
                notifyDataSetChanged();// 通知数据发生改变
                MusicUtil.mediaItems = mediaItems;// 更新播放列表
            } else {
                notifyDataSetInvalidated();// 通知数据失效
            }
        }
    }
    
    static class ViewHolder {
        TextView musicName;
        TextView musicArtist;
        ImageView currentPlayingMusicImage;
    }
}

