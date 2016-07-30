package com.example.smy.wifip2p.Task;

import android.os.AsyncTask;

import com.example.smy.wifip2p.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by SMY on 2016/7/30.
 */
public class DataServerAsyncTask  extends AsyncTask<Void, Void, String> {

    private MainActivity activity;

    public DataServerAsyncTask(MainActivity activity) {
        this.activity=activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            activity.showlog("Task data do in back, port 8888.");
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();
            InputStream inputstream = client.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = inputstream.read()) != -1) {
                baos.write(i);
            }
            String str = baos.toString();
            serverSocket.close();
            return str;

        } catch (IOException e) {
            activity.showlog("IOException, " + e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        activity.showlog("Receive String: " + result + ".");
    }

    @Override
    protected void onPreExecute() {

    }
}
