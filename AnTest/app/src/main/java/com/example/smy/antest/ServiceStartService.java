package com.example.smy.antest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by SMY on 2016/6/1.
 */
public class ServiceStartService extends Service{

    @Override
    public void onCreate()
    {
        super.onCreate();
        showMessage("on service start service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        showMessage("on service start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        showMessage("on service start");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        showMessage("on service bind");
        return  null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        showMessage("on service unbind");
        return  super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        showMessage("on service destroy");
        super.onDestroy();
    }


    MessageListener listener;
    public void setMessageListener(MessageListener listener)
    {
        this.listener = listener;
    }

    Toast mToast;
    private void showMessage(String msg)
    {
        if(listener != null)
        {
            listener.onShowMessage(msg);
        }

        if(mToast != null)
        {
            //mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
