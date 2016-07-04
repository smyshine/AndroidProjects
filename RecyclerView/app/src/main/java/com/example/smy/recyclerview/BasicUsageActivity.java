package com.example.smy.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

public class BasicUsageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BasicRecyclerViewAdapter basicRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_usage);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        basicRecyclerViewAdapter = new BasicRecyclerViewAdapter(this);
        recyclerView.setAdapter(basicRecyclerViewAdapter);
    }

    public void onLinearClick(View v)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        basicRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void onGridClick(View v)
    {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        basicRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void onStaggeredClick(View v)
    {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
        basicRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void onInsertClick(View v)
    {
        basicRecyclerViewAdapter.addData(1);
    }

    public void onRemoveClick(View v)
    {
        basicRecyclerViewAdapter.removeData(1);
    }
}
