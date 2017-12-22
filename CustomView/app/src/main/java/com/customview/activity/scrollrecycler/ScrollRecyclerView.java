package com.customview.activity.scrollrecycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.customview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2017/12/21.
 */

public class ScrollRecyclerView extends RecyclerView {

    private static final int DEFAULT_ORIENTATION = Orientation.HORIZONTAL.ordinal();
    private ScrollRecyclerLayoutManager layoutManager;

    private List<ScrollStateChangeListener> scrollStateChangeListeners;
    private List<OnItemChangedListener> onItemChangedListeners;

    public ScrollRecyclerView(Context context) {
        super(context);
        init(null);
    }

    public ScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        scrollStateChangeListeners = new ArrayList<>();
        onItemChangedListeners = new ArrayList<>();

        int orientation = DEFAULT_ORIENTATION;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScrollRecyclerView);
            orientation = typedArray.getInt(R.styleable.ScrollRecyclerView_dsv_orientation, DEFAULT_ORIENTATION);
            typedArray.recycle();
        }

        layoutManager = new ScrollRecyclerLayoutManager(getContext(),
                new ScrollStateListener(), Orientation.values()[orientation]);
        setLayoutManager(layoutManager);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof ScrollRecyclerLayoutManager) {
            super.setLayoutManager(layout);
        } else {
            throw new IllegalArgumentException("Illegal layout manager");
        }
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        boolean isFling = super.fling(velocityX, velocityY);
        if (isFling) {
            layoutManager.onFling(velocityX, velocityY);
        } else {
            layoutManager.returnToCurrentPosition();
        }
        return isFling;
    }

    public void setSlideOnFling(boolean fling) {
        layoutManager.setShouldSlideOnFling(fling);
    }

    private class ScrollStateListener implements ScrollRecyclerLayoutManager.ScrollStateListener {
        @Override
        public void onIsBoundReachedFlagChange(boolean isBoundReached) {
            setOverScrollMode(isBoundReached ? OVER_SCROLL_ALWAYS : OVER_SCROLL_NEVER);
        }

        @Override
        public void onScrollStart() {
            if (scrollStateChangeListeners.isEmpty()) {
                return;
            }
            int current = layoutManager.getCurrentPosition();
            ViewHolder holder = getViewHolder(current);
            if (holder != null) {
                notifyScrollStart(holder, current);
            }
        }

        @Override
        public void onScrollEnd() {
            if (onItemChangedListeners.isEmpty() && scrollStateChangeListeners.isEmpty()) {
                return;
            }
            int current = layoutManager.getCurrentPosition();
            ViewHolder holder = getViewHolder(current);
            if (holder != null) {
                notifyScrollEnd(holder, current);
                notifyCurrentItemChanged(holder, current);
            }
        }

        @Override
        public void onScroll(float position) {
            if (scrollStateChangeListeners.isEmpty()) {
                return;
            }
            int currentIndex = getCurrentItem();
            int newIndex = layoutManager.getNextPosition();
            if (currentIndex != newIndex) {
                notifyScroll(position,
                        currentIndex, newIndex,
                        getViewHolder(currentIndex),
                        getViewHolder(newIndex));
            }
        }

        @Override
        public void onCurrentViewFirstLayout() {
            post(new Runnable() {
                @Override
                public void run() {
                    notifyCurrentItemChanged();
                }
            });
        }

        @Override
        public void onDataSetChangePosition() {
            notifyCurrentItemChanged();
        }
    }

    @Nullable
    private ViewHolder getViewHolder(int current) {
        View view = layoutManager.findViewByPosition(current);
        return view != null ? getChildViewHolder(view) : null;
    }

    private void notifyScrollStart(ViewHolder holder, int current) {
        for (ScrollStateChangeListener listener : scrollStateChangeListeners) {
            listener.onScrollStart(holder, current);
        }
    }

    private void notifyScrollEnd(ViewHolder holder, int current) {
        for (ScrollStateChangeListener listener : scrollStateChangeListeners) {
            listener.onScrollEnd(holder, current);
        }
    }

    private void notifyScroll(float position, int currentIndex, int newIndex,
                              ViewHolder currentHolder, ViewHolder newHolder) {
        for (ScrollStateChangeListener listener : scrollStateChangeListeners) {
            listener.onScroll(position, currentIndex, newIndex, currentHolder, newHolder);
        }
    }

    private void notifyCurrentItemChanged(ViewHolder holder, int current) {
        for (OnItemChangedListener listener : onItemChangedListeners) {
            listener.onCurrentItemChanged(holder, current);
        }
    }

    private void notifyCurrentItemChanged() {
        if (onItemChangedListeners.isEmpty()) {
            return;
        }
        int current = layoutManager.getCurrentPosition();
        ViewHolder currentHolder = getViewHolder(current);
        notifyCurrentItemChanged(currentHolder, current);
    }

    public interface OnItemChangedListener<T extends ViewHolder> {
        void onCurrentItemChanged(@Nullable T holder, int position);
    }

    public interface ScrollStateChangeListener<T extends ViewHolder> {
        void onScrollStart(@NonNull T holder, int position);

        void onScrollEnd(@NonNull T holder, int position);

        void onScroll(float srcollPosition, int currentPosition, int newPosition,
                      @Nullable T currentHolder, @Nullable T newHolder);
    }


    public int getCurrentItem() {
        return layoutManager.getCurrentPosition();
    }

    public void addOnItemChangedListener(@NonNull OnItemChangedListener<?> onItemChangedListener) {
        onItemChangedListeners.add(onItemChangedListener);
    }

    public void removeScrollStateChangeListener(@NonNull ScrollStateChangeListener<?> scrollStateChangeListener) {
        scrollStateChangeListeners.remove(scrollStateChangeListener);
    }

    public void addScrollStateChangeListener(@NonNull ScrollStateChangeListener<?> scrollStateChangeListener) {
        scrollStateChangeListeners.add(scrollStateChangeListener);
    }

    public void setItemTransitionTimeMillis(@IntRange(from = 10) int millis) {
        layoutManager.setTimeForItemSettle(millis);
    }


}
