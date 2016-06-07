package com.example.smy.myrocket;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by SMY on 2016/6/7.
 */
public class FloatWindowSmallView extends LinearLayout {
    public static int viewWidth;
    public static int viewHeight;

    private static int statusBarHeight;

    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;

    private LinearLayout smallWindowLayout;
    private ImageView rocketImage;

    private float xInScreen;
    private float yInScreen;
    private float xDownScreen;
    private float yDownScreen;
    private float xInView;
    private float yInView;
    private int rocketWidth;
    private int rocketHeight;
    private boolean isPressed;

    public FloatWindowSmallView(Context context)
    {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
        smallWindowLayout = (LinearLayout) findViewById(R.id.small_window_layout);
        viewWidth = smallWindowLayout.getLayoutParams().width;
        viewHeight = smallWindowLayout.getLayoutParams().height;
        rocketImage = (ImageView) findViewById(R.id.rocket_image);
        rocketWidth = rocketImage.getLayoutParams().width;
        rocketHeight = rocketImage.getLayoutParams().height;
        TextView percentView = (TextView) findViewById(R.id.percent);
        percentView.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                xInView = event.getX();
                yInView = event.getY();
                xDownScreen = event.getRawX();
                yDownScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                updateViewStatus();
                break;
            case MotionEvent.ACTION_UP:
                isPressed = false;
                if(MyWindowManager.isReadyToLaunch())
                {
                    launchRocket();
                }
                else
                {
                    updateViewStatus();
                    if(xDownScreen == xInScreen && yDownScreen == yInScreen)
                    {
                        openBigWindow();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setParams(WindowManager.LayoutParams params)
    {
        mParams = params;
    }

    private void launchRocket()
    {
        MyWindowManager.removeLauncher(getContext());
        new LaunchTask().execute();
    }

    private void updateViewStatus()
    {
        if(isPressed && rocketImage.getVisibility() != View.VISIBLE)
        {
            mParams.width = rocketWidth;
            mParams.height = rocketHeight;
            windowManager.updateViewLayout(this, mParams);
            smallWindowLayout.setVisibility(View.GONE);
            rocketImage.setVisibility(View.VISIBLE);
            MyWindowManager.createLauncher(getContext());
        }
        else
        {
            mParams.width = viewWidth;
            mParams.height = viewHeight;
            windowManager.updateViewLayout(this, mParams);
            smallWindowLayout.setVisibility(View.VISIBLE);
            rocketImage.setVisibility(View.GONE);
            MyWindowManager.removeLauncher(getContext());
        }
    }

    public void openBigWindow()
    {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    private void updateViewPosition()
    {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
        MyWindowManager.updateLauncher();
    }

    private int getStatusBarHeight()
    {
        if(statusBarHeight == 0)
        {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    class LaunchTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            while(mParams.y > 0)
            {
                mParams.y = mParams.y - 10;
                publishProgress();
                try {
                    Thread.sleep(8);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params)
        {
            windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            updateViewStatus();
            mParams.x = (int) (xDownScreen - xInView);
            mParams.y = (int) (yDownScreen - yInView);
            windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);
        }
    }
}
