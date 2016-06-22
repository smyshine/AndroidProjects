package com.example.smy.bidirsliding;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by SMY on 2016/6/12.
 */
public class BidirSlidingLayout extends RelativeLayout implements View.OnTouchListener {

    public static final int SNAP_VELOCITY = 200;

    public static final int DO_NOTHING = 0;
    public static final int SHOW_LEFT_MENU = 1;
    public static final int SHOW_RIGHT_MENU = 2;
    public static final int HIDE_LEFT_MENU = 3;
    public static final int HIDE_RIGHT_MENU = 4;
    private int slideState;

    private int screenWidth;
    private int touchScop;
    private float xDown;
    private float yDown;
    private float xMove;
    private float yMove;
    private float xUp;

    private boolean isLeftMenuVisible;
    private boolean isRightMenuVisible;
    private boolean isSliding;

    private View leftMenuLayout;
    private View contentLayout;
    private View rightMenuLayout;
    private View mBindView;
    private MarginLayoutParams leftMenuLayoutParams;
    private MarginLayoutParams rightMenuLayoutParams;
    private LayoutParams contentLayoutParams;
    private VelocityTracker mVelocityTracker;

    public BidirSlidingLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        touchScop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setScrollEvent(View bindView)
    {
        mBindView = bindView;
        mBindView.setOnTouchListener(this);
    }

    public void scrollToLeftMenu()
    {
        new LeftMenuScrollTask().execute(-30);
    }

    public void scrollToContentFromLefeMenu()
    {
        new LeftMenuScrollTask().execute(30);
    }

    public void scrollToRightMenu()
    {
        new RightMenuScrollTask().execute(-30);
    }

    public void scrollToContentFromRightMenu()
    {
        new RightMenuScrollTask().execute(30);
    }

    public boolean isLeftMenuVisible()
    {
        return isLeftMenuVisible;
    }

