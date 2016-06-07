package com.example.smy.slidemenu;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements View.OnTouchListener {
    public static final int SNAP_VELOCITY = 200;

    private int screenWidth;
    private int leftEdge;
    private int rightEdge = 0;
    private int menuPadding = 160;
    private View content;
    private View menu;

    private LinearLayout.LayoutParams menuParams;
    private float xDown;
    private float xMove;
    private float xUp;
    private boolean isMenuVisible = false;
    private VelocityTracker mVelocityTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initValues();
        content.setOnTouchListener(this);
    }

    private void initValues()
    {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        content = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
        menuParams.width = screenWidth - menuPadding;
        leftEdge = -menuParams.width;
        menuParams.leftMargin = leftEdge;
        content.getLayoutParams().width = screenWidth;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown);
                if(isMenuVisible)
                {
                    menuParams.leftMargin = distanceX;
                }
                else
                {
                    menuParams.leftMargin = leftEdge + distanceX;
                }
                if(menuParams.leftMargin < leftEdge)
                {
                    menuParams.leftMargin = leftEdge;
                }
                else if(menuParams.leftMargin > rightEdge)
                {
                    menuParams.leftMargin = rightEdge;
                }
                menu.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                if(wantToShowMenu())
                {
                    if(shouldScrollToMenu())
                    {
                        scrollToMenu();
                    }
                    else
                    {
                        scrollToContent();
                    }
                }
                else if(wantToShowContent())
                {
                    if(shouldScrollToContent())
                    {
                        scrollToContent();
                    }
                    else
                    {
                        scrollToMenu();
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    private boolean wantToShowContent()
    {
        return xUp - xDown < 0 && isMenuVisible;
    }

    private boolean wantToShowMenu()
    {
        return xUp - xDown > 0 && !isMenuVisible;
    }

    private boolean shouldScrollToMenu()
    {
        return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToContent()
    {
        return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private void scrollToMenu()
    {
        new ScrollTask().execute(60);
    }

    private void scrollToContent()
    {
        new ScrollTask().execute(-60);
    }

    private void createVelocityTracker(MotionEvent event)
    {
        if(mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private int getScrollVelocity()
    {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    private void recycleVelocityTracker()
    {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
            int leftMargin = menuParams.leftMargin;
            while (true)
            {
                leftMargin = leftMargin + speed[0];
                if(leftMargin > rightEdge)
                {
                    leftMargin = rightEdge;
                    break;
                }
                if(leftMargin < leftEdge)
                {
                    leftMargin = leftEdge;
                    break;
                }
                publishProgress(leftMargin);
                sleep(20);
            }
            if(speed[0] > 0)
            {
                isMenuVisible = true;
            }
            else
            {
                isMenuVisible = false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin)
        {
            menuParams.leftMargin = leftMargin[0];
            menu.setLayoutParams(menuParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin)
        {
            menuParams.leftMargin = leftMargin;
            menu.setLayoutParams(menuParams);
        }
    }

    private void sleep(long mills)
    {
        try
        {
            Thread.sleep(mills);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
