package com.customview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.customview.R;
import com.customview.view.CustomCircleMenuLayout;

public class CustomCircleMenuActivity extends Activity {

    private String[] mItemTexts = new String[]{"Save Home", "Special", "Investment", "Mine", "Credit"};
    private int[] mItemImages = new int[]{R.drawable.community_like_nor,
            R.drawable.community_live_hl,
            R.drawable.community_live_nor,
            R.drawable.community_like_hl,
            R.drawable.icon_share};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_circle_menu);

        CustomCircleMenuLayout layout = (CustomCircleMenuLayout) findViewById(R.id.circleMenuLayout);
        layout.setMenuItemIconsAndTexts(mItemImages, mItemTexts);
        layout.setOnMenuItemClickListener(new CustomCircleMenuLayout.onMenuItemClickListener() {
            @Override
            public void itemClick(View v, int postion) {
                Toast.makeText(CustomCircleMenuActivity.this, mItemTexts[postion], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void itemCenterClick(View view) {
                Toast.makeText(CustomCircleMenuActivity.this, "do something", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
