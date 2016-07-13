package com.example.smy.vrplayer.VRActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.example.smy.vrplayer.R;
import com.example.smy.vrplayer.VRView.CustomVRPlayerView;


public class VRPlayerActivity extends Activity {

    public static final String VIDEO_PATH = "vrvideopath";

    CustomVRPlayerView mVRPlayerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vrplayer_view);

        mVRPlayerView = (CustomVRPlayerView) findViewById(R.id.playerView);

        mVRPlayerView.setDataSource(getIntent().getStringExtra(VIDEO_PATH));

    }
}
