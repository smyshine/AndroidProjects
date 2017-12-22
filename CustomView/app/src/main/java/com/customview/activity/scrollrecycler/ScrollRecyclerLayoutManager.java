package com.customview.activity.scrollrecycler;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by SMY on 2017/12/21.
 */

public class ScrollRecyclerLayoutManager extends RecyclerView.LayoutManager {

    public static final int NO_POSITION = -1;

    private static final String EXTRA_POSITION = "extra_position";
    private static final int DEFAULT_TIME_FOR_ITEM_SETTLE = 300;
    private static final int DEFAULT_FLING_THRESHOLD = 2100;

    private Point viewCenterIterator;
    private Point recyclerCenter;
    private Point currentViewCenter;
    private int childHalfWidth, childHalfHeight;
    private int extraLayoutSpace;

    private int scrollToChangeCurrent;
    private int currentScrollState;

    private Orientation.Helper orientationHelper;

    private int scrolled;
    private int pendingScroll;
    private int currentPosition;
    private int pendingPosition;

    private Context context;

    private int timeForItemSettle;
    private int offscreenItems;

    private SparseArray<View> detachedCache;

    private boolean dataSetChangeShiftedPosition;
    private boolean isFirstOrEmptyLayout;

    private int flingThreshold;
    private boolean shouldSlideOnFling;

    @NonNull
    private final ScrollStateListener scrollStateListener;

    public void setShouldSlideOnFling(boolean fling) {
        this.shouldSlideOnFling = fling;
    }

    public void setTimeForItemSettle(int millis) {
        this.timeForItemSettle = millis;
    }

    public interface ScrollStateListener {
        void onIsBoundReachedFlagChange(boolean isBoundReached);

        void onScrollStart();

        void onScrollEnd();

        void onScroll(float position);

        void onCurrentViewFirstLayout();

        void onDataSetChangePosition();
    }

    public ScrollRecyclerLayoutManager(
            @NonNull Context context,
            @NonNull ScrollStateListener scrollStateListener,
            @NonNull Orientation orientation) {
        this.context = context;
        this.scrollStateListener = scrollStateListener;
        this.timeForItemSettle = DEFAULT_TIME_FOR_ITEM_SETTLE;
        this.pendingPosition = NO_POSITION;
        this.currentPosition = NO_POSITION;
        this.flingThreshold = DEFAULT_FLING_THRESHOLD;
        this.shouldSlideOnFling = false;
        this.recyclerCenter = new Point();
        this.currentViewCenter = new Point();
        this.viewCenterIterator = new Point();
        this.detachedCache = new SparseArray<>();
        this.orientationHelper = orientation.createHelper();
        setAutoMeasureEnabled(true);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            currentPosition = pendingPosition = NO_POSITION;
            scrolled = pendingScroll = 0;
            return;
        }

        if (currentPosition == NO_POSITION) {
            currentPosition = 0;
        }

        if (!isFirstOrEmptyLayout) {
            isFirstOrEmptyLayout = getChildCount() == 0;
            if (isFirstOrEmptyLayout) {
                initChildDimensions(recycler);
            }
        }

        updateRecyclerDimensions();

        detachAndScrapAttachedViews(recycler);

