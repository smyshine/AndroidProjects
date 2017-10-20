package com.customview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.customview.R;
import com.customview.view.CustomLuckyPlateView;

public class CustomLuckyPlateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_lucky_plate);

        final CustomLuckyPlateView luckyPlateView = (CustomLuckyPlateView) findViewById(R.id.luckPlate);
        final ImageView start = (ImageView) findViewById(R.id.start);
        final TextView hintText = (TextView) findViewById(R.id.text);
        final TextView resultText = (TextView) findViewById(R.id.result);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!luckyPlateView.isStart()){
                    //start.setImageResource(R.drawable.default_no_network);
                    hintText.setText("Click to stop rotating and generate result");
                    luckyPlateView.luckyStart(1);
                } else if (!luckyPlateView.isShouldEnd()){
                    //start.setImageResource(R.drawable.icon_share);
                    hintText.setText("Click to Luck draw again");
                    luckyPlateView.luckyEnd();
                }
            }
        });
        luckyPlateView.setResultListener(new CustomLuckyPlateView.LuckPlateResultListener() {
            @Override
            public void onResult(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setText(result);
                    }
                });
            }
        });
    }
}
