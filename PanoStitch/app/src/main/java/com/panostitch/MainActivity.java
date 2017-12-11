package com.panostitch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stitcher.getInstance().imageStitch("/storage/emulated/0/vrsmy/stitch-in.jpg",
                        "/storage/emulated/0/vrsmy/stitch-out.jpg",
                        "/storage/emulated/0/vrsmy/stitch.txt"
                );
            }
        });
    }

}
