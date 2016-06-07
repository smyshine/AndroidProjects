package com.example.smy.myrocket;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by SMY on 2016/6/7.
 */
public class RocketLauncher extends LinearLayout {
    public static int width;
    public static int height;
    private ImageView launcherImage;

    public RocketLauncher(Context context)
    {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.launcher, this);
        launcherImage = (ImageView) findViewById(R.id.launcher_img);
        width = launcherImage.getLayoutParams().width;
        height = launcherImage.getLayoutParams().height;
    }

    public void updateLauncherStatus(boolean isReadyToLauch)
    {
        if(isReadyToLauch)
        {
            launcherImage.setImageResource(R.drawable.launcher_bg_run);
        }
        else
        {
            launcherImage.setImageResource(R.drawable.launcher_bg_hold);
        }
    }
}
