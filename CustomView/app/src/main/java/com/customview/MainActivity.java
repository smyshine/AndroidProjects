package com.customview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tvCustomText).setOnClickListener(this);
        findViewById(R.id.tvCustomImage).setOnClickListener(this);
        findViewById(R.id.tvCustomProgress).setOnClickListener(this);
        findViewById(R.id.tvCustomVolume).setOnClickListener(this);
        findViewById(R.id.tvCustomGroup).setOnClickListener(this);
        findViewById(R.id.tvCustomDrag).setOnClickListener(this);
        findViewById(R.id.tvCustomDrawer).setOnClickListener(this);
        findViewById(R.id.tvCustomChangeColor).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvCustomText:
                startActivity(new Intent(MainActivity.this, CustomViewTextActivity.class));
                break;
            case R.id.tvCustomImage:
                startActivity(new Intent(MainActivity.this, CustomViewImageActivity.class));
                break;
            case R.id.tvCustomProgress:
                startActivity(new Intent(MainActivity.this, CustomViewProgressActivity.class));
                break;
            case R.id.tvCustomVolume:
                startActivity(new Intent(MainActivity.this, CustomViewVolumeActivity.class));
                break;
            case R.id.tvCustomGroup:
                startActivity(new Intent(MainActivity.this, CustomViewGroupActivity.class));
                break;
            case R.id.tvCustomDrag:
                startActivity(new Intent(MainActivity.this, CustomViewDragActivity.class));
                break;
            case R.id.tvCustomDrawer:
                startActivity(new Intent(MainActivity.this, CustomDrawerViewActivity.class));
                break;
            case R.id.tvCustomChangeColor:
                startActivity(new Intent(MainActivity.this, CustomViewChangeColorActivity.class));
                break;
        }
    }
}
