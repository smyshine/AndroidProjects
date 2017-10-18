package com.customview.activity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.customview.R;
import com.customview.view.CustomCircleImageDrawable;
import com.customview.view.RoundImageDrawable;

public class CustomViewDrawableRoundActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_drawable_round);

        ImageView iv1 = (ImageView) findViewById(R.id.iv1);
        iv1.setImageDrawable(new RoundImageDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.live_tutorial_bg)));

        ImageView iv2 = (ImageView) findViewById(R.id.iv2);
        iv2.setImageDrawable(new RoundImageDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.firmware_upgrade_pic)));

        TextView tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setBackground(new RoundImageDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.firmware_upgrade_pic)));

        ImageView iv3 = (ImageView) findViewById(R.id.iv3);
        iv3.setImageDrawable(new CustomCircleImageDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.firmware_upgrade_pic)));

        ImageView iv4 = (ImageView) findViewById(R.id.iv4);
        iv4.setImageDrawable(new CustomCircleImageDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.live_tutorial_bg)));
    }
}
