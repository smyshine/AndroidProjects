package com.example.smy.broadcasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by SMY on 2016/6/9.
 */
//broadcast 动态注册和静态注册
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, "Boot complete", Toast.LENGTH_SHORT)
                .show();
    }
}
