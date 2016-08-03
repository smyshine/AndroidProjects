package com.example.smy.vrplayer.VRActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.example.smy.vrplayer.R;
import com.example.smy.vrplayer.VRView.CustomMultiPlayView;

public class MultiPlayerActivity extends Activity {

    public static final String VIDEO_PATH = "vrvideopath";
    CustomMultiPlayView multiPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_multi_player);

        multiPlayView = (CustomMultiPlayView) findViewById(R.id.playerView);
        multiPlayView.setDataSource(getIntent().getStringExtra(VIDEO_PATH));
    }

}
