package com.example.smy.slidingviewswitcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SMY on 2016/6/10.
 */
public class AutoSlidingSwitcherView extends RelativeLayout implements View.OnTouchListener {
    public static final int SNAP_VELOCITY = 200;

    private int switchViewWidth;
    private int currentItemIndex;
    private int itemsCount;
    private int[] borders;
    private int leftEdge = 0;
    private int rightEdge = 0;

    private float xDown;
    private float xMove;
    private float xUp;

    private LinearLayout itemsLayout;
    private LinearLayout dotsLayout;
    private View firstItem;
    private MarginLayoutParams firstItemParams;

    private VelocityTracker mVelocityTracker;

    public AutoSlidingSwitcherView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoSlidingSwitcherView);
        boolean isAutoPlay = a.getBoolean(R.styleable.AutoSlidingSwitcherView_auto_play, false);
        if(isAutoPlay)
        {
            startAutoPlay();
        }
        a.recycle();
    }

    public void scrollToNext()
    {
        new ScrollTask().execute(-20);
    }

    public void scrollToPrevious()
    {
        new ScrollTask().execute(20);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if(changed)
        {
            initializeItems();
            initializeDots();
        }
    }

    private void initializeItems()
    {
        switchViewWidth = getWidth();
        itemsLayout = (LinearLayout) getChildAt(0);
        itemsCount = itemsLayout.getChildCount();
        borders = new int[itemsCount];
        for(int i = 0; i < itemsCount; ++i)
        {
            borders[i] = -i * switchViewWidth;
            View item = itemsLayout.getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) item.getLayoutParams();
            params.width = switchViewWidth;
            item.setLayoutParams(params);
            item.setOnTouchListener(this);
        }
        leftEdge = borders[itemsCount - 1];
        firstItem = itemsLayout.getChildAt(0);
        firstItemParams = (MarginLayoutParams) firstItem.getLayoutParams();
    }

    private void initializeDots()
    {
        dotsLayout = (LinearLayout) getChildAt(1);
        refreshDotsLayout();
    }

    private void refreshDotsLayout()
    {
        dotsLayout.removeAllViews();
        for(int i = 0; i < itemsCount; ++i)
        {
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
            linearParams.weight = 1;
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            ImageView image = new ImageView(getContext());
            if(i == currentItemIndex)
            {
                image.setBackgroundResource(R.drawable.dot_selected);
            }
            else
            {
                image.setBackgroundResource(R.drawable.dot_unselected);
            }
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            relativeLayout.addView(image, relativeParams);
            dotsLayout.addView(relativeLayout, linearParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        createVelocityTracker(event);
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown) - (currentItemIndex * switchViewWidth);
                firstItemParams.leftMargin = distanceX;
                if(beAbleToScroll())
                {
                    firstItem.setLayoutParams(firstItemParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                if(beAbleToScroll())
                {
                    if(wantScrollToPrevious())
                    {
                        if(shouldScrollToPrevious())
                        {
                            --currentItemIndex;
                            scrollToPrevious();
                            refreshDotsLayout();
                        }
                        else
                        {
                            scrollToNext();
                        }
                    }
                    else if(wantScrollToNext())
                    {
                        if(shouldScrollToNext())
                        {
                            ++currentItemIndex;
                            scrollToNext();
                            refreshDotsLayout();
                        }
                        else
                        {
                            scrollToPrevious();
                        }
                    }
                }
                recycleVelocityTracker();
                break;
        }

        return false;
    }

    private boolean beAbleToScroll()
    {
        return firstItemParams.leftMargin < rightEdge && firstItemParams.leftMargin > leftEdge;
    }

    private boolean wantScrollToPrevious()
    {
        return xUp - xDown > 0;
    }

    private boolean wantScrollToNext()
    {
        return xUp - xDown < 0;
    }

    private boolean shouldScrollToNext()
    {
        return xDown - xUp > switchViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToPrevious()
    {
        return xUp - xDown > switchViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
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
        int velocity = (int)mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    private void recycleVelocityTracker()
    {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    private boolean isCrossBorder(int leftMargin, int speed)
    {
        for(int border : borders)
        {
            if(speed > 0)
            {
                if(leftMargin >= border && leftMargin - speed < border)
                {
                    return true;
                }
            }
            else
            {
                if(leftMargin <= border && leftMargin - speed > border)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private int findClosesBorder(int leftMargin)
    {
        int absLeftMargin = Math.abs(leftMargin);
        int closetBorder = borders[0];
        int closetMargin = Math.abs(Math.abs(closetBorder) - absLeftMargin);
        for(int border : borders)
        {
            int margin = Math.abs(Math.abs(border) - absLeftMargin);
            if(margin < closetMargin)
            {
                closetBorder = border;
                closetMargin = margin;
            }
        }
        return closetBorder;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
            int leftMargin = firstItemParams.leftMargin;
            while(true)
            {
                leftMargin = leftMargin + speed[0];
                if(isCrossBorder(leftMargin, speed[0]))
                {
                    leftMargin = findClosesBorder(leftMargin);
                    break;
                }
                publishProgress(leftMargin);
                sleep(10);
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin)
        {
            firstItemParams.leftMargin = leftMargin[0];
            firstItem.setLayoutParams(firstItemParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin)
        {
            firstItemParams.leftMargin = leftMargin;
            firstItem.setLayoutParams(firstItemParams);
        }

    }

    private void sleep(long mills)
    {
        try {
            Thread.sleep(mills);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void scrollToFirstItem()
    {
        new ScrollToFirstItemTask().execute(20 * itemsCount);
    }

    class ScrollToFirstItemTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
            int leftMargin = firstItemParams.leftMargin;
            while (true)
            {
                leftMargin = leftMargin + speed[0];
                if(leftMargin > 0)
                {
                    leftMargin = 0;
                    break;
                }
                publishProgress(leftMargin);
                sleep(20);
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin)
        {
            firstItemParams.leftMargin = leftMargin[0];
            firstItem.setLayoutParams(firstItemParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin)
        {
            firstItemParams.leftMargin = leftMargin;
            firstItem.setLayoutParams(firstItemParams);
        }
    }

    private Handler handler = new Handler();

    public void startAutoPlay()
    {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(currentItemIndex == itemsCount - 1)
                {
                    currentItemIndex = 0;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToFirstItem();
                            refreshDotsLayout();
                        }
                    });
                }
                else
                {
                    ++currentItemIndex;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToNext();
                            refreshDotsLayout();
                        }
                    });
                }
            }
        }, 3000, 3000);
    }
}
