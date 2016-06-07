package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewBaseAdapterRealize extends Activity {

    private ListView mListView;
    private MyAdapterOnBase mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_base_adapter_realize);

        mListView = (ListView) findViewById(R.id.listViewBaseAdapter);
        initAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("ei yo cute~")
                        .setMessage("you just clicked item " + mAdapter.getItem(position).toString())
                        .setPositiveButton(" OK I Know, Go Away", null)
                        .show();
            }
        });
    }

    private void initAdapter()
    {
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
        for(int i=0;i<30;i++)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemTitle", "No "+ i + " Line");
            map.put("ItemText", "This is " + i + " line la ba yep why not");
            listItem.add(map);
        }

        mAdapter = new MyAdapterOnBase(this, listItem);
    }
}
