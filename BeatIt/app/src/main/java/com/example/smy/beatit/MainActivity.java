package com.example.smy.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ChatStatusListener mListener;

    public void setListener(ChatStatusListener lis)
    {
        mListener = lis;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnStartDareClick).setOnClickListener(this);
        findViewById(R.id.btnStartChat).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnStartDareClick:
                showMessage("You are brave, Fine!");
                findViewById(R.id.btnStartDareClick).setVisibility(View.INVISIBLE);
                findViewById(R.id.edtName).setVisibility(View.VISIBLE);
                findViewById(R.id.btnStartChat).setVisibility(View.VISIBLE);
                break;
            case R.id.btnStartChat:
                showMessage("Ok, let's start!");
                onBtnChatStartClick();
                break;
            default:
                break;
        }
    }

    private void onBtnChatStartClick()
    {
        String name = ((EditText)findViewById(R.id.edtName)).getText().toString();
        if(name == null || name == "" || name.length() < 1)
        {
            showMessage("come on man, input your name before get started!");
            return ;
        }

        findViewById(R.id.somethinglikebi).setVisibility(View.INVISIBLE);
        findViewById(R.id.edtName).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnStartChat).setVisibility(View.INVISIBLE);

        if(mListener != null)
        {
            //mListener.onChatStart(name);
        }

        showMessage("o ho ho ho ho, " + name);

        //((TextView)findViewById(R.id.somethinglikebi)).setText("you are fooled " + name);
        //((TextView)findViewById(R.id.somethinglikebi)).setVisibility(View.VISIBLE);
        //showMessage("You are fooled!");

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ListViewTest.class);
        startActivity(intent);
    }

    private Toast mToast;
    public void showMessage(String msg)
    {
        if (msg == null) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
