package com.example.smy.antest;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MyBroadcastReceiverActivity extends Activity {

    private MyReceiver mReceiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_broadcast_receiver);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MY_BROADCAST");
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected  void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void onClickSendMessage(View v)
    {
        Intent intent = new Intent("android.intent.action.MY_BROADCAST");
        intent.putExtra("msg", "hello receiver.");
        sendBroadcast(intent);
    }

    public void onClickSendEditMessage(View v)
    {
        Intent intent = new Intent("android.intent.action.MY_BROADCAST");
        intent.putExtra("msg", ((TextView)findViewById(R.id.bcMessage)).getText().toString());
        sendBroadcast(intent);
    }
}
