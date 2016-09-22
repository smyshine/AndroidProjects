package com.example.smy.collapsingtextview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String text = "123我听见雨滴落在青青草地，我听见远方下课钟声响起，可是我没有听见的的声音，" +
                "认真呼唤我姓名~~~~(≧▽≦)~啦啦啦~~~为什么没有发现遇见了你，是生命最美好的事情，" +
                "是谁一直在风里雨里一直默默呼唤我姓名~~~那为我对抗世界的勇气~~~" +
                "与你相遇好幸运";
        CollapsibleTextView textView = (CollapsibleTextView) findViewById(R.id.collapsibleTextView);
        textView.setDesc(text, TextView.BufferType.NORMAL);
    }
}