        fill(recycler);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        if (isFirstOrEmptyLayout) {
            scrollStateListener.onCurrentViewFirstLayout();
            isFirstOrEmptyLayout = false;
        } else if (dataSetChangeShiftedPosition){
            scrollStateListener.onDataSetChangePosition();
            dataSetChangeShiftedPosition = false;
        }
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        int newPosition = currentPosition;
        if (currentPosition == NO_POSITION) {
            newPosition = 0;
        } else if (currentPosition >= positionStart) {
            newPosition = Math.min(currentPosition + itemCount, getItemCount() - 1);
        }
        onNewPosition(newPosition);
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        int newPostion = currentPosition;
        if (getItemCount() == 0) {
            newPostion = NO_POSITION;
        } else if (currentPosition >= positionStart) {
            if (currentPosition < positionStart + itemCount) {
                currentPosition = NO_POSITION;
            }
            newPostion = Math.max(0, currentPosition - itemCount);
        }
        onNewPosition(newPostion);
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        currentPosition = Math.min(Math.max(0, currentPosition), getItemCount() - 1);
        dataSetChangeShiftedPosition = true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dx, recycler);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dy, recycler);
    }

    @Override
    public void scrollToPosition(int position) {
        if (currentPosition == position) {
            return;
        }

        currentPosition = position;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (currentPosition == position || pendingPosition != NO_POSITION) {
            return;
        }
        startSmoothPendingScroll(position);
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientationHelper.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        return orientationHelper.canScrollVertically();
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (currentScrollState == RecyclerView.SCROLL_STATE_IDLE && currentScrollState != state) {
            scrollStateListener.onScrollStart();
        }

        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            boolean isScrollEnd = onScrollEnd();
            if (isScrollEnd) {
                scrollStateListener.onScrollEnd();
            } else {
                return;
            }
        } else if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            onDragStart();
        }

        currentScrollState = state;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        pendingPosition = NO_POSITION;
        scrolled = pendingScroll = 0;
        currentPosition = 0;

        removeAllViews();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        if (pendingPosition != NO_POSITION) {
            currentPosition = pendingPosition;
        }
        bundle.putInt(EXTRA_POSITION, currentPosition);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        currentPosition = bundle.getInt(EXTRA_POSITION);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            final AccessibilityRecordCompat recordCompat = AccessibilityEventCompat.asRecord(event);
            recordCompat.setFromIndex(getPosition(getFirstChild()));
            recordCompat.setToIndex(getPosition(getLastChild()));
        }
    }

    public View getLastChild() {
        return getChildAt(getChildCount() - 1);
    }

    public View getFirstChild() {
        return getChildAt(0);
    }

    public int getExtraLayoutSpace() {
        return extraLayoutSpace;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getNextPosition() {
        if (scrolled == 0) {
            return currentPosition;
        }
        if (pendingPosition != NO_POSITION) {
            return pendingPosition;
        }
        return currentPosition + Direction.fromDelta(scrolled).applyTo(1);
    }

    private void fill(RecyclerView.Recycler recycler) {
        cacheAndDetachAttachedViews();

        orientationHelper.setCurrentViewCenter(recyclerCenter, scrolled, currentViewCenter);

        final int endBound = orientationHelper.getViewEnd(getWidth(), getHeight());

        if (isViewVisible(currentViewCenter, endBound)) {
            layoutView(recycler, currentPosition, currentViewCenter);
        }

        layoutViews(recycler, Direction.START, endBound);
        layoutViews(recycler, Direction.END, endBound);
        recycleViewsAndClearCache(recycler);
    }

    private void recycleViewsAndClearCache(RecyclerView.Recycler recycler) {
        for (int i = 0; i < detachedCache.size(); i++) {
            View toRemove = detachedCache.valueAt(i);
            recycler.recycleView(toRemove);
        }
        detachedCache.clear();
    }

    private void layoutViews(RecyclerView.Recycler recycler, Direction direction, int endBound) {
        final int positionStep = direction.applyTo(1);

        boolean noPredictiveLayout = pendingPosition == NO_POSITION
                || !direction.sameAs(pendingPosition - currentPosition);

        viewCenterIterator.set(currentViewCenter.x, currentViewCenter.y);
        for (int pos = currentPosition + positionStep; isInBound(pos); pos += positionStep) {
            if (pos == pendingPosition) {
                noPredictiveLayout = true;
            }
            orientationHelper.shiftViewCenter(direction, scrollToChangeCurrent, viewCenterIterator);
            if (isViewVisible(viewCenterIterator, endBound)) {
                layoutView(recycler, pos, viewCenterIterator);
            } else if (noPredictiveLayout) {
                break;
            }
        }
    }

    private boolean isInBound(int pos) {
        return pos >= 0 && pos < getItemCount();
    }

    private void layoutView(RecyclerView.Recycler recycler, int position, Point viewCenter) {
        if (position < 0) {
            return;
        }

        View view = detachedCache.get(position);
        if (view == null) {
            view = recycler.getViewForPosition(position);
            addView(view);
            measureChild(view, 0, 0);
            layoutDecoratedWithMargins(view, viewCenter.x - childHalfWidth, viewCenter.y - childHalfHeight,
                    viewCenter.x + childHalfWidth, viewCenter.y + childHalfHeight);
        } else {
            attachView(view);
            detachedCache.remove(position);
        }
    }

    private boolean isViewVisible(Point viewCenter, int endBound) {
        return orientationHelper.isViewVisible(viewCenter, childHalfWidth, childHalfHeight, endBound, extraLayoutSpace);
    }

    private void cacheAndDetachAttachedViews() {
        detachedCache.clear();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            detachedCache.put(getPosition(child), child);
        }

        for (int i = 0; i < detachedCache.size(); i++) {
            detachView(detachedCache.valueAt(i));
        }
    }

    private void updateRecyclerDimensions() {
        recyclerCenter.set(getWidth() / 2, getHeight() / 2);
    }

    private void initChildDimensions(RecyclerView.Recycler recycler) {
        View toMeasure = recycler.getViewForPosition(0);
        addView(toMeasure);
        measureChildWithMargins(toMeasure, 0, 0);

        int childWidth = getDecoratedMeasuredWidth(toMeasure);
        int childHeight = getDecoratedMeasuredHeight(toMeasure);

        childHalfWidth = childWidth / 2;
        childHalfHeight = childHeight / 2;

        scrollToChangeCurrent = orientationHelper.getDistanceToChangeCurrent(childWidth, childHeight);

        extraLayoutSpace = scrollToChangeCurrent * offscreenItems;

        detachAndScrapView(toMeasure, recycler);
    }

    private void onNewPosition(int position) {
        if (currentPosition != position) {
            currentPosition = position;
            dataSetChangeShiftedPosition = true;
        }
    }

    private int scrollBy(int dx, RecyclerView.Recycler recycler) {
        if (getChildCount() == 0) {
            return 0;
        }

        Direction direction = Direction.fromDelta(dx);
        int leftToScroll = calculateAllowedScrollIn(direction);
        if (leftToScroll <= 0) {
            return 0;
        }

        int delta = direction.applyTo(Math.min(leftToScroll, Math.abs(dx)));
        scrolled += delta;
        if (pendingScroll != 0) {
            pendingScroll -= delta;
        }
        orientationHelper.offsetChildren(-delta, this);
        if (orientationHelper.hasNewBecomeVisible(this)) {
            fill(recycler);
        }

        notifyScroll();
        return delta;
    }

    private void notifyScroll() {
        float toScroll = pendingPosition != NO_POSITION ? Math.abs(scrolled + pendingScroll) : scrollToChangeCurrent;
        float position = -Math.min(Math.max(-1f, scrolled / toScroll), 1f);
        scrollStateListener.onScroll(position);
    }

    private int calculateAllowedScrollIn(Direction direction) {
        if (pendingScroll != 0) {
            return Math.abs(pendingScroll);
        }

        int allowedScroll;
        boolean isBoundReached;
        boolean isScrollDirectionSame = direction.applyTo(scrolled) > 0;
        if (direction == Direction.START && currentPosition == 0) {
            isBoundReached = scrolled == 0;
            allowedScroll = isBoundReached ? 0 : Math.abs(scrolled);
        } else if (direction == Direction.END && currentPosition == getItemCount() - 1) {
            isBoundReached = scrolled == 0;
            allowedScroll = isBoundReached ? 0 : Math.abs(scrolled);
        } else {
            isBoundReached = false;
            allowedScroll = isScrollDirectionSame ?
                    scrollToChangeCurrent - Math.abs(scrolled) :
                    scrollToChangeCurrent + Math.abs(scrolled);
        }
        scrollStateListener.onIsBoundReachedFlagChange(isBoundReached);
        return allowedScroll;
    }

    private void startSmoothPendingScroll(int position) {
        if (currentPosition == position) {
            return;
        }

        pendingScroll = -scrolled;
        Direction direction = Direction.fromDelta(position - currentPosition);
        int distance = Math.abs(position - currentPosition) * scrollToChangeCurrent;
        pendingScroll += direction.applyTo(distance);
        pendingPosition = position;
        startSmoothPendingScroll();
    }

    private void startSmoothPendingScroll() {
        LinearSmoothScroller scroller = new RecyclerLinearSmoothScroller(context);
        scroller.setTargetPosition(currentPosition);
        startSmoothScroll(scroller);
    }

    private void onDragStart() {
        boolean isScrolledThroughMulti = Math.abs(scrolled) > scrollToChangeCurrent;
        if (isScrolledThroughMulti) {
            int scrolledPostion = scrolled / scrollToChangeCurrent;
            currentPosition += scrolledPostion;
            scrolled -= scrolledPostion * scrollToChangeCurrent;
        }
        if (isAnotherItemCloserThanCurrent()) {
            Direction direction = Direction.fromDelta(scrolled);
            currentPosition += direction.applyTo(1);
            scrolled = -getLeftToScroll(scrolled);
        }
        pendingPosition = NO_POSITION;
        pendingScroll = 0;
    }

    private int getLeftToScroll(int dx) {
        return Direction.fromDelta(dx).applyTo(scrollToChangeCurrent - Math.abs(scrolled));
    }

    private boolean isAnotherItemCloserThanCurrent() {
        return Math.abs(scrolled) >= scrollToChangeCurrent * 0.6f;
    }

    private boolean onScrollEnd() {
        if (pendingPosition != NO_POSITION) {
            currentPosition = pendingPosition;
            pendingPosition = NO_POSITION;
            scrolled = 0;
        }

        Direction direction = Direction.fromDelta(scrolled);
        if (Math.abs(scrolled) == scrollToChangeCurrent) {
            currentPosition += direction.applyTo(1);
            scrolled = 0;
        }

        if (isAnotherItemCloserThanCurrent()) {
            pendingScroll = getLeftToScroll(scrolled);
        } else {
            pendingScroll = -scrolled;
        }
        if (pendingScroll == 0) {
            return true;
        }
        startSmoothPendingScroll();
        return false;
    }


    private class RecyclerLinearSmoothScroller extends LinearSmoothScroller {

        public RecyclerLinearSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            float dist = Math.min(Math.abs(dx), scrollToChangeCurrent);
            return (int) (Math.max(0.01f, dist / scrollToChangeCurrent) * timeForItemSettle);
        }

        @Override
        public int calculateDyToMakeVisible(View view, int snapPreference) {
            return orientationHelper.getPendingDy(-pendingScroll);
        }

        @Override
        public int calculateDxToMakeVisible(View view, int snapPreference) {
            return orientationHelper.getPendingDx(-pendingScroll);
        }

        @Nullable
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return new PointF(orientationHelper.getPendingDx(pendingScroll), orientationHelper.getPendingDy(pendingScroll));
        }
    }


    public void onFling(int velocityX, int velocityY) {
        int velocity = orientationHelper.getFlingVelocity(velocityX, velocityY);
        int throttleValue = shouldSlideOnFling ? Math.abs(velocity / flingThreshold) : 1;
        int newPosition = currentPosition + Direction.fromDelta(velocity).applyTo(throttleValue);
        newPosition = checkNewOnFlingPositionIsInBounds(newPosition);
        boolean isInScrollDirection = velocity * scrolled >= 0;
        boolean canFling = isInScrollDirection && isInBound(newPosition);
        if (canFling) {
            startSmoothPendingScroll(newPosition);
        } else {
            returnToCurrentPosition();
        }
    }

    public void returnToCurrentPosition() {
        pendingScroll = -scrolled;
        if (pendingScroll != 0) {
            startSmoothPendingScroll();
        }
    }

    private int checkNewOnFlingPositionIsInBounds(int position) {
        if (currentPosition != 0 && position < 0) {
            return 0;
        }
        if (currentPosition != getItemCount() - 1 && position >= getItemCount()) {
            return getItemCount() - 1;
        }
        return position;
    }

    public void setOffscreenItems(int offscreenItems) {
        this.offscreenItems = offscreenItems;
        extraLayoutSpace = scrollToChangeCurrent * offscreenItems;
        requestLayout();
    }

    public void setOrientation(Orientation orientation) {
        orientationHelper = orientation.createHelper();
        removeAllViews();
        requestLayout();
    }
}
