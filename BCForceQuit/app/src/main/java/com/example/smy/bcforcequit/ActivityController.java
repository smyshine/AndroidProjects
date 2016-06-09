package com.example.smy.bcforcequit;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2016/6/9.
 */
public class ActivityController {
    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity)
    {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity)
    {
        activities.remove(activity);
    }

    public static void finishAll()
    {
        for(Activity activity : activities)
        {
            activity.finish();
        }
    }
}
