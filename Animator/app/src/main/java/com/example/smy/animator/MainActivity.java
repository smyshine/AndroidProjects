package com.example.smy.animator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void onClickSingleAnimation(View v)
    {
        Intent intent = new Intent(MainActivity.this, SingleAnimation.class);
        startActivity(intent);
    }

    public void onClickCombinationAnimation(View v)
    {
        Intent intent = new Intent(MainActivity.this, CombinationAnimation.class);
        startActivity(intent);
    }

    public void onClickPointAnimation(View v)
    {
        Intent intent = new Intent(MainActivity.this, PointAnimation.class);
        startActivity(intent);
    }

    public void onClickDrawShape(View v)
    {
        Intent intent = new Intent(MainActivity.this, DrawShape.class);
        startActivity(intent);
    }

    public void onClickDrawPath(View v)
    {

    }
}
