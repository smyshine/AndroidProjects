package com.example.smy.phonewall;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.GridView;

public class MainActivity extends Activity {

    private GridView mPhotoWall;
    private PhotoWallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mPhotoWall = (GridView) findViewById(R.id.photo_wall);
        adapter = new PhotoWallAdapter(this, 0, Images.imageThumbUrls, mPhotoWall);
        mPhotoWall.setAdapter(adapter);
    }

    @Override
    protected  void onDestroy()
    {
        super.onDestroy();
        adapter.cancelAllTasks();
    }
}
