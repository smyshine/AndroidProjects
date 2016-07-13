package com.example.smy.vrplayer.VRActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;

import com.example.smy.vrplayer.R;
import com.example.smy.vrplayer.VRView.CustomVRPlayerView;
import com.example.smy.vrplayer.VRView.VRSettingView;

public class MultiPlayerActivity extends Activity {

    public static final String VIDEO_PATH = "vrvideopath";
    CustomVRPlayerView mVRPlayerView;

    VRSettingView mVRPictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_multi_player);

        mVRPictureView = (VRSettingView) findViewById(R.id.pictureView);
        mVRPictureView.setDataSource(convertLayoutToBitmap());

        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        mVRPictureView.setSystemUiVisibility(uiFlags);

        mVRPlayerView = (CustomVRPlayerView) findViewById(R.id.playerView);
        mVRPlayerView.setDataSource(getIntent().getStringExtra(VIDEO_PATH));
    }

    public Bitmap convertLayoutToBitmap(){
        View view = getLayoutInflater().inflate(R.layout.picture_setting_float, null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Log.d("SDBG","get measure : " + width + " ;  " + height);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, width, height);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

}
