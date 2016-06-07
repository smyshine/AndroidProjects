package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DialogTestActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_test);

        ((Button)findViewById(R.id.btnExit)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnContent)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnView)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnSingleChoice)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnMultiChoice)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnList)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnFree)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnExit:
                onBtnExitClick();
                break;
            case R.id.btnContent:
                onContentClick();
                break;
            case R.id.btnView:
                onViewClick();
                break;
            case R.id.btnSingleChoice:
                onSingleChoiceClick();
                break;
            case R.id.btnMultiChoice:
                onMultiChoiceClick();
                break;
            case R.id.btnList:
                onListClick();
                break;
            case R.id.btnFree:
                onFreeClick();
                break;
            default:
                break;
        }
    }

    private void onBtnExitClick()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to Exit?");
        builder.setTitle("Query");
        builder.setPositiveButton("Yes, Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("No, Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void onContentClick()
    {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("It's a small query")
                .setMessage("Do you like the Game of Thrones")
                .setPositiveButton("Very much!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "I love tha song of fire and ice", Toast.LENGTH_LONG)
                                .show();
                    }
                }).setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "I don't like tha Game of Thrones", Toast.LENGTH_LONG)
                                .show();
                    }
                }).setNeutralButton("Just so so", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "Hard to say", Toast.LENGTH_LONG)
                                .show();
                    }
                }).create();
        dialog.show();
    }

    private void onViewClick()
    {
        new AlertDialog.Builder(this).setTitle("Just input something")
                .setView(new EditText(this))
                .setPositiveButton("   OK ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "you input", Toast.LENGTH_LONG);
                    }
                }).setNegativeButton("Cancel ", null)
                .show();
    }

    private void onSingleChoiceClick()
    {
        new AlertDialog.Builder(this)
                .setTitle("Multi Choose")
                .setSingleChoiceItems(new String[]{"Monday", "Tomorrow", "Tuedday"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onMultiChoiceClick()
    {
        new AlertDialog.Builder(this)
                .setTitle("Multi Choose")
                .setMultiChoiceItems(new String[]{"Today", "Tomorrow", "Yesterday"}, null, null)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onListClick()
    {
        new AlertDialog.Builder(this)
                .setTitle("List View")
                .setItems(new String[] { "Item1", "Item2" }, null)
                .setNegativeButton("OK", null)
                .show();
    }

    private void onFreeClick()
    {
        new AlertDialog.Builder(this)
                .setTitle("FreeStyle")
                .setMessage("Want to see free style? Go back to check the Contact test!")
                .setPositiveButton("OK I know", null)
                .show();
    }
}
