package com.example.smy.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by SMY on 2016/6/29.
 */
public class BasicRecyclerViewAdapter extends RecyclerView.Adapter<BasicRecyclerViewAdapter.BasicTextViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Context context;
    private ArrayList<String> titles;

    public BasicRecyclerViewAdapter(Context context){
        titles = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.titles)));
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public BasicTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BasicTextViewHolder(layoutInflater.inflate(R.layout.item_text,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(BasicTextViewHolder holder, int position) {
        holder.mTextView.setText(titles.get(position));
    }


    @Override
    public int getItemCount() {
        return titles == null ? 0 : titles.size();
    }

    public static class BasicTextViewHolder extends RecyclerView.ViewHolder{

        TextView mTextView;

        public BasicTextViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    public void addData(int position) {
        titles.add(position, "Insert One");
        notifyItemInserted(position);
    }

    public void removeData(int position) {
        titles.remove(position);
        notifyItemRemoved(position);
    }

}
