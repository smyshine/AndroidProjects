package com.example.smy.actionbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            setOverflowShowingAlways();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onMenuOpened(int featureId, Menu menu) {
            if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
                if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                    try {
                        Method m = menu.getClass().getDeclaredMethod(
                                "setOptionalIconsVisible", Boolean.TYPE);
                        m.setAccessible(true);
                        m.invoke(menu, true);
                    } catch (Exception e) {
                    }
                }
            }
            return super.onMenuOpened(featureId, menu);
        }

    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
