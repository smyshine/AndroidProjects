package com.example.smy.photoloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by SMY on 2016/9/2.
 */
public class SingleButtonMenu extends LinearLayout implements View.OnClickListener {

    private ImageView button;

    private SingleMenuClickListener listener;

    public SingleButtonMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleButtonMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SingleButtonMenu, defStyle, 0);
        initView(array);
        array.recycle();
    }

    private void initView(TypedArray array) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.single_button_menu, this);
        button = (ImageView) findViewById(R.id.middleButton);
        rootView.setOnClickListener(this);
        Drawable d = array.getDrawable(R.styleable.SingleButtonMenu_buttonSrc);
        if (d != null) {
            button.setImageDrawable(d);
        }
    }

    public void setMenuClickListener(SingleMenuClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick();
        }
    }

    /**
     * button是否可以点击
     * @param enable
     */
    public void setButtonEnable(boolean enable, int resId){
        button.setEnabled(enable);
        button.setImageResource(resId);
    }

    public interface SingleMenuClickListener {
        void onClick();
    }
}
