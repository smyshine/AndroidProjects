package com.customview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.customview.activity.CustomDrawerViewActivity;
import com.customview.activity.CustomRoundImageShaderActivity;
import com.customview.activity.CustomViewArcMenuActivity;
import com.customview.activity.CustomViewChangeColorActivity;
import com.customview.activity.CustomViewDragActivity;
import com.customview.activity.CustomViewDrawableRoundActivity;
import com.customview.activity.CustomViewGestureLockActivity;
import com.customview.activity.CustomViewGroupActivity;
import com.customview.activity.CustomViewImageActivity;
import com.customview.activity.CustomViewProgressActivity;
import com.customview.activity.CustomViewRoundImageActivity;
import com.customview.activity.CustomViewSlideDeleteActivity;
import com.customview.activity.CustomViewTextActivity;
import com.customview.activity.CustomViewVolumeActivity;

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
        findViewById(R.id.tvCustomRoundImage).setOnClickListener(this);
        findViewById(R.id.tvCustomSlideDelete).setOnClickListener(this);
        findViewById(R.id.tvCustomGestureLock).setOnClickListener(this);
        findViewById(R.id.tvCustomArcMenu).setOnClickListener(this);
        findViewById(R.id.tvCustomShaderRound).setOnClickListener(this);
        findViewById(R.id.tvCustomDrawableRound).setOnClickListener(this);
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
            case R.id.tvCustomRoundImage:
                startActivity(new Intent(MainActivity.this, CustomViewRoundImageActivity.class));
                break;
            case R.id.tvCustomSlideDelete:
                startActivity(new Intent(MainActivity.this, CustomViewSlideDeleteActivity.class));
                break;
            case R.id.tvCustomGestureLock:
                startActivity(new Intent(MainActivity.this, CustomViewGestureLockActivity.class));
                break;
            case R.id.tvCustomArcMenu:
                startActivity(new Intent(MainActivity.this, CustomViewArcMenuActivity.class));
                break;
            case R.id.tvCustomShaderRound:
                startActivity(new Intent(MainActivity.this, CustomRoundImageShaderActivity.class));
                break;
            case R.id.tvCustomDrawableRound:
                startActivity(new Intent(MainActivity.this, CustomViewDrawableRoundActivity.class));
                break;
        }
    }
}
