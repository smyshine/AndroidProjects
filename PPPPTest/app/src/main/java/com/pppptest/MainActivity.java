package com.pppptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import smy.com.pppptest.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.server).setEnabled(false);
                findViewById(R.id.client).setEnabled(false);
                log("I'm server");
                handleAsServer();
            }
        });

        findViewById(R.id.client).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.server).setEnabled(false);
                findViewById(R.id.client).setEnabled(false);
                log("I'm client");
                handleAsClient();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
    }

    private void log(final String message) {
        Log.d(TAG, "log: " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append("\n" + message);
            }
        });
    }


    private PPPPServer server = null;
    private void handleAsServer() {
        server = new PPPPServer(new PPPPCallback() {
            @Override
            public void onLog(String message) {
                log(message);
            }
        });
    }

    private PPPPClient client = null;
    private void handleAsClient() {
        client = new PPPPClient(new PPPPCallback() {
            @Override
            public void onLog(String message) {
                log(message);
            }
        });
        client.startConnect();
    }

}
