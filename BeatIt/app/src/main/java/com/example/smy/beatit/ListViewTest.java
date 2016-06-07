package com.example.smy.beatit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListViewTest extends AppCompatActivity {

    private static final String[] strs = new String[]{"first", "Tuesday", "third", "what about", "come on!", "Hateful", "KnowNothing", "SongPlay"};
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_test);

        lv = (ListView)findViewById(R.id.lvtest1);
        lv.setAdapter(new ArrayAdapter< String >(this, R.layout.list_view_item_1, strs));
    }
}
