package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListViewArrayAdapterRealize extends Activity {

    private static final String[] mStrs = new String[] {
            "first", "second", "Wednesday", "family", "Friday", "yep",
            "Jon Snow", "Eson Wong", "Elena Darlk", "Flash", "Vampire",
            "Hero", "More than I can say", "Just do it"
    };

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_array_adapter_realize);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item_array_adapter, mStrs));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("a ha ~")
                        .setMessage("why click " + mStrs[position])
                        .setPositiveButton("OK OK I Know", null)
                        .show();
            }
        });
    }
}
