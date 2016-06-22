package com.example.smy.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;

public class MainActivity extends Activity {

    private static final int SCAN_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scan = (Button) findViewById(R.id.scan_btn);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case SCAN_CODE:
                TextView result = (TextView) findViewById(R.id.scan_result);
                if(resultCode == RESULT_OK)
                {
                    String r = data.getStringExtra("scan_result");
                    result.setText(r);
                }
                else
                {
                    result.setText("Scan Error!");
                }
                break;
            default:
                break;
        }
    }
}
