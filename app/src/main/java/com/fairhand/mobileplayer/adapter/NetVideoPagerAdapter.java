package com.fairhand.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.domain.MediaItem;

import org.xutils.x;

import java.util.ArrayList;

/**
 * NetVideoPagerFragment的适配器
 */
public class NetVideoPagerAdapter extends BaseAdapter {
    
    private ArrayList<MediaItem> mediaItems;
    
    private Context context;
    
    public NetVideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
    }
    
    @Override
    public int getCount() {
        return mediaItems.size();
    }
    
    @Override
    public Object getItem(int position) {
        return null;
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_net_video_pager, null);
            holder = new ViewHolder();
            holder.videoImage = convertView.findViewById(R.id.video_image);
            holder.videoName = convertView.findViewById(R.id.video_name);
            holder.videoDescribe = convertView.findViewById(R.id.video_describe);
            
            // 与ViewHolder关联
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        
        holder.videoName.setText(mediaItem.getMediaName());// 设置视频名称
        holder.videoDescribe.setText(mediaItem.getDescribe());// 设置视频的描述
        x.image().bind(holder.videoImage, mediaItem.getImageUrl());// 绑定图片
        
        return convertView;
    }
    
    static class ViewHolder {
        ImageView videoImage;
        TextView videoName;
        TextView videoDescribe;
    }
}

