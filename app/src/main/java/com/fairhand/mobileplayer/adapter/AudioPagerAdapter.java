package com.fairhand.mobileplayer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.domain.MediaItem;

import java.util.ArrayList;

/**
 * AudioPagerFragment的适配器
 */
public class AudioPagerAdapter extends BaseAdapter implements Filterable {
    
    private ArrayList<MediaItem> mediaItems;
    
    /**
     * 备份ListView中的数据，用于过滤
     */
    private ArrayList<MediaItem> backupsMediaItems;
    
    private Context context;
    
    private MyFilter mFilter;
    
    /**
     * @param mediaItems 媒体列表
     */
    public AudioPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
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
        
        // 设置音乐名称
        holder.musicName.setText(mediaItem.getMediaName());
        // 设置歌手
        holder.musicArtist.setText(mediaItem.getMusicArtist());
        
        return convertView;
    }
    
    /**
     * 当ListView调用setTextFilter()方法的时候调用该方法
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
                    if (mediaItem.getMediaName().contains(constraint)
                            || mediaItem.getMusicArtist().contains(constraint)) {
                        filterMediaItems.add(mediaItem);
                    }
                }
            }
            
            results.values = filterMediaItems;// 将过滤完的数据保存到FilterResults的value变量中
            results.count = filterMediaItems.size();// 将过滤完数据的大小保存到FilterResults的count变量中
    
            return results;
        }
    
        /**
         * 告诉适配器更新界面
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mediaItems = (ArrayList<MediaItem>) results.values;// 设置过滤过的数据
            if (results.count > 0) {
                notifyDataSetChanged();// 通知数据发生改变
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

