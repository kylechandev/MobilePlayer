package com.fairhand.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairhand.mobileplayer.R;
import com.fairhand.mobileplayer.entity.MediaItem;

import java.util.ArrayList;

import com.fairhand.mobileplayer.utils.TimeConvertUtil;

/**
 * VideoPagerFragment的适配器
 */
public class VideoPagerAdapter extends BaseAdapter {
    
    private ArrayList<MediaItem> mediaItems;
    
    private Context context;
    
    private TimeConvertUtil convertUtils;
    
    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
        convertUtils = new TimeConvertUtil();
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
            convertView = View.inflate(context,
                    R.layout.item_video_pager, null);
            holder = new ViewHolder();
            holder.videoIcon = convertView.findViewById(R.id.video_icon);
            holder.videoName = convertView.findViewById(R.id.video_name);
            holder.videoDuration = convertView.findViewById(R.id.video_duration);
            holder.videoSize =  convertView.findViewById(R.id.video_size);
            
            // 与ViewHolder关联
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        
        holder.videoName.setText(mediaItem.getMediaName());// 设置视频名称
        holder.videoSize.setText(Formatter.formatFileSize(context,// 设置视频大小
                mediaItem.getSize()));
        // 设置视频时长
        holder.videoDuration.setText(convertUtils.stringForTime((int) mediaItem.getDuration()));
        
        return convertView;
    }
    
    static class ViewHolder {
        ImageView videoIcon;
        TextView videoName;
        TextView videoDuration;
        TextView videoSize;
    }
}
