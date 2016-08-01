package com.example.smy.wifip2p.Service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.smy.wifip2p.Task.FileServerAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DataTransferService extends IntentService {

    public static final String ACTION_SEND_DATA = "com.example.smy.wifip2p.Service.action.SENDDATA";
    public static final String ACTION_SEND_FILE = "com.example.smy.wifip2p.Service.action.SENDFILE";

    public static final String EXTRA_GROUP_OWNER_ADDRESS = "host_address";
    public static final String EXTRA_GROUP_OWNER_PORT = "host_port";
    public static final String EXTRA_DATA_MESSAGE = "data_message";

    private static final int SOCKET_TIMEOUT = 5000;

    public DataTransferService() {
        super("DataTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String address = intent.getStringExtra(EXTRA_GROUP_OWNER_ADDRESS);
            final String message = intent.getStringExtra(EXTRA_DATA_MESSAGE);
            final int port = intent.getIntExtra(EXTRA_GROUP_OWNER_PORT, -1);

            if (ACTION_SEND_DATA.equals(action)) {
                handleActionSendData(address, port, message);
            } else if (ACTION_SEND_FILE.equals(action)) {
                handleActionSendFile(address, port, message);
            }
        }
    }


    private void handleActionSendData(String host, int port, String message) {
        Socket socket = new Socket();
        try {
            Log.d("xyz", "Opening client socket -- ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            Log.d("xyz", "Client socket - " + socket.isConnected());

            OutputStream stream = socket.getOutputStream();
            stream.write(message.getBytes());
        } catch (IOException e) {
            Log.e("xyz", e.getMessage());
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void handleActionSendFile(String host, int port, String fileUri) {
        Socket socket = new Socket();
        try {
            Log.d("xyz", "Opening client socket - ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            Log.d("xyz", "Client socket - " + socket.isConnected());

            OutputStream stream = socket.getOutputStream();
            ContentResolver cr = getApplicationContext().getContentResolver();
            InputStream is = null;
            try {
                is = cr.openInputStream(Uri.parse(fileUri));
            } catch (FileNotFoundException e) {
                Log.d("xyz", e.toString());
            }
            FileServerAsyncTask.copyFile(is, stream);
            Log.d("xyz", "Client: Data written");
        } catch (IOException e) {
            Log.e("xyz", e.getMessage());
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Give up
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
