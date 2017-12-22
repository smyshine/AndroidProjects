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