    public boolean isRightMenuVisible()
    {
        return isRightMenuVisible;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (changed)
        {
            leftMenuLayout = getChildAt(0);
            leftMenuLayoutParams = (MarginLayoutParams) leftMenuLayout.getLayoutParams();
            rightMenuLayout = getChildAt(1);
            rightMenuLayoutParams = (MarginLayoutParams) rightMenuLayout.getLayoutParams();
            contentLayout = getChildAt(2);
            contentLayoutParams = (LayoutParams) contentLayout.getLayoutParams();
            contentLayoutParams.width = screenWidth;
            contentLayout.setLayoutParams(contentLayoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                yDown = event.getRawY();
                slideState = DO_NOTHING;
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                yMove = event.getRawY();
                int moveDistanceX = (int) (xMove - xDown);
                int moveDistanceY = (int) (yMove - yDown);
                checkSlideState(moveDistanceX, moveDistanceY);
                switch (slideState)
                {
                    case SHOW_LEFT_MENU:
                        contentLayoutParams.rightMargin = -moveDistanceX;
                        checkLeftMenuBorder();
                        contentLayout.setLayoutParams(contentLayoutParams);
                        break;
                    case HIDE_LEFT_MENU:
                        contentLayoutParams.rightMargin = -leftMenuLayoutParams.width - moveDistanceX;
                        checkLeftMenuBorder();
                        contentLayout.setLayoutParams(contentLayoutParams);
                        break;
                    case SHOW_RIGHT_MENU:
                        contentLayoutParams.leftMargin = moveDistanceX;
                        checkRightMenuBorder();
                        contentLayout.setLayoutParams(contentLayoutParams);
                        break;
                    case HIDE_RIGHT_MENU:
                        contentLayoutParams.leftMargin = -rightMenuLayoutParams.width + moveDistanceX;
                        checkRightMenuBorder();
                        contentLayout.setLayoutParams(contentLayoutParams);
                        break;
                    default:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                int upDistanceX = (int) (xUp - xDown);
                if(isSliding)
                {
                    switch (slideState)
                    {
                        case SHOW_LEFT_MENU:
                            if(shouldScrollToLeftMenu())
                            {
                                scrollToLeftMenu();
                            }
                            else
                            {
                                scrollToContentFromLefeMenu();
                            }
                            break;
                        case HIDE_LEFT_MENU:
                            if(shouldScrollToContentFromLeftMenu())
                            {
                                scrollToContentFromLefeMenu();
                            }
                            else
                            {
                                scrollToLeftMenu();
                            }
                            break;
                        case SHOW_RIGHT_MENU:
                            if(shouldScrollToRightMenu())
                            {
                                scrollToRightMenu();
                            }
                            else
                            {
                                scrollToContentFromRightMenu();
                            }
                            break;
                        case HIDE_RIGHT_MENU:
                            if(shouldScrollToContentFromRightMenu())
                            {
                                scrollToContentFromRightMenu();
                            }
                            else
                            {
                                scrollToRightMenu();
                            }
                            break;
                        default:
                            break;
                    }
                }
                else if (upDistanceX < touchScop && isLeftMenuVisible)
                {
                    scrollToContentFromLefeMenu();
                }
                else if (upDistanceX < touchScop && isRightMenuVisible)
                {
                    scrollToContentFromRightMenu();
                }
                recycleVelocityTracker();
                break;
        }
        if(v.isEnabled())
        {
            if(isSliding)
            {
                unFocusBindView();
                return true;
            }
            if(isLeftMenuVisible || isRightMenuVisible)
            {
                return true;
            }
            return false;
        }
        return true;
    }

    private void checkSlideState(int moveDistanceX, int moveDistanceY)
    {
        if(isSliding || Math.abs(moveDistanceX) < touchScop)
        {
            return;
        }

        if (isLeftMenuVisible)
        {
            if (moveDistanceX < 0)
            {
                isSliding = true;
                slideState = HIDE_LEFT_MENU;
            }
        }
        else if (isRightMenuVisible)
        {
            if (moveDistanceX > 0)
            {
                isSliding = true;
                slideState = HIDE_RIGHT_MENU;
            }
        }
        else
        {
            if (moveDistanceX > 0 && Math.abs(moveDistanceY) < touchScop)
            {
                isSliding = true;
                slideState = SHOW_LEFT_MENU;
                contentLayoutParams.addRule(ALIGN_PARENT_LEFT, 0);
                contentLayoutParams.addRule(ALIGN_PARENT_RIGHT);
                contentLayout.setLayoutParams(contentLayoutParams);
                leftMenuLayout.setVisibility(VISIBLE);
                rightMenuLayout.setVisibility(GONE);
            }
            else if (moveDistanceX < 0 && Math.abs(moveDistanceY) < touchScop)
            {
                isSliding = true;
                slideState = SHOW_RIGHT_MENU;
                contentLayoutParams.addRule(ALIGN_PARENT_RIGHT, 0);
                contentLayoutParams.addRule(ALIGN_PARENT_LEFT);
                contentLayout.setLayoutParams(contentLayoutParams);
                leftMenuLayout.setVisibility(GONE);
                rightMenuLayout.setVisibility(VISIBLE);
            }
        }
    }

    private void checkLeftMenuBorder()
    {
        if(contentLayoutParams.rightMargin > 0)
        {
            contentLayoutParams.rightMargin = 0;
        }
        else if (contentLayoutParams.rightMargin < -leftMenuLayoutParams.width)
        {
            contentLayoutParams.rightMargin = -leftMenuLayoutParams.width;
        }
    }

    private void checkRightMenuBorder()
    {
        if(contentLayoutParams.leftMargin > 0)
        {
            contentLayoutParams.leftMargin = 0;
        }
        else if (contentLayoutParams.leftMargin < -rightMenuLayoutParams.width)
        {
            contentLayoutParams.leftMargin = -rightMenuLayoutParams.width;
        }
    }

    private boolean shouldScrollToLeftMenu()
    {
        return xUp - xDown > leftMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToRightMenu()
    {
        return xDown - xUp > rightMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToContentFromLeftMenu()
    {
        return xDown - xUp > leftMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToContentFromRightMenu()
    {
        return xUp - xDown > rightMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
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

    private void unFocusBindView()
    {
        if(mBindView != null)
        {
            mBindView.setPressed(false);
            mBindView.setFocusable(false);
            mBindView.setFocusableInTouchMode(false);
        }
    }

    class LeftMenuScrollTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
            int rightMargin = contentLayoutParams.rightMargin;
            while(true)
            {
                rightMargin = rightMargin + speed[0];
                if(rightMargin < -leftMenuLayoutParams.width)
                {
                    rightMargin = -leftMenuLayoutParams.width;
                    break;
                }
                if(rightMargin > 0)
                {
                    rightMargin = 0;
                    break;
                }
                publishProgress(rightMargin);
                sleep(15);
            }

            if(speed[0] > 0)
            {
                isLeftMenuVisible = false;
            }
            else
            {
                isLeftMenuVisible = true;
            }
            isSliding = false;
            return rightMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... rightMargin)
        {
            contentLayoutParams.rightMargin = rightMargin[0];
            contentLayout.setLayoutParams(contentLayoutParams);
            unFocusBindView();
        }

        @Override
        protected void onPostExecute(Integer rightMargin)
        {
            contentLayoutParams.rightMargin = rightMargin;
            contentLayout.setLayoutParams(contentLayoutParams);
        }
    }

    class RightMenuScrollTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
            int leftMargin = contentLayoutParams.leftMargin;
            while(true)
            {
                leftMargin = leftMargin + speed[0];
                if(leftMargin < -rightMenuLayoutParams.width)
                {
                    leftMargin = -rightMenuLayoutParams.width;
                    break;
                }
                if(leftMargin > 0)
                {
                    leftMargin = 0;
                    break;
                }
                publishProgress(leftMargin);
                sleep(15);
            }

            if(speed[0] > 0)
            {
                isRightMenuVisible = false;
            }
            else
            {
                isRightMenuVisible = true;
            }
            isSliding = false;
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin)
        {
            contentLayoutParams.leftMargin = leftMargin[0];
            contentLayout.setLayoutParams(contentLayoutParams);
            unFocusBindView();
        }

        @Override
        protected void onPostExecute(Integer leftMargin)
        {
            contentLayoutParams.leftMargin = leftMargin;
            contentLayout.setLayoutParams(contentLayoutParams);
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

}
