package com.example.smy.progressbar;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ClipDrawable mClipDrawable;
    private int mCurrentProgress = 0;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AnimationDrawable animationDrawable = (AnimationDrawable) findViewById(R.id.iv_fourpoint).getBackground();
        if (animationDrawable != null)
        {
            animationDrawable.start();
        }

        imageView = (ImageView) findViewById(R.id.iv_battery);
        mClipDrawable = (ClipDrawable) imageView.getDrawable();

        mHandler.postDelayed(mRunnable, 100);

    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mCurrentProgress += 1000;
            mCurrentProgress %= 10000;
            mClipDrawable.setLevel(mCurrentProgress);
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

}
