package com.customview.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.customview.R;
import com.customview.activity.scrollrecycler.ScrollRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity implements
        ScrollRecyclerView.ScrollStateChangeListener<ScrollRecyclerAdapter.ViewHolder>,
        ScrollRecyclerView.OnItemChangedListener<ScrollRecyclerAdapter.ViewHolder>{

    private ScrollRecyclerView scrollRecyclerView;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        mDatas = new ArrayList<>(Arrays.asList("Wow", "Hello", "Smyan", "How", "DoYou", "Android",
                "Maybe", "Java", "Kotlin", "DoIt", "Come", "Yes", "Believe", "And", "Beautiful",
                "Quiet", "Slide", "Delete", "Able"));


        scrollRecyclerView = (ScrollRecyclerView) findViewById(R.id.recyclerView);
        scrollRecyclerView.setSlideOnFling(true);
        scrollRecyclerView.setAdapter(new ScrollRecyclerAdapter(mDatas));
        scrollRecyclerView.addOnItemChangedListener(this);
        scrollRecyclerView.addScrollStateChangeListener(this);
        scrollRecyclerView.scrollToPosition(0);
        scrollRecyclerView.setItemTransitionTimeMillis(50);


        /**
         *
          //实现平滑滑动还有最新简单的方法，不会让选中的显示在center位置
         1.create a layout manager class
         public class SmoothLinearLayoutManager extends LinearLayoutManager {

         private Context context;
         private static final float SPEED = 200f;

         public SmoothLinearLayoutManager(Context context) {
         super(context);
         this.context = context;
         }

         @Override
         public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
         LinearSmoothScroller scroller = new LinearSmoothScroller(context) {

         @Override
         protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
         return SPEED / displayMetrics.densityDpi;
         }
         };

         scroller.setTargetPosition(position);
         startSmoothScroll(scroller);
         }
         }

         2.save parent recycler view in adapter
         @Override
         public void onAttachedToRecyclerView(RecyclerView recyclerView) {
         super.onAttachedToRecyclerView(recyclerView);
         parentRecycler = recyclerView;
         }

         3.call smooth scroll when onclik in adapter's viewHolder
         @Override
         public void onClick(View v) {
         parentRecycler.smoothScrollToPosition(getAdapterPosition());
         }

         4.recycler view set layout manager and adapter above
         mRecyclerView = (RecyclerView) findViewById(R.id.rv_filter);
         LinearLayoutManager linearLayoutManager = new SmoothLinearLayoutManager(this);
         linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
         mRecyclerView.setLayoutManager(linearLayoutManager);
         mAdapter = new FilterAdapter(this);
         mRecyclerView.setAdapter(mAdapter);


         *
         * */

    }


    @Override
    public void onScrollStart(@NonNull ScrollRecyclerAdapter.ViewHolder holder, int position) {
        holder.choose(false);
    }

    @Override
    public void onScrollEnd(@NonNull ScrollRecyclerAdapter.ViewHolder holder, int position) {

    }

    @Override
    public void onScroll(float srcollPosition, int currentPosition, int newPosition, @Nullable ScrollRecyclerAdapter.ViewHolder currentHolder, @Nullable ScrollRecyclerAdapter.ViewHolder newHolder) {

    }

    @Override
    public void onCurrentItemChanged(@Nullable ScrollRecyclerAdapter.ViewHolder holder, int position) {
        if (holder != null) {
            holder.choose(true);
        }
    }
}
