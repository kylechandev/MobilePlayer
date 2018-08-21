package com.fairhand.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.domain.MediaItem;

import java.util.ArrayList;

/**
 * AudioPagerFragment的适配器
 */
public class AudioPagerAdapter extends BaseAdapter {
    
    private ArrayList<MediaItem> mediaItems;
    
    private Context context;
    
    /**
     * @param mediaItems 媒体列表
     */
    public AudioPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
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
    
    static class ViewHolder {
        TextView musicName;
        TextView musicArtist;
        ImageView currentPlayingMusicImage;
    }
}

