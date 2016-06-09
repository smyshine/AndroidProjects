package com.example.smy.sharedpreference;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button saveData;
    private Button restoreData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveData = (Button) findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("name", "Cherry");
                editor.putInt("age", 18);
                editor.putBoolean("married", false);
                editor.commit();
            }
        });

        restoreData = (Button) findViewById(R.id.restore_data);
        restoreData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
                String name = preferences.getString("name", "Unknown");
                int age = preferences.getInt("age", 0);
                boolean married = preferences.getBoolean("married", false);
                Toast.makeText(getApplicationContext(), "Resotre success! \n name :" + name + ", \nage :" + age + ",\nmarried : " + married , Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
