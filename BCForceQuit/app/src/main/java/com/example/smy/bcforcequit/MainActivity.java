package com.example.smy.bcforcequit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button offline = (Button) findViewById(R.id.force_offline);
        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.smy.bcforcequit.FORCE_OFFLINE");
                sendBroadcast(intent);
            }
        });
    }
}
