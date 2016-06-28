package com.example.smy.animator;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class SingleAnimation extends Activity {

    private ObjectAnimator animator = null;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_single_animation);
        textView = (TextView) findViewById(R.id.text_view);
    }

    public void onClickAlpha(View v)
    {
        if (animator != null && animator.isRunning())
        {
            Toast.makeText(getApplicationContext(), "Please wait !", Toast.LENGTH_SHORT).show();
            return;
        }

        animator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
        animator.setDuration(5000);
        animator.start();
    }

    public void onClickRotation(View v)
    {
        if (animator != null && animator.isRunning())
        {
            Toast.makeText(getApplicationContext(), "Please wait !", Toast.LENGTH_SHORT).show();
            return;
        }

        animator = ObjectAnimator.ofFloat(textView, "rotation", 0f, 360f);
        animator.setDuration(5000);
        animator.start();
    }

    public void onClickTranslation(View v)
    {
        if (animator != null && animator.isRunning())
        {
            Toast.makeText(getApplicationContext(), "Please wait !", Toast.LENGTH_SHORT).show();
            return;
        }

        float currentTranslationX = textView.getTranslationX();
        animator = ObjectAnimator.ofFloat(textView, "translationX", currentTranslationX, -800f, currentTranslationX, 800f, currentTranslationX);
        animator.setDuration(6000);
        animator.start();
    }

    public void onClickScaleY(View v)
    {
        if (animator != null && animator.isRunning())
        {
            Toast.makeText(getApplicationContext(), "Please wait !", Toast.LENGTH_SHORT).show();
            return;
        }

        animator = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 5f, 0f, 1f);
        animator.setDuration(6000);
        animator.start();
    }
}
