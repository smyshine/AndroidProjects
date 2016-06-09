package com.example.smy.bcforcequit;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

/**
 * Created by SMY on 2016/6/9.
 */
public class ForceOfflineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(context);
        dialogBuiler.setTitle("Warning");
        dialogBuiler.setMessage("You are forced to be offline.Please try to login again.");
        dialogBuiler.setCancelable(false);
        dialogBuiler.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityController.finishAll();
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
        AlertDialog alertDialog = dialogBuiler.create();
        alertDialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}
