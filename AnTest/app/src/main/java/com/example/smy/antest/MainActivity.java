package com.example.smy.antest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        findViewById(R.id.picbutton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.picbutton:
                Toast.makeText(this, "u just click a picture button", Toast.LENGTH_SHORT);
                break;
        }
    }

    public void onClickListViewArrayAdapter(View v)
    {
        Intent intent = new Intent(MainActivity.this, ListViewArrayAdapterRealize.class);
        startActivity(intent);
    }

    public  void onClickListViewSimpleAdapter(View v)
    {
        Intent intent = new Intent(MainActivity.this, ListViewSimpleAdapter.class);
        startActivity(intent);
    }

    public void onClickListViewBaseAdapter(View v)
    {
        Intent intent = new Intent(MainActivity.this, ListViewBaseAdapterRealize.class);
        startActivity(intent);
    }

    public void onClickServiceStartService(View v)
    {
        Intent intent = new Intent(MainActivity.this, ServiceStartServiceActivity.class);
        startActivity(intent);
    }

    public void onClickServiceBindService(View v)
    {

    }

    public void onClickToast(View v)
    {
        Intent intent = new Intent(MainActivity.this, ToastTestActivity.class);
        startActivity(intent);
    }

    public void onClickBroadcastReceiverBtn(View v)
    {
        Intent intent = new Intent(MainActivity.this, MyBroadcastReceiverActivity.class);
        startActivity(intent);
    }

    public void onClickContactsTestBtn(View v)
    {
        Intent intent = new Intent(MainActivity.this, ContactsTestActivity.class);
        startActivity(intent);
    }

    public void onClickMyProviderBtn(View v)
    {
        Intent intent = new Intent(MainActivity.this, ContentProviderMyActivity.class);
        startActivity(intent);
    }

    public void onClickDialogTestBtn(View v)
    {
        Intent intent = new Intent(MainActivity.this, DialogTestActivity.class);
        startActivity(intent);
    }

}
