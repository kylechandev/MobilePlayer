package com.fairhand.mobileplayer;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动管理器
 */
public class ActivityCollector {
    
    /**
     * 存放活动的集合
     */
    private static List<Activity> activities = new ArrayList<>();
    
    /**
     * 添加活动
     */
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }
    
    /**
     * 移除活动
     */
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }
    
    /**
     * 结束所有活动
     */
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }
}
