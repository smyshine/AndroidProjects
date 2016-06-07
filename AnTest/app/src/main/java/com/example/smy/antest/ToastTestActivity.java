package com.example.smy.antest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastTestActivity extends Activity {

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_test);
    }

    private void checkToast()
    {
        if(mToast != null)
        {
            mToast.cancel();
        }
    }
    public void onClickDefaultBtn(View v)
    {
        checkToast();
        mToast = Toast.makeText(getApplicationContext(), "Default Toast Style", Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void onClickUdPositionBtn(View v)
    {
        checkToast();
        mToast = Toast.makeText(getApplicationContext(), "User Defined pos Toast Style", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    public void onClickPictureToastBtn(View v)
    {
        checkToast();
        mToast = Toast.makeText(getApplicationContext(), "Picture-Carry Toast Style", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) mToast.getView();
        ImageView imagecp = new ImageView(getApplicationContext());
        imagecp.setImageResource(R.drawable.image_tulips);
        toastView.addView(imagecp, 0);
        mToast.show();
    }

    public void onClickTotalFreeToastBtn(View v)
    {
        checkToast();
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_test_custom, (ViewGroup) findViewById(R.id.llToast));
        ImageView image = (ImageView) layout.findViewById(R.id.tvImageToast);
        image.setImageResource(R.drawable.image_penguins);
        TextView title = (TextView) layout.findViewById(R.id.tvTitleToast);
        title.setText("Attention Please");
        TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
        text.setText("Totally free style of Toast");

        mToast = new Toast(getApplicationContext());
        mToast.setGravity(Gravity.RIGHT | Gravity.TOP, 12, 40);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(layout);
        mToast.show();
    }

    public void onClickTotalOtherThreadBtn(View v)
    {
        checkToast();
        new Thread(new Runnable() {
            @Override
            public void run() {
                showToast();
            }
        }).start();
    }

    Handler handler = new Handler();

    public void showToast()
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Hi there, I come from other thread!", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
