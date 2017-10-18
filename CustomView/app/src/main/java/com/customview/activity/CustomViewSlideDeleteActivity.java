package com.customview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.customview.R;
import com.customview.view.CustomViewSlideDelete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomViewSlideDeleteActivity extends Activity {

    private CustomViewSlideDelete mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_slide_delete);

        mListView = (CustomViewSlideDelete) findViewById(R.id.listView);
        mDatas = new ArrayList<>(Arrays.asList("Wow", "Hello", "Smyan", "How", "DoYou", "Android",
                "Maybe", "Java", "Kotlin", "DoIt", "Come", "Yes", "Believe", "And", "Beautiful",
                "Quiet", "Slide", "Delete", "Able"));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDatas);
        mListView.setAdapter(mAdapter);

        mListView.setListener(new CustomViewSlideDelete.DeleteClickListener() {
            @Override
            public void Click(int position) {
                Toast.makeText(CustomViewSlideDeleteActivity.this, "Delete, " + position + " : " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
                mAdapter.remove(mAdapter.getItem(position));
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CustomViewSlideDeleteActivity.this, "Click, " + position + " : " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
