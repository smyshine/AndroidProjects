package com.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.customview.R;

/**
 * Created by SMY on 2017/10/16.
 */

public class CustomViewSlideDelete extends ListView {

    private static final String TAG = "CustomSlideDelete";

    private int touchSlop;
    private boolean isSliding;
    private int xDown, xMove;
    private int yDown, yMove;
    private LayoutInflater mInflater;
    private PopupWindow mPopupWindow;
    private int mPopupWindowHeight;
    private int mPopupWindowWidth;

    private Button mDeleteButton;

    private DeleteClickListener mListener;

    private View mCurrentView;
    private int mCurrentViewPos;


    public CustomViewSlideDelete(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mInflater = LayoutInflater.from(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        View view = mInflater.inflate(R.layout.delete_button, null);
        mDeleteButton = (Button) view.findViewById(R.id.id_item_btn);

        mPopupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.getContentView().measure(0, 0);
        mPopupWindowWidth = mPopupWindow.getContentView().getMeasuredWidth();
        mPopupWindowHeight = mPopupWindow.getContentView().getMeasuredHeight();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                xDown = x;
                yDown = y;
                if (mPopupWindow.isShowing()){
                    dismissPopupWindow();
                    return false;
                }
                mCurrentViewPos = pointToPosition(xDown, yDown);
                mCurrentView = getChildAt(mCurrentViewPos - getFirstVisiblePosition());
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = x;
                yMove = y;
                int dx = xMove - xDown;
                int dy = yMove - yDown;

                if (xMove < xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop){
                    isSliding = true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSliding){
            switch (ev.getAction()){
                case MotionEvent.ACTION_MOVE:
                    int[] location = new int[2];
                    mCurrentView.getLocationOnScreen(location);
                    //mPopupWindow.setAnimationStyle(R.style.slideIn);
                    mPopupWindow.update();
                    mPopupWindow.showAtLocation(mCurrentView, Gravity.LEFT | Gravity.TOP,
                            location[0] + mCurrentView.getWidth(),
                            location[1] + mCurrentView.getHeight() / 2 - mPopupWindowHeight / 2);
                    mDeleteButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null){
                                mListener.Click(mCurrentViewPos);
                                mPopupWindow.dismiss();
                            }
                        }
                    });
                    break;
                case MotionEvent.ACTION_UP:
                    isSliding = false;
                    break;
            }
        }

        return super.onTouchEvent(ev);
    }

    private void dismissPopupWindow(){
        if (mPopupWindow != null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    public interface DeleteClickListener {
        void Click(int position);
    }

    public void setListener(DeleteClickListener listener){
        this.mListener = listener;
    }
}
