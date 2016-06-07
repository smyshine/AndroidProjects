package com.example.smy.another;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickTestPage1(View v)
    {
        Intent intent = new Intent(MainActivity.this, TestPage1.class);
        startActivity(intent);
    }

}
