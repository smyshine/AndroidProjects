package com.example.smy.animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class CombinationAnimation extends Activity {

    private AnimatorSet animatorSet = new AnimatorSet();

    private ObjectAnimator animator = null;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_combination_animation);
        textView = (TextView) findViewById(R.id.text_view);
    }

    public void onClickAnimationSet1(View v)
    {
        if (animatorSet != null && animatorSet.isRunning())
        {
            Toast.makeText(getApplicationContext(), "Please wait !", Toast.LENGTH_SHORT).show();
            return;
        }

        ObjectAnimator moveIn = ObjectAnimator.ofFloat(textView, "translationX", -800f, 0f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(textView, "rotation", 0f, 360f);
        ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
        animatorSet.play(rotate).with(fadeInOut).after(moveIn);
        animatorSet.setDuration(5000);
        animatorSet.start();
    }
}
