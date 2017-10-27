package com.customview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.customview.activity.ClipImageActivity;
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
import com.customview.activity.Game2048Activity;
import com.customview.activity.GamePuzzleActivity;

public class MainActivity extends AppCompatActivity {

    private enum Item{
        CustomText(R.id.tvCustomText, CustomViewTextActivity.class),
        CustomImage(R.id.tvCustomImage, CustomViewImageActivity.class),
        CustomProgress(R.id.tvCustomProgress, CustomViewProgressActivity.class),
        CustomVolume(R.id.tvCustomVolume, CustomViewVolumeActivity.class),
        CustomGroup(R.id.tvCustomGroup, CustomViewGroupActivity.class),
        CustomDrag(R.id.tvCustomDrag, CustomViewDragActivity.class),
        CustomDrawer(R.id.tvCustomDrawer, CustomDrawerViewActivity.class),
        CustomChangeColor(R.id.tvCustomChangeColor, CustomViewChangeColorActivity.class),
        CustomRoundImage(R.id.tvCustomRoundImage, CustomViewRoundImageActivity.class),
        CustomSlideDelete(R.id.tvCustomSlideDelete, CustomViewSlideDeleteActivity.class),
        CustomGestureLock(R.id.tvCustomGestureLock, CustomViewGestureLockActivity.class),
        ArcMenu(R.id.tvCustomArcMenu, CustomViewArcMenuActivity.class),
        ShaderRoundImage(R.id.tvCustomShaderRound, CustomRoundImageShaderActivity.class),
        DrawableRoundImage(R.id.tvCustomDrawableRound, CustomViewDrawableRoundActivity.class),
        DrawableState(R.id.tvCustomDrawableState, CustomDrawableStateActivity.class),
        CircleMenu(R.id.tvCustomCircleMenu, CustomCircleMenuActivity.class),
        FlabbyBird(R.id.tvCustomFlabbyBird, CustomFlabbyBirdActivity.class),
        LuckyPlate(R.id.tvCustomLuckyPlate, CustomLuckyPlateActivity.class),
        Puzzle(R.id.tvCustomGamePuzzle, GamePuzzleActivity.class),
        Game2018(R.id.tvCustomGame2048, Game2048Activity.class),
        ClipImage(R.id.tvClipImage, ClipImageActivity.class),
        ;

        int id;
        Class activity;

        Item(int id, Class activity) {
            this.id = id;
            this.activity = activity;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setClickListener(Item.values());
    }

    private void setClickListener(final Item... items){
        for (final Item item : items){
            findViewById(item.id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jump2Activity(item.activity);
                }
            });
        }
    }

    private void jump2Activity(Class activity){
        startActivity(new Intent(MainActivity.this, activity));
    }
}
