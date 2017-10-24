package com.customview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.customview.activity.CustomCircleMenuActivity;
import com.customview.activity.CustomDrawableStateActivity;
import com.customview.activity.CustomDrawerViewActivity;
import com.customview.activity.CustomFlabbyBirdActivity;
import com.customview.activity.CustomLuckyPlateActivity;
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
import com.customview.activity.GamePuzzleActivity;

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
        findViewById(R.id.tvCustomDrawableState).setOnClickListener(this);
        findViewById(R.id.tvCustomCircleMenu).setOnClickListener(this);
        findViewById(R.id.tvCustomFlabbyBird).setOnClickListener(this);
        findViewById(R.id.tvCustomLuckyPlate).setOnClickListener(this);
        findViewById(R.id.tvCustomGamePuzzle).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvCustomText:
                jump2Activity(CustomViewTextActivity.class);
                break;
            case R.id.tvCustomImage:
                jump2Activity(CustomViewImageActivity.class);
                break;
            case R.id.tvCustomProgress:
                jump2Activity(CustomViewProgressActivity.class);
                break;
            case R.id.tvCustomVolume:
                jump2Activity(CustomViewVolumeActivity.class);
                break;
            case R.id.tvCustomGroup:
                jump2Activity(CustomViewGroupActivity.class);
                break;
            case R.id.tvCustomDrag:
                jump2Activity(CustomViewDragActivity.class);
                break;
            case R.id.tvCustomDrawer:
                jump2Activity(CustomDrawerViewActivity.class);
                break;
            case R.id.tvCustomChangeColor:
                jump2Activity(CustomViewChangeColorActivity.class);
                break;
            case R.id.tvCustomRoundImage:
                jump2Activity(CustomViewRoundImageActivity.class);
                break;
            case R.id.tvCustomSlideDelete:
                jump2Activity(CustomViewSlideDeleteActivity.class);
                break;
            case R.id.tvCustomGestureLock:
                jump2Activity(CustomViewGestureLockActivity.class);
                break;
            case R.id.tvCustomArcMenu:
                jump2Activity(CustomViewArcMenuActivity.class);
                break;
            case R.id.tvCustomShaderRound:
                jump2Activity(CustomRoundImageShaderActivity.class);
                break;
            case R.id.tvCustomDrawableRound:
                jump2Activity(CustomViewDrawableRoundActivity.class);
                break;
            case R.id.tvCustomDrawableState:
                jump2Activity(CustomDrawableStateActivity.class);
                break;
            case R.id.tvCustomCircleMenu:
                jump2Activity(CustomCircleMenuActivity.class);
                break;
            case R.id.tvCustomFlabbyBird:
                jump2Activity(CustomFlabbyBirdActivity.class);
                break;
            case R.id.tvCustomLuckyPlate:
                jump2Activity(CustomLuckyPlateActivity.class);
                break;
            case R.id.tvCustomGamePuzzle:
                jump2Activity(GamePuzzleActivity.class);
                break;
        }
    }

    private void jump2Activity(Class activity){
        startActivity(new Intent(MainActivity.this, activity));
    }
}
