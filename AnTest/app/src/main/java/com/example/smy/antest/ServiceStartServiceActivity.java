package com.example.smy.antest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ServiceStartServiceActivity extends Activity implements MessageListener {

    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_start_service);

        tvMsg = (TextView) findViewById(R.id.tvMessage);
    }

    public void onClickStartService(View v)
    {
        Intent intent = new Intent(this, ServiceStartService.class);
        startService(intent);
    }

    public void onClickStopService(View v)
    {
        Intent intent = new Intent(this, ServiceStartService.class);
        stopService(intent);
    }

    @Override
    public void onShowMessage(String msg)
    {
        showMessage(msg);
    }

    public void showMessage(String msg)
    {
        if(tvMsg != null)
        {
            tvMsg.setText(tvMsg.getText() + "\n" + msg);
        }
    }

}
