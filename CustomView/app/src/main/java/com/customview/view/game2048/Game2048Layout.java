package com.customview.view.game2048;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by SMY on 2017/10/24.
 */

public class Game2048Layout extends RelativeLayout {

    private int mColumn = 3;
    private Game2048Item[] mGameItems;
    private int mMargin = 10;
    private int mPadding;
    private GestureDetector mGestureDetector;
    private boolean isMergeHappen = true;
    private boolean isMoveHappen = true;
    private int mScore;
    private boolean once = false;

    public Game2048Layout(Context context) {
        this(context, null);
    }

    public Game2048Layout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Game2048Layout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMargin, getResources().getDisplayMetrics());
        mPadding = Math.min(Math.min(getPaddingLeft(), getPaddingRight()), Math.min(getPaddingTop(), getPaddingBottom()));
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int length = Math.min(getMeasuredHeight(), getMeasuredWidth());
        int childWidth = (length - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        if (!once){
            if (mGameItems == null){
                mGameItems = new Game2048Item[mColumn * mColumn];
            }
            for (int i = 0; i < mGameItems.length; i++) {
                Game2048Item item = new Game2048Item(getContext());
                mGameItems[i] = item;
                item.setId(i + 1);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(childWidth, childWidth);
                if ((i + 1) % mColumn != 0){
                    lp.rightMargin = mMargin;
                }
                if (i % mColumn != 0){
                    lp.addRule(RelativeLayout.RIGHT_OF, mGameItems[i - 1].getId());
                }
                if ((i + 1) > mColumn){
                    lp.topMargin = mMargin;
                    lp.addRule(RelativeLayout.BELOW, mGameItems[i - mColumn].getId());
                }
                addView(item, lp);
            }
            generateNumber();
        }
        once = true;
        setMeasuredDimension(length, length);
    }

    private void generateNumber(){
        if (checkOver()){
            if (mListener != null){
                mListener.onGameOver();
            }
            return;
        }
        if (!isFull()){
            if (isMergeHappen || isMoveHappen){
                Random random = new Random();
                int next = random.nextInt(mColumn * mColumn);
                Game2048Item item = mGameItems[next];
                while (item.getmNumber() != 0){
                    next = random.nextInt(mColumn * mColumn);
                    item = mGameItems[next];
                }
                item.setmNumber(Math.random() > 0.75 ? 4 : 2);
                isMergeHappen = isMoveHappen = false;
            }
        }
    }

    private void mergeItem(List<Game2048Item> row){
        if (row.size() < 2){
            return;
        }
        for (int j = 0; j < row.size() - 1; j++) {
            Game2048Item item1 = row.get(j);
            Game2048Item item2 = row.get(j + 1);
            if (item1.getmNumber() == item2.getmNumber()){
                isMergeHappen = true;
                int val = item1.getmNumber() + item2.getmNumber();
                item1.setmNumber(val);
                mScore += val;
                if (mListener != null){
                    mListener.onScoreChange(mScore);
                }

                for (int k = j + 1; k < row.size() - 1; k++) {
                    row.get(k).setmNumber(row.get(k + 1).getmNumber());
                }
                row.get(row.size() - 1).setmNumber(0);
                return;
            }
        }
    }

    private boolean checkOver(){
        if (!isFull()){
            return false;
        }
        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mColumn; j++) {
                int index = i * mColumn + j;
                Game2048Item item = mGameItems[index];
                if ((index + 1) % mColumn != 0){
                    Game2048Item itemRight = mGameItems[index + 1];
                    if (item.getmNumber() == itemRight.getmNumber()){
                        return false;
                    }
                }
                if ((index + mColumn) < mColumn * mColumn){
                    Game2048Item itemDown = mGameItems[index + mColumn];
                    if (item.getmNumber() == itemDown.getmNumber()){
                        return false;
                    }
                }
                if ((index) % mColumn != 0){
                    Game2048Item itemLeft = mGameItems[index - 1];
                    if (item.getmNumber() == itemLeft.getmNumber()){
                        return false;
                    }
                }
                if (index + 1 > mColumn){
                    Game2048Item itemUp = mGameItems[index - mColumn];
                    if (item.getmNumber() == itemUp.getmNumber()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isFull(){
        for (int i = 0; i < mGameItems.length; i++) {
            if (mGameItems[i].getmNumber() == 0){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private enum ACTION{
        LEFT, RIGHT, UP, DOWN
    }

    private void action(ACTION action){
        for (int i = 0; i < mColumn; i++) {
            List<Game2048Item> row = new ArrayList<>();
            for (int j = 0; j < mColumn; j++) {
                int index = getIndexByAction(action, i, j);
                Game2048Item item = mGameItems[index];
                if (item.getmNumber() != 0){
                    row.add(item);
                }
            }
            for (int j = 0; j < mColumn && j < row.size(); j++) {
                int index = getIndexByAction(action, i, j);
                Game2048Item item = mGameItems[index];
                if (item.getmNumber() != row.get(j).getmNumber()){
                    isMoveHappen = true;
                }
            }
            mergeItem(row);
            for (int j = 0; j < mColumn; j++) {
                int index = getIndexByAction(action, i, j);
                if (row.size() > j){
                    mGameItems[index].setmNumber(row.get(j).getmNumber());
                } else {
                    mGameItems[index].setmNumber(0);
                }
            }
        }

        generateNumber();
    }

    private int getIndexByAction(ACTION action, int i, int j){
        switch (action){
            case UP:
                return i + j * mColumn;
            case DOWN:
                return i + (mColumn - 1 - j) * mColumn;
            case LEFT:
                return i * mColumn + j;
            case RIGHT:
                return i * mColumn + mColumn - 1 - j;
            default:
                return 0;
        }
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        final int FLING_MIN_DISTANCE = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = e2.getX() - e1.getX();
            float y = e2.getY() - e1.getY();

            if (x > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)){
                action(ACTION.RIGHT);
            } else if (x < -FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)){
                action(ACTION.LEFT);
            } else if (y > FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)){
                action(ACTION.DOWN);
            } else if (y < -FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)){
                action(ACTION.UP);
            }
            return true;
        }
    }

    public interface Game2048Listener{
        void onScoreChange(int score);
        void onGameOver();
    }

    private Game2048Listener mListener;

    public void setGameListener(Game2048Listener listener){
        this.mListener = listener;
    }

    public void restart(){
        removeAllViews();
        for (Game2048Item item : mGameItems){
            item.setmNumber(0);
        }
        once = false;
        isMergeHappen = isMoveHappen = true;
        invalidate();
    }

    public void levelUp(){
        removeAllViews();
        mGameItems = null;
        ++mColumn;
        once = false;
        isMergeHappen = isMoveHappen = true;
        invalidate();
    }

    public void levelDown(){
        removeAllViews();
        mGameItems = null;
        --mColumn;
        once = false;
        isMergeHappen = isMoveHappen = true;
        invalidate();
    }
}
