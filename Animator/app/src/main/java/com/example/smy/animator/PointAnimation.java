package com.example.smy.animator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class PointAnimation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_point_animation);
    }
}
