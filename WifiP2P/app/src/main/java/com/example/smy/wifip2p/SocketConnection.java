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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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

    SocketConnection(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * 消息处理线程
     *
     * @author Administrator
     *
     */
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
                        if (!TextUtils.isEmpty(msg)){
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 初始化消息处理线程
         *
         * @throws UnknownHostException
         * @throws IOException
         * @throws SocketException
         */
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
                        e1.printStackTrace();
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

        /**
         * 接受消息
         */
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
                    TimeUnit.MILLISECONDS.sleep(SLEEP_TIMEOUT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 发送消息
         */
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

    /**
     * 添加消息到消息队列
     *
     * @param message
     */
    private void addMessage(String message) {
        //activity.showlog("message queue add " + message );
        mMessageQueue.add(message);
    }

    void clearMessageQueue() {
        if (mMessageQueue != null) {
            mMessageQueue.clear();
        }
    }

    void sendMessage(String message) {
        if (mMessageQueue != null) {
            addMessage(message);
        }
    }

    boolean isReadyToSendFile = false;
    void setSendFileOK(){
        isReadyToSendFile = true;
    }

    void receiveFile(){
        FileHandleThread thread = new FileHandleThread(null, 8787, null, true);
        thread.start();
    }

    void sendFile(String host, int port, String uri){
        FileHandleThread thread = new FileHandleThread(host, port, uri, false);
        thread.start();
    }

    boolean isStart = false;
    boolean isServer = false;
    void start(String host, int port, boolean isServer) {
        this.isServer = isServer;

        close();

        if (mMessageQueue == null) {
            mMessageQueue = new LinkedBlockingQueue<String>();
        }

        isStart = true;

        if (mMessageHandlerThread == null || !mMessageHandlerThread.isAlive()) {
            mMessageHandlerThread = new MessageHandleThread(host,port, isServer);
            mMessageHandlerThread.start();
        }
    }

    private void close() {
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
        isStart = false;
    }

    public void stop() {
        close();
    }

    public boolean isStart(){
        return isStart;
    }


    private class FileHandleThread extends Thread {
        private String host;
        private int  port;
        private boolean isFileServer;
        private String uri;

        FileHandleThread(String host, int port, String uri, boolean server){
            this.host = host;
            this.port = port;
            this.uri = uri;
            this.isFileServer = server;
        }

        @Override
        public void run() {
            super.run();

            if(isFileServer){
                receiveFile(port);
            }else{
                sendFile(host, port, uri.toString());
            }

        }

        void sendFile(String host, int port, String fileUri){
            Socket socket = new Socket();
            try {
                Log.d("xyz", "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d("xyz", "Client socket - " + socket.isConnected());

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

        void receiveFile(int port){
            ServerSocket serverSocket = null;
            Socket client = null;
            try {
                activity.showlog("receive file start.");
                serverSocket = new ServerSocket(port);
                /*while(client == null){
                    try {
                        client = serverSocket.accept();
                    } catch (Exception e){
                        client = null;
                        e.printStackTrace();
                    }
                }*/
                client = serverSocket.accept();
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
            } catch (IOException e) {
                activity.showlog("IOException, " + e.getMessage());
            }finally {
                if (client != null) {
                    try {
                        client.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(serverSocket != null){
                    try {
                        serverSocket.close();
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
