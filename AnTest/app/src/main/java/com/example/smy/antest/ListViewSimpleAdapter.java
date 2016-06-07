package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewSimpleAdapter extends Activity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_simple_adapter);

        mListView = (ListView) findViewById(R.id.list_item_simple);
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        for(int i = 0; i < 10; ++i)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", R.drawable.penguins);
            map.put("ItemTitle", "No " + i + " Line");
            map.put("ItemText", "wow Hint no " + i + "Line ha ha ha");
            listItem.add(map);
        }

        final SimpleAdapter msAdapter = new SimpleAdapter(this,
                listItem,
                R.layout.list_item_simple_adapter,
                new String[] {"ItemImage", "ItemTitle", "ItemText"},
                new int[] {R.id.ItemImage, R.id.ItemTitle, R.id.ItemText});

        mListView.setAdapter(msAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("a yo yo~")
                        .setMessage("you just clicked " + position)// msAdapter.getItem(position).toString())
                        .setPositiveButton("OK OK OK I Know", null)
                        .show();
            }
        });
    }
}
