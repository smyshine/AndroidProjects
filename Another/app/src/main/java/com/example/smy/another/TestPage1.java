package com.example.smy.another;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TestPage1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page1);
    }

    public void onClickT1Drag(View v)
    {
        Intent intent = new Intent(this, DropAndDrag.class);
        startActivity(intent);
    }
}
