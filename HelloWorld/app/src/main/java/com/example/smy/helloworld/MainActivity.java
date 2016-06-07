package com.example.smy.helloworld;

import android.app.Activity;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private static final String TAG = "HelloWorld";

    private ImageView mImageView;
    private ClipDrawable mDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_penguins);

        mDrawable = (ClipDrawable) mImageView.getDrawable();

        mHandler.post(mRunnable);

        //smy add
        Log.e(TAG, "~~~ Hello world onCreate ~~~");
    }

    private int mDir = 30;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg)
        {
            if(msg.what == 0x01)
            {
                if(mDrawable.getLevel() >= 10000)
                {
                    mDir = -30;
                }
                else if(mDrawable.getLevel() <= 0)
                {
                    mDir = 30;
                }
                mDrawable.setLevel(mDrawable.getLevel() + mDir);
            }
            return true;
        }
    });

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mRunnable, 10);
            mHandler.sendEmptyMessage(0x01);
        }
    };

}
