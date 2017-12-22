package com.customview.activity.scrollrecycler;

import android.graphics.Path;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by SMY on 2017/12/21.
 */

public enum Orientation {

    HORIZONTAL{
        @Override
        Helper createHelper() {
            return new HorizontalHelper();
        }
    },

    VERTICAL {
        @Override
        Helper createHelper() {
            return new VerticalHelper();
        }
    };

    abstract Helper createHelper();

    interface Helper {
        int getViewEnd(int recyclerWidth, int recyclerHeight);

        int getDistanceToChangeCurrent(int childWidth, int childHeight);

        void setCurrentViewCenter(Point recyclerCenter, int srcolled, Point outPoint);

        void shiftViewCenter(Direction direction, int shiftAmount, Point outCenter);

        int getFlingVelocity(int velocityX, int velocityY);

        int getPendingDx(int pendingScroll);

        int getPendingDy(int pendingScroll);

        void offsetChildren(int amout, RecyclerView.LayoutManager layoutManager);

        float getDistanceFromCenter(Point center, int viewCenterX, int viewCenterY);

        boolean isViewVisible(Point center, int halfWidth, int halfHeight, int endBound, int extraSpace);

        boolean hasNewBecomeVisible(ScrollRecyclerLayoutManager layoutManager);

        boolean canScrollVertically();

        boolean canScrollHorizontally();
    }

    protected static class VerticalHelper implements Helper {
        @Override
        public int getViewEnd(int recyclerWidth, int recyclerHeight) {
            return recyclerHeight;
        }

        @Override
        public int getDistanceToChangeCurrent(int childWidth, int childHeight) {
            return childHeight;
        }

        @Override
        public void setCurrentViewCenter(Point recyclerCenter, int scrolled, Point outPoint) {
            int newY = recyclerCenter.y - scrolled;
            outPoint.set(recyclerCenter.x, newY);
        }

        @Override
        public void shiftViewCenter(Direction direction, int shiftAmount, Point outCenter) {
            int newY = outCenter.y + direction.applyTo(shiftAmount);
            outCenter.set(outCenter.x, newY);
        }

        @Override
        public int getFlingVelocity(int velocityX, int velocityY) {
            return velocityY;
        }

        @Override
        public int getPendingDx(int pendingScroll) {
            return 0;
        }

        @Override
        public int getPendingDy(int pendingScroll) {
            return pendingScroll;
        }

        @Override
        public void offsetChildren(int amout, RecyclerView.LayoutManager layoutManager) {
            layoutManager.offsetChildrenVertical(amout);
        }

        @Override
        public float getDistanceFromCenter(Point center, int viewCenterX, int viewCenterY) {
            return viewCenterY - center.y;
        }

        @Override
        public boolean isViewVisible(Point center, int halfWidth, int halfHeight, int endBound, int extraSpace) {
            int top = center.y - halfHeight;
            int bottom = center.y + halfHeight;
            return top < (endBound + extraSpace) && bottom > -extraSpace;
        }

        @Override
        public boolean hasNewBecomeVisible(ScrollRecyclerLayoutManager layoutManager) {
            View firstChild = layoutManager.getFirstChild(), lastChild = layoutManager.getLastChild();
            int top = -layoutManager.getExtraLayoutSpace();
            int bottom = layoutManager.getHeight() + layoutManager.getExtraLayoutSpace();

            boolean isVisibleFromTop = layoutManager.getDecoratedTop(firstChild) > top
                    && layoutManager.getPosition(firstChild) > 0;
            boolean isVisibleFromBottom = layoutManager.getDecoratedBottom(lastChild) < bottom
                    && layoutManager.getPosition(lastChild) < layoutManager.getItemCount() - 1;
            return isVisibleFromTop || isVisibleFromBottom;

        }

        @Override
        public boolean canScrollVertically() {
            return true;
        }

        @Override
        public boolean canScrollHorizontally() {
            return false;
        }
    }

    protected static class HorizontalHelper implements Helper {
        @Override
        public int getViewEnd(int recyclerWidth, int recyclerHeight) {
            return recyclerWidth;
        }

        @Override
        public int getDistanceToChangeCurrent(int childWidth, int childHeight) {
            return childWidth;
        }

        @Override
        public void setCurrentViewCenter(Point recyclerCenter, int scrolled, Point outPoint) {
            int newX = recyclerCenter.x - scrolled;
            outPoint.set(newX, recyclerCenter.y);
        }

        @Override
        public void shiftViewCenter(Direction direction, int shiftAmount, Point outCenter) {
            int newX = outCenter.x + direction.applyTo(shiftAmount);
            outCenter.set(newX, outCenter.y);
        }

        @Override
        public int getFlingVelocity(int velocityX, int velocityY) {
            return velocityX;
        }

        @Override
        public int getPendingDx(int pendingScroll) {
            return pendingScroll;
        }

        @Override
        public int getPendingDy(int pendingScroll) {
            return 0;
        }

        @Override
        public void offsetChildren(int amout, RecyclerView.LayoutManager layoutManager) {
            layoutManager.offsetChildrenHorizontal(amout);
        }

        @Override
        public float getDistanceFromCenter(Point center, int viewCenterX, int viewCenterY) {
            return viewCenterX - center.x;
        }

        @Override
        public boolean isViewVisible(Point center, int halfWidth, int halfHeight, int endBound, int extraSpace) {
            int left = center.x - halfWidth;
            int right = center.x + halfWidth;
            return left < (endBound + extraSpace) && right > -extraSpace;
        }

        @Override
        public boolean hasNewBecomeVisible(ScrollRecyclerLayoutManager layoutManager) {
            View firstChild = layoutManager.getFirstChild(), lastChild = layoutManager.getLastChild();
            int left = -layoutManager.getExtraLayoutSpace();
            int right = layoutManager.getWidth() + layoutManager.getExtraLayoutSpace();

            boolean isVisibleFromLeft = layoutManager.getDecoratedLeft(firstChild) > left
                    && layoutManager.getPosition(firstChild) > 0;
            boolean isVisibleFromRight = layoutManager.getDecoratedBottom(lastChild) < right
                    && layoutManager.getPosition(lastChild) < layoutManager.getItemCount() - 1;
            return isVisibleFromLeft || isVisibleFromRight;

        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }

        @Override
        public boolean canScrollHorizontally() {
            return true;
        }
    }

}
