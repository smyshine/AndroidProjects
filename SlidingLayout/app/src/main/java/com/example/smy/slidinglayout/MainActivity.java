package com.example.smy.slidinglayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SlideLayout slideLayout;
    private Button menuButton;
    private ListView contentListView;
    private ArrayAdapter<String> contentListAdapter;

    private String[] contentItems = {
            "Content Item 1", "Content Item 2", "Content Item 3", "Content Item 4", "Content Item 5",
            "Content Item 6", "Content Item 7", "Content Item 8", "Content Item 9", "Content Item 10",
            "Content Item 11", "Content Item 12", "Content Item 13", "Content Item 14", "Content Item 15",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slideLayout = (SlideLayout) findViewById(R.id.slidinglayout);
        menuButton = (Button) findViewById(R.id.menuButton);
        contentListView = (ListView) findViewById(R.id.contentList);
        contentListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contentItems);
        contentListView.setAdapter(contentListAdapter);

        slideLayout.setScrollEvent(contentListView);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slideLayout.isLeftLayoutVisible()) {
                    slideLayout.scrollToRightLayout();
                } else {
                    slideLayout.scrollToLeftLayout();
                }
            }
        });

        contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = contentItems[position];
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
