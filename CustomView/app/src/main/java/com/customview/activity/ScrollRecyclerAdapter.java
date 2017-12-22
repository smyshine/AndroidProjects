package com.customview.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.customview.R;

import java.util.List;

/**
 * Created by SMY on 2017/12/21.
 */

public class ScrollRecyclerAdapter extends RecyclerView.Adapter<ScrollRecyclerAdapter.ViewHolder>{

    private RecyclerView parentRecycler;
    private List<String> data;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecycler = recyclerView;
    }

    public ScrollRecyclerAdapter(List<String> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.horizon_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        public void choose(boolean choosed){
            textView.setTextColor(choosed ?
                    ContextCompat.getColor(itemView.getContext(), R.color.colorAccent) :
                    ContextCompat.getColor(itemView.getContext(), R.color.a));
        }

        @Override
        public void onClick(View v) {
            parentRecycler.smoothScrollToPosition(getAdapterPosition());
        }
    }
}
