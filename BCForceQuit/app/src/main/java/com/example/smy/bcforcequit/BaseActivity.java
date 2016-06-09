package com.example.smy.bcforcequit;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by SMY on 2016/6/9.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActivityController.addActivity(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ActivityController.removeActivity(this);
    }
}
