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

import java.io.FileOutputStream;
import java.io.IOException;

public class VRPictureShowActivity extends Activity {
    CustomVRPlayerView mVRPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vrpicture_show);
        mVRPlayerView = (CustomVRPlayerView) findViewById(R.id.playerView);

        //mVRPlayerView.setDataSource(getIntent().getStringExtra(IMAGE_URL));
        //mVRPlayerView.setDataSource("/storage/emulated/0/smy/tempImage.jpg");
        //mVRPlayerView.setDataSource("http://cdn.fds.api.xiaomi.com/sportscamera/20160614/1/2_071752439_media.jpg");
        //View settings = getLayoutInflater().inflate(R.layout.picture_setting_float, null);
        saveBitmapToFile(convertLayoutToBitmap(), "/storage/emulated/0/smy/tempImage.jpg");
        mVRPlayerView.setDataSource(convertLayoutToBitmap());

        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        mVRPlayerView.setSystemUiVisibility(uiFlags);
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

    public void saveBitmapToFile(Bitmap bmp, String filename){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
