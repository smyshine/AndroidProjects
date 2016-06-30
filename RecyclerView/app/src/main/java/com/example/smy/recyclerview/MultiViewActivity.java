package com.example.smy.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

public class MultiViewActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MultiViewItemAdapter multiViewItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_view);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        multiViewItemAdapter = new MultiViewItemAdapter(this);
        recyclerView.setAdapter(multiViewItemAdapter);
    }

    public void onLinearClick(View v)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        multiViewItemAdapter.notifyDataSetChanged();
    }

    public void onGridClick(View v)
    {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        multiViewItemAdapter.notifyDataSetChanged();
    }

    public void onStaggeredClick(View v)
    {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
        multiViewItemAdapter.notifyDataSetChanged();
    }

    public void onInsertClick(View v)
    {
        multiViewItemAdapter.addData(1);
        //recyclerView.getAdapter().notifyItemRangeInserted(2, 2);
    }

    public void onRemoveClick(View v)
    {
        multiViewItemAdapter.removeData(1);
        //recyclerView.getAdapter().notifyItemRangeRemoved(2, 2);
    }
}
