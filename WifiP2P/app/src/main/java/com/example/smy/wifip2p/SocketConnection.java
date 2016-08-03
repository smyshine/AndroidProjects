package com.example.smy.wifip2p;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by SMY on 2016/8/1.
 */
public class SocketConnection {

    MainActivity activity;

    private LinkedBlockingQueue<String> mMessageQueue;

    private String mCurrentMessage;
    private MessageHandleThread mMessageHandlerThread;

    private static final int BUFFER_SIZE = 1024 * 16 * 2;
    private static final int SOCKET_TIMEOUT = 10000;
    private static final long SLEEP_TIMEOUT = 50L;

    private boolean isServer = false;

    SocketConnection(MainActivity activity) {
        this.activity = activity;
    }

    private void addMessage(String message) {
        mMessageQueue.add(message);
    }

    void sendMessage(String message) {
        if (mMessageQueue != null) {
            addMessage(message);
        }
    }

    FileHandleThread fileHandleThread = null;
    void receiveFile(String host, int port){
        stopFileHandlerThread();

        fileHandleThread = new FileHandleThread(host, port, null, isServer, false);
        fileHandleThread.start();
    }

    void sendFile(String host, int port, String uri){
        stopFileHandlerThread();

        fileHandleThread = new FileHandleThread(host, port, uri, isServer, true);
        fileHandleThread.start();
    }

    void start(String host, int port, boolean isServer) {
        this.isServer = isServer;

        stopMessageHandlerThread();

        if (mMessageQueue == null) {
            mMessageQueue = new LinkedBlockingQueue<String>();
        }

        if (mMessageHandlerThread == null || !mMessageHandlerThread.isAlive()) {
            mMessageHandlerThread = new MessageHandleThread(host,port, isServer);
            mMessageHandlerThread.start();
        }
    }

    private void stopMessageHandlerThread(){
        if (mMessageHandlerThread != null) {
            mMessageHandlerThread.stop = true;
            mMessageHandlerThread.closeSocket();
            mMessageHandlerThread.interrupt();
            try {
                mMessageHandlerThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mMessageHandlerThread = null;
        }
    }

    private void stopFileHandlerThread(){
        if(fileHandleThread != null){
            fileHandleThread.interrupt();
            try {
                fileHandleThread.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fileHandleThread = null;
        }
    }

    public void stop() {
        stopMessageHandlerThread();

        stopFileHandlerThread();
    }

    private class MessageHandleThread extends Thread {
        private boolean stop;
        private char[] cbuf;
        private InputStreamReader receiver;
        private OutputStreamWriter sender;
        private Socket socket;
        private String host;
        private int  port;
        private boolean isServer;

        MessageHandleThread(String host, int port, boolean server){
            this.host = host;
            this.port = port;
            this.isServer = server;
        }

        @Override
        public void run() {
            super.run();
            if (initialize(isServer)){
                while (!stop) {
                    if (mMessageQueue.size() > 0) {
                        mCurrentMessage = mMessageQueue.poll();
                        if (mCurrentMessage == null) {
                            continue;
                        }
                        sendMessage(mCurrentMessage);
                    } else {
                        String msg = receiveMessage();
                        if (!TextUtils.isEmpty(msg) && activity != null){
                            activity.onReceiveMessage(msg);
                        }
                    }
                }
                if (mMessageQueue != null) {
                    mMessageQueue.clear();
                    mMessageQueue = null;
                }

                mCurrentMessage = null;
            }else {
                if (mCurrentMessage != null) {
                    mCurrentMessage = null;
                }
            }

            if (receiver != null) {
                try {
                    receiver.close();
                    receiver = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (sender != null) {
                try {
                    sender.close();
                    sender = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean initialize(boolean isServer){
            for (int count = 2; count >= 0; count--) {
                if (stop) {
                    return false;
                }

                try {
                    if (isServer){
                        ServerSocket serverSocket = new ServerSocket(port);
                        socket = serverSocket.accept();
                    } else{
                        socket = new Socket();
                        socket.bind(null);
                        socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    }

                    receiver = new InputStreamReader(socket.getInputStream(), "UTF-8");
                    sender = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    cbuf = new char[BUFFER_SIZE];
                    activity.showlog("start socket success .");
                    return true;
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000L);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        e1.printStackTrace();
                    } catch (Exception ei){
                        ei.printStackTrace();
                    }
                }
            }

            return false;
        }

        private void closeSocket() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String receiveMessage() {
            try {
                if (receiver.ready()) {
                    int len = receiver.read(cbuf);
                    if (len != -1){
                        if (!stop){
                            StringWriter writer = new StringWriter();
                            writer.write(cbuf, 0, len);
                            return writer.toString();
                        }
                    }
                } else {
                    Thread.sleep(SLEEP_TIMEOUT);
                }
            }catch(InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                activity.showlog("recv msg exception: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private void sendMessage(String message) {
            try {
                activity.showlog("send message " + message);
                if (sender != null) {
                    sender.write(message);
                    sender.flush();
                }
            } catch (Exception e) {
                activity.showlog("send message fail (" + e.toString() + " ).");
                if (mCurrentMessage != null) {
                    mCurrentMessage = null;
                }
                e.printStackTrace();
            }
        }
    }


    private class FileHandleThread extends Thread {
        private String host;
        private int  port;
        private boolean isFileServer;
        private String uri;
        private boolean isSend;

        FileHandleThread(String host, int port, String uri, boolean server, boolean send){
            this.host = host;
            this.port = port;
            this.uri = uri;
            this.isFileServer = server;
            this.isSend = send;
        }

        @Override
        public void run() {
            super.run();

            Socket socket = null;
            ServerSocket serverSocket = null;
            for(int i = 0; i < 3; ++i){
                try {
                    if (isFileServer){
                        serverSocket = new ServerSocket(port);
                        socket = serverSocket.accept();
                    }else{
                        socket = new Socket();
                        Log.d("xyz", "Opening client socket - ");
                        socket.bind(null);
                        socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    }
                    break;
                }catch (Exception e){
                    activity.showlog("Exception: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        Thread.sleep(500L);
                    }catch (InterruptedException ei){
                        ei.printStackTrace();
                        Thread.currentThread().interrupt();
                    } catch (Exception eii){
                        eii.printStackTrace();
                    }
                }
            }

            if(isSend){
                sendFile(socket, uri.toString());
            }else{
                receiveFile(socket);
            }

            if(serverSocket != null){
                try {
                    serverSocket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (socket != null){
                try {
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        void sendFile(Socket socket, String fileUri){
            try {
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = activity.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d("xyz", e.toString());
                }
                copyFile(is, stream);
                Log.d("xyz", "Client: Data written");
            } catch (IOException e) {
                activity.showlog("IOException, " + e.getMessage());
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

        void receiveFile(Socket client){
            try {
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
                activity.showlog("File copied - " + f.getAbsolutePath());
            } catch (Exception e) {
                activity.showlog("Exception, " + e.getMessage());
            }finally {
                if (client != null) {
                    try {
                        client.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        public boolean copyFile(InputStream inputStream, OutputStream out) {
            byte buf[] = new byte[1024];
            int len;
            try {
                while (inputStream != null && (len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                activity.showlog("IOException, " + e.getMessage());
                return false;
            }
            return true;
        }
    }
}
