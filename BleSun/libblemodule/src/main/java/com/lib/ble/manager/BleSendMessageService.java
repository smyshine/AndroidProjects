package com.lib.ble.manager;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by SMY on 2017/1/18.
 * 发送数据service
 */
public class BleSendMessageService extends IntentService {

    public static final String OPERATION = "operation";

    public BleSendMessageService(){
        super("BleSendMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int what = intent.getIntExtra(OPERATION, 0);
        BleLogger.print("BleSendMessageService", "handleMessage intent " + what);
        BleManager.getInstance().doSendMessage(what);
    }
}
