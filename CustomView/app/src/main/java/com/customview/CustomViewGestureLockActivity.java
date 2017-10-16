package com.customview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.customview.view.CustomViewGroupGestureLock;

public class CustomViewGestureLockActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_gesture_lock);

        final CustomViewGroupGestureLock lock = (CustomViewGroupGestureLock) findViewById(R.id.gestureLock);
        lock.setAnswer(new int[] {1,2,3,5,8});
        lock.setGestureLockViewListener(new CustomViewGroupGestureLock.GestureLockViewListener() {
            @Override
            public void onBlockSelected(int id) {

            }

            @Override
            public void onGestureResult(boolean matched) {
                Toast.makeText(CustomViewGestureLockActivity.this, "Result : " + matched, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnmatchExceed() {
                Toast.makeText(CustomViewGestureLockActivity.this, "错误此时超过限制", Toast.LENGTH_SHORT).show();
                lock.setMaxRetryCount(5);
            }
        });
    }
}
