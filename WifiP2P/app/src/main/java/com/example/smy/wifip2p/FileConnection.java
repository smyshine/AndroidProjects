package com.example.smy.wifip2p;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by SMY on 2016/8/1.
 */
public class FileConnection {
    MainActivity activity;

    private LinkedBlockingQueue<String> mMessageQueue;

    private String mCurrentMessage;
    private MessageHandleThread mMessageHandlerThread;

    private static final int BUFFER_SIZE = 1024 * 16 * 2;
    private static final int SOCKET_TIMEOUT = 10000;
    private static final long SLEEP_TIMEOUT = 50L;

    FileConnection(MainActivity activity) {
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
        public boolean isShouldReceive = false;
        public boolean isShouleSend = false;
        private String uri;

        MessageHandleThread(String host, int port, String uri, boolean server){
            this.host = host;
            this.port = port;
            this.uri = uri;
            this.isServer = server;
        }

        @Override
        public void run() {
            super.run();
            if (initialize(isServer)){
                while (!stop) {
                    if (isShouldReceive){
                        receiveFile(port);
                        isShouldReceive = false;
                    }else if (isShouleSend){
                        sendFile(host, port, uri);
                        isShouleSend = false;
                    }else{
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
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
                    if (isServer) {
                        ServerSocket serverSocket = new ServerSocket(port);
                        socket = serverSocket.accept();
                    } else {
                        socket = new Socket();
                        socket.bind(null);
                        socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    }

                    activity.showlog("start file socket success .");

                    return true;
                } catch (Exception e) {
                    activity.showlog("Exception : " + e.getMessage());
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
                while(client == null){
                    try {
                        client = serverSocket.accept();
                    } catch (Exception e){
                        client = null;
                        e.printStackTrace();
                    }
                }
                //client = serverSocket.accept();
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



    boolean isReadyToSendFile = false;
    void setSendFileOK(){
        isReadyToSendFile = true;
    }

    void receiveFile(String host, int port, String uri, boolean isServer){
        this.isServer = isServer;

        close();

        isStart = true;

        if (mMessageHandlerThread == null || !mMessageHandlerThread.isAlive()) {
            mMessageHandlerThread = new MessageHandleThread(host, port, uri, isServer);
            mMessageHandlerThread.isShouldReceive = true;
            mMessageHandlerThread.start();
        }
    }

    void sendFile(String host, int port, String uri, boolean isServer){
        this.isServer = isServer;

        close();

        isStart = true;

        if (mMessageHandlerThread == null || !mMessageHandlerThread.isAlive()) {
            mMessageHandlerThread = new MessageHandleThread(host, port, uri, isServer);
            mMessageHandlerThread.isShouleSend = true;
            mMessageHandlerThread.start();
        }
    }

    boolean isStart = false;
    boolean isServer = false;
    void start(String host, int port, String uri, boolean isServer) {
        this.isServer = isServer;

        close();

        isStart = true;

        if (mMessageHandlerThread == null || !mMessageHandlerThread.isAlive()) {
            mMessageHandlerThread = new MessageHandleThread(host, port, uri, isServer);
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
}
