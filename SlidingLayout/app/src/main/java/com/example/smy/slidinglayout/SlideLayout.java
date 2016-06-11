package com.example.smy.slidinglayout;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by SMY on 2016/6/9.
 */
public class SlideLayout extends RelativeLayout implements View.OnTouchListener {

    public static final int SNAP_VELOCITY = 200;

    private int screenWidth;
    private int leftEdge = 0;
    private int rightEdge = 0;
	private int touchSlop;

    private float xDown;
	private float yDown;
    private float xMove;
	private float yMove;
    private float xUp;

    private boolean isLeftLayoutVisible;

	private boolean isSliding;
    private View leftLayout;
    private View rightLayout;
    private View mBindView;

    private MarginLayoutParams leftLayoutParams;
    private MarginLayoutParams rightLayoutParams;

    private VelocityTracker mVelocityTracker;

    public SlideLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setScrollEvent(View bindView)
    {
        mBindView = bindView;
        mBindView.setOnTouchListener(this);
    }

    public void scrollToLeftLayout()
    {
        new ScrollTask().execute(-30);
    }

    public void scrollToRightLayout()
    {
        new ScrollTask().execute(30);
    }

    public boolean isLeftLayoutVisible()
    {
        return isLeftLayoutVisible;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if(changed)
        {
            leftLayout = getChildAt(0);
            leftLayoutParams = (MarginLayoutParams) leftLayout.getLayoutParams();
            leftLayoutParams.width = screenWidth - 80;
            rightEdge = -leftLayoutParams.width;
            leftLayoutParams.leftMargin = leftEdge;
            leftLayout.setLayoutParams(leftLayoutParams);

            rightLayout = getChildAt(1);
            rightLayoutParams = (MarginLayoutParams) rightLayout.getLayoutParams();
            rightLayoutParams.width = screenWidth;
            rightLayout.setLayoutParams(rightLayoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        createVelocityTracker(event);
		if (leftLayout.getVisibility() != View.VISIBLE) {
			leftLayout.setVisibility(View.VISIBLE);
		}
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
			    yDown = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
			    yMove = event.getRawY();
                int distanceX = (int) (xMove - xDown);
			    int distanceY = (int) (yMove - yDown);
			    if (!isLeftLayoutVisible && distanceX >= touchSlop && (isSliding || Math.abs(distanceY) <= touchSlop))
                {
				    isSliding = true;
				    rightLayoutParams.rightMargin = -distanceX;
				    if (rightLayoutParams.rightMargin > leftEdge)
                    {
					    rightLayoutParams.rightMargin = leftEdge;
				    }
				    rightLayout.setLayoutParams(rightLayoutParams);
			    }
			    if (isLeftLayoutVisible && -distanceX >= touchSlop)
                {
				    isSliding = true;
				    rightLayoutParams.rightMargin = rightEdge - distanceX;
				    if (rightLayoutParams.rightMargin < rightEdge)
                    {
					    rightLayoutParams.rightMargin = rightEdge;
				    }
                    rightLayout.setLayoutParams(rightLayoutParams);
			    }
                rightLayout.setLayoutParams(rightLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
			    int upDistanceX = (int) (xUp - xDown);
			    if (isSliding)
                {
				    if (wantToShowLeftLayout())
                    {
					    if (shouldScrollToLeftLayout())
                        {
						    scrollToLeftLayout();
					    }
                        else
                        {
						    scrollToRightLayout();
					    }
				    }
                    else if (wantToShowRightLayout())
                    {
					    if (shouldScrollToRightLayout())
                        {
						    scrollToRightLayout();
					    }
                        else
                        {
						    scrollToLeftLayout();
					    }
				    }
			    }
                else if (upDistanceX < touchSlop && isLeftLayoutVisible)
                {
				    scrollToRightLayout();
			    }
                recycleVelocityTracker();
                break;
        }
		if (v.isEnabled()) {
			if (isSliding) {
				unFocusBindView();
				return true;
			}
			if (isLeftLayoutVisible) {
				return true;
			}
			return false;
		}
		return true;
	}

    private boolean wantToShowRightLayout()
    {
        return xUp - xDown < 0 && isLeftLayoutVisible;
    }

    private boolean wantToShowLeftLayout()
    {
        return xUp - xDown > 0 && !isLeftLayoutVisible;
    }

    private boolean shouldScrollToLeftLayout()
    {
		return xUp - xDown > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    private boolean shouldScrollToRightLayout()
    {
		return xDown - xUp > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
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
		if (mBindView != null) {
			mBindView.setPressed(false);
			mBindView.setFocusable(false);
			mBindView.setFocusableInTouchMode(false);
		}
	}

    class ScrollTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... speed)
        {
			int rightMargin = rightLayoutParams.rightMargin;
            while(true)
            {
				rightMargin = rightMargin + speed[0];
				if (rightMargin < rightEdge)
                {
					rightMargin = rightEdge;
                    break;
                }
				if (rightMargin > leftEdge)
                {
					rightMargin = leftEdge;
                    break;
                }
				publishProgress(rightMargin);
				sleep(15);
            }

            if(speed[0] > 0)
			{
				isLeftLayoutVisible = false;
			} 
			else 
			{
                isLeftLayoutVisible = true;
            }
            
			isSliding = false;
			return rightMargin;
        }

		@Override
		protected void onProgressUpdate(Integer... rightMargin) {
			rightLayoutParams.rightMargin = rightMargin[0];
			rightLayout.setLayoutParams(rightLayoutParams);
			unFocusBindView();
		}

		@Override
		protected void onPostExecute(Integer rightMargin) {
			rightLayoutParams.rightMargin = rightMargin;
			rightLayout.setLayoutParams(rightLayoutParams);
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
