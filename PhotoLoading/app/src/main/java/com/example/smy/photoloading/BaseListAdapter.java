package com.example.smy.photoloading;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by SMY on 2016/9/2.
 */
public class BaseListAdapter extends BaseAdapter {
    protected int mResourceId;

    public BaseListAdapter(int mResourceId) {
        this.mResourceId = mResourceId;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }

    public static final class ViewHolder {

        private SparseArray<View> viewArray;

        private View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public <V extends View> V getView(int id) {
            return (V) findView(id);
        }

        public TextView getTextView(int id) {
            return getView(id);
        }

        public Button getButton(int id) {
            return getView(id);
        }

        public ImageView getImageView(int id) {
            return getView(id);
        }

        public RelativeLayout getRelativeLayout(int id) {
            return getView(id);
        }

        private View findView(int id) {
            if (viewArray == null) {
                viewArray = new SparseArray<View>();
            }
            View view = viewArray.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                viewArray.put(id, view);
            }
            return view;
        }

        public View getItemView() {
            return itemView;
        }
    }
}
