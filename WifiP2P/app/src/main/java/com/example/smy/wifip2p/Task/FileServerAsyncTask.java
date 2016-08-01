package com.example.smy.wifip2p.Task;

import android.os.AsyncTask;
import android.os.Environment;

import com.example.smy.wifip2p.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by SMY on 2016/7/30.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

    MainActivity activity;

    public FileServerAsyncTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            activity.showlog("Task(File) do in back, port 8988.");
            ServerSocket serverSocket = new ServerSocket(8988);
            Socket client = serverSocket.accept();
            final File f = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + "smy" + "/wifip2pshared-"
                            + System.currentTimeMillis() + ".file");

            File dirs = new File(f.getParent());

            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            InputStream inputstream = client.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            client.close();
            return f.getAbsolutePath();

        } catch (IOException e) {
            activity.showlog("IOException, " + e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        activity.showlog("File on post.");

        if (result != null) {
            activity.showlog("File copied - " + result);
            //Intent intent = new Intent();
            //intent.setAction(Intent.ACTION_VIEW);
            //intent.setDataAndType(Uri.parse("file://" + result), "*/*");
            //activity.startActivity(intent);
        }
    }

    @Override
    protected void onPreExecute() {

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while (inputStream != null && (len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
