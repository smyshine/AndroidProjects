package com.example.smy.bidirsliding;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    private BidirSlidingLayout bidirSlidingLayout;
    private ListView contentList;
    private ArrayAdapter<String> contentAdapter;

    private String[] contentItems = {"Content Item 1","Content Item 2","Content Item 3","Content Item 4",
            "Content Item 5","Content Item 6","Content Item 7","Content Item 8","Content Item 9",
            "Content Item 10","Content Item 11","Content Item 12","Content Item 13","Content Item 14",
            "Content Item 15","Content Item 16","Content Item 17","Content Item 18","Content Item 19"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bidirSlidingLayout = (BidirSlidingLayout) findViewById(R.id.bidir_sliding_layout);
        contentList = (ListView) findViewById(R.id.content_list);
        contentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contentItems);
        contentList.setAdapter(contentAdapter);
        bidirSlidingLayout.setScrollEvent(contentList);
    }
}
