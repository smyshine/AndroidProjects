package com.example.smy.vrplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by SMY on 2016/7/15.
 */
public class MySurfaceView extends SurfaceView {

    public MySurfaceView(Context context){
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }
/*
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        super.onDraw(canvas);
    }*/

    private void init(){
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }
}
