package com.example.smy.antest;

import android.database.ContentObserver;
import android.os.Message;

import android.os.Handler;

/**
 * Created by SMY on 2016/6/2.
 */
public class PersonObserver extends ContentObserver {
    public static final String TAG = "PersonObserver";
    private Handler handler;

    public PersonObserver(Handler handler)
    {
        super(handler);
        this.handler = handler;
    }

    @Override
    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Message msg = new Message();
        handler.sendMessage(msg);
    }
}
