package com.example.smy.broadcasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by SMY on 2016/6/9.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, "recevived in MyBroadcastReceiver", Toast.LENGTH_SHORT)
                .show();
    }
}
