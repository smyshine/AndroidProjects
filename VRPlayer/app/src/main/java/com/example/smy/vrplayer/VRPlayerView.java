package com.example.smy.vrplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VRPlayerView extends AppCompatActivity {

    public static final String VIDEO_PATH = "vrvideopath";

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrplayer_view);

        url = getIntent().getStringExtra(VIDEO_PATH);
    }
}
