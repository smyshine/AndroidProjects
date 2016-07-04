package com.example.smy.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBasicUsageClick(View v)
    {
        startActivity(new Intent(this, BasicUsageActivity.class));
    }

    public void onMultiViewClick(View v)
    {
        startActivity(new Intent(this, MultiViewActivity.class));
    }
}
