package com.example.smy.whereau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static final int RequestCodeWhere = 1992;

    TextView tvLocation ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        tvLocation = (TextView)findViewById(R.id.tvLocation);
    }

    public void onClickLocation(View v){
        startActivityForResult(new Intent(MainActivity.this, LocationActivity.class), RequestCodeWhere);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == RequestCodeWhere && resultCode == RESULT_OK) {
            tvLocation.setText(data.getStringExtra(LocationActivity.LOCATION));
        }else{
            tvLocation.setText("No location");
        }
    }
}
