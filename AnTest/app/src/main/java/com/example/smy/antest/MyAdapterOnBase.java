package com.example.smy.antest;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SMY on 2016/6/1.
 */
public class MyAdapterOnBase extends BaseAdapter {

    ArrayList<HashMap<String, Object>> listItems;

    private LayoutInflater mInflater;

    public MyAdapterOnBase(Context context, ArrayList<HashMap<String, Object>> items)
    {
        this.listItems = items;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return listItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.list_item_my_base_adapter, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.ItemTitleB);
            holder.text = (TextView) convertView.findViewById(R.id.ItemTextB);
            holder.btn = (Button) convertView.findViewById(R.id.ItemButton);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(listItems.get(position).get("ItemTitle").toString());
        holder.text.setText(listItems.get(position).get("ItemText").toString());

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("a ho ho~")
                        .setMessage("you just clicked me! taoqi~")
                        .setPositiveButton("OK OK OK I Know", null)
                        .show();
            }
        });

        return  convertView;
    }

    public final class ViewHolder
    {
        public TextView title;
        public TextView text;
        public Button btn;
    }

}
