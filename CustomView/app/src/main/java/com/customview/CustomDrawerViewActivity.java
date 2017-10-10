package com.customview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.customview.view.CustomLeftDrawerLayout;

public class CustomDrawerViewActivity extends Activity implements View.OnClickListener {

    private CustomLeftDrawerLayout drawerLayout;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_drawer_view);

        drawerLayout = (CustomLeftDrawerLayout) findViewById(R.id.drawer);
        tvContent = (TextView) findViewById(R.id.tvContent);

        findViewById(R.id.tvMenu1).setOnClickListener(this);
        findViewById(R.id.tvMenu2).setOnClickListener(this);
        findViewById(R.id.tvMenu3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvMenu1:
                drawerLayout.closeDrawer();
                tvContent.setText("menu1");
                break;
            case R.id.tvMenu2:
                drawerLayout.closeDrawer();
                tvContent.setText("menu2");
                break;
            case R.id.tvMenu3:
                drawerLayout.closeDrawer();
                tvContent.setText("menu3");
                break;
        }
    }
}
