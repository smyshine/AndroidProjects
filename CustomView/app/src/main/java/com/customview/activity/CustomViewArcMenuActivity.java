package com.customview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.customview.R;
import com.customview.view.CustomViewArcMenu;

public class CustomViewArcMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_arc_menu);

        CustomViewArcMenu arcMenu = (CustomViewArcMenu) findViewById(R.id.arcMenu);
        arcMenu.setItemClickListener(new CustomViewArcMenu.OnMenuItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Toast.makeText(CustomViewArcMenuActivity.this, pos + ":" + view.getTag(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
