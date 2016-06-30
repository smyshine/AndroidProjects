package com.example.smy.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by SMY on 2016/6/30.
 */
public class MultiViewItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static enum ITEM_TYPE {
        ITEM_TYPE_IMAGE,
        ITEM_TYPE_TEXT
    }

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private ArrayList<String> mTitles;

    public MultiViewItemAdapter(Context context)
    {
        mTitles = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.titles)));
        mContext=context;
        mLayoutInflater=LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TextViewHolder){
            ((TextViewHolder)holder).mTextView.setText(mTitles.get(position));
        }else if(holder instanceof ImageViewHolder){
            ((ImageViewHolder)holder).mTextView.setText(mTitles.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mTitles==null ? 0 : mTitles.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal()){
            return new ImageViewHolder(mLayoutInflater.inflate(R.layout.item_image,parent,false));
        }else{
            return new TextViewHolder(mLayoutInflater.inflate(R.layout.item_text,parent,false));
        }
    }

    @Override
    public int getItemViewType(int position){
        return position%2==0 ? ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal() : ITEM_TYPE.ITEM_TYPE_TEXT.ordinal();
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder{

        TextView mTextView;

        TextViewHolder(View view){
            super(view);
            mTextView = (TextView)view.findViewById(R.id.text_view);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d("TextViewHolder", "onClick--> position = " + getPosition());
                }});
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{

        TextView mTextView;

        ImageView mImageView;

        ImageViewHolder(View view){
            super(view);
            mTextView = (TextView) view.findViewById(R.id.text_view);
            mImageView = (ImageView) view.findViewById(R.id.image_view);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d("ImageViewHolder","onClick--> position = "+getPosition());
                }});
        }
    }

    public void addData(int position) {
        mTitles.add(position, "Insert Two");
        mTitles.add(position, "Insert One");
        notifyItemRangeInserted(position, 2);
    }

    public void removeData(int position) {
        mTitles.remove(position);
        mTitles.remove(position);
        notifyItemRangeRemoved(position, 2);
    }

}
