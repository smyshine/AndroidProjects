package com.langtao;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.audio.handle.AudioProcOut;
import com.encoder.util.EncG726;
import com.sinaapp.bashell.VoAACEncoder;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.xiaoyi.camera.sdk.AntsCamera;
import com.xiaoyi.camera.sdk.IRegisterCameraListener;
import com.xiaoyi.camera.sdk.P2PDevice;
import com.xiaoyi.camera.sdk.P2PMessage;
import com.xiaoyi.camera.sdk.Step;
import com.xiaoyi.camera.util.AntsUtil;
import com.xiaoyi.log.AntsLog;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import glnk.ants.GlnkLiveChannel;
import glnk.ants.GlnkLiveDataSource;
import glnk.io.GlnkCode.ResponseCode;

public class LangtaoCamera {

    private final static String TAG = "LangtaoCamera";

    private String uid;
    private String account;
    private String password;
    private String model;
    private boolean isEncrypted;
    private String decryptKey;
    private boolean isByteOrderBig;

    private int mMode = -1;
    private GlnkLiveChannel liveChannel;
    private IRegisterCameraListener listenr;
    private boolean isConnected = false;
    private boolean isInConnecting = false;
    private SendIOCtrlThread sendIOCtrlThread;
    private ThreadRecordAudio recordAudioThread;
    private SendAudioThread sendAudioThread;

    private Queue<P2PMessage> ioCtrls = new LinkedBlockingQueue<P2PMessage>();
    private Queue<RecordData> mRecordAudioQueue = new LinkedBlockingQueue<RecordData>();

    private Handler handler = new Handler(Looper.getMainLooper());


    private Object LOCK = new Object();

    public LangtaoCamera(String uid, String account, String password, String model, boolean isEncrypted, boolean isByteOrderBig) {
        this.uid = uid;
        this.account = account;
        this.password = password;
        this.model = model;
        this.isEncrypted = isEncrypted;
        this.decryptKey = password + "0";
        this.isByteOrderBig = isByteOrderBig;
    }

    public String getUid() {
        return uid;
    }

    public void setListener(IRegisterCameraListener listenr) {
        this.listenr = listenr;
    }

    private Runnable connectingTimeOutRunnable = new Runnable() {

        @Override
        public void run() {
            if (isInConnecting) {
                isInConnecting = false;
                isConnected = false;
                if (listenr != null) {
                    listenr.receiveErrorState(Step.onDisconnected, -9999);
                }
            }
        }
    };

    private void setToNotInConnectingState() {
        isInConnecting = false;
        handler.removeCallbacks(connectingTimeOutRunnable);
    }

    public void connect() {
        synchronized (LOCK) {
            if (isInConnecting) {
                return;
            }
            AntsLog.d(TAG, "startConnectWith: uid=" + uid
                    + ",account=" + account + ",password=" + password);
            if (listenr != null) {
                listenr.receiveConnectingProgress(5);
            }
            isInConnecting = true;
            handler.postDelayed(connectingTimeOutRunnable, 30 * 1000);

            ioCtrls.clear();
            startLiveChannel();
            // liveChannel = new GlnkLiveChannel(cameraCallback);
            // liveChannel.setMetaData(uid, account, password, 0, 0, 2);
            // liveChannel.startChannelVideo();
            startSendIOCtrlThread();
            // sendIOCtrlThread = new SendIOCtrlThread();
            // sendIOCtrlThread.start();
        }
    }

    public void startLiveChannel() {
        if (liveChannel != null) return;

        if (TextUtils.isEmpty(password)) {
            if (listenr != null) {
                listenr.receiveErrorState(Step.onAuthorized, AVAPIs.AV_ER_WRONG_VIEWACCorPWD);
            }
            return;
        }
        AntsLog.D("liveChannel startChannelVideo");
        liveChannel = new GlnkLiveChannel(cameraCallback);
        if (isEncrypted) {
            String nonce = AntsUtil.genNonce(15);
            String encryptPwd = AntsUtil.getPassword(nonce, password);
            liveChannel.setMetaData(uid, nonce, encryptPwd, 0, 0, 2);
        } else {
            liveChannel.setMetaData(uid, account, password, 0, 0, 2);
        }
        liveChannel.setReconnectable(false);
        liveChannel.startChannelVideo();
    }

    public void startSendIOCtrlThread() {
        if ((sendIOCtrlThread != null) && (sendIOCtrlThread.isRunning())) {
            return;
        }

        sendIOCtrlThread = new SendIOCtrlThread();
        sendIOCtrlThread.start();
    }

    public void startTalking() {
        if (liveChannel != null) {
            mRecordAudioQueue.clear();
            liveChannel.stopTalking();
            liveChannel.startTalking();
            if(P2PDevice.MODEL_V2.equals(model) || P2PDevice.MODEL_H19.equals(model) || P2PDevice.MODEL_H20.equals(model) ) {
                recordAudioThread = new ThreadRecordAudioAAC();
            } else {
                recordAudioThread = new ThreadRecordAudioG726();
            }
            recordAudioThread.start();
            sendAudioThread = new SendAudioThread();
            sendAudioThread.start();
        }
    }

    public void stopTalking() {
        if (recordAudioThread != null) {
            recordAudioThread.stopThread();
            recordAudioThread = null;
        }
        if (sendAudioThread != null) {
            sendAudioThread.stopThread();
            sendAudioThread.interrupt();
            sendAudioThread = null;
        }

        if (liveChannel != null) {
            liveChannel.stopTalking();
        }
    }

    public void sendIOCtrl(P2PMessage p2pMessage) {
        ioCtrls.add(p2pMessage);
    }

    public void sendIOCtrl(int reqId, byte[] data) {
        P2PMessage p2pMessage = new P2PMessage(reqId, data);
        sendIOCtrl(p2pMessage);
    }

    public void sendAudioDataByManu(byte[] frameInfo, byte[] data) {
        liveChannel.sendAudioDataByManu(frameInfo, data);
    }

    public boolean isInConnecting() {
        return isInConnecting;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void stopLiveChannel() {
        try {
            if (liveChannel != null) {
                liveChannel.stop();
                liveChannel.release();
                liveChannel = null;

                // sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, new byte[8]);
                // sendStopRecordVideoCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        AntsLog.D("langtao disconnect()");
        stopLiveChannel();

        if (sendIOCtrlThread != null) {
            sendIOCtrlThread.interrupt();
            sendIOCtrlThread.stopThread();
            sendIOCtrlThread = null;
        }
        if (recordAudioThread != null) {
            recordAudioThread.interrupt();
            recordAudioThread = null;
        }

        if (sendAudioThread != null) {
            sendAudioThread.interrupt();
            sendAudioThread = null;
        }
        ioCtrls.clear();
        setToNotInConnectingState();
        isConnected = false;
    }

    public void setPassword(String password) {
        this.password = password;
        this.decryptKey = password + "0";
        if (isInConnecting) {
            stopLiveChannel();
            startLiveChannel();
        }
    }

    public void sendStopPlayVideoCommand() {
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, new byte[8]);
    }

    public void sendStopRecordVideoCommand() {
        int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL;
        if (isEncrypted) {
            command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL2;
        }
        sendIOCtrl(command, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(0,
                AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0, new byte[8], (byte) 0, isByteOrderBig));
    }

    public void sendStartPlayVideoCommand() {
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START, new byte[8]);
    }

    private final int PASSWORD_ERROR = -20;
    private final int LOGIN_FAILED = -10;

    /**
     * 浪涛P2P回调
     */
    private GlnkLiveDataSource cameraCallback = new GlnkLiveDataSource() {


        public void onConnected(int mode, String ip, int port) {
            AntsLog.d(TAG, "onConnected");
            mMode = mode;
            if (listenr != null) {
                listenr.receiveConnectingProgress(25);
            }
        }


        public void onAuthorized(int result) {
            AntsLog.d(TAG, "onAuthorized result=" + result);
            if (result == ResponseCode.RESPONSECODE_SUCC) {
                isConnected = true;
                if (listenr != null) {
                    listenr.receiveConnectedSuccess();
                }
            } else {
                isConnected = false;
            }
        }


        @Override
        public void onAudioDataByManu(byte[] data, byte[] frameInfo) {
            setToNotInConnectingState();
            AVFrame avFrame = new AVFrame(frameInfo, data, isByteOrderBig);
            if (listenr != null) {
                listenr.receiveAudioFrameData(avFrame);
            }
        }

        public void onVideoDataByManu(byte[] data, byte[] frameInfo) {
            setToNotInConnectingState();

            AVFrame avFrame = new AVFrame(frameInfo, data, isByteOrderBig);

            if (isEncrypted && avFrame.isIFrame()) {
                AntsUtil.decryptIframe(avFrame, decryptKey);
            }

            // for log print purpose
            int cameraUseCount = 0;
            if(listenr != null && listenr instanceof AntsCamera){
                cameraUseCount = ((AntsCamera) listenr).getUseCount();
            }

            AntsLog.d(TAG, "langtao receive video "
                    + avFrame.toFrameString()
                    + ", userCount:" + avFrame.useCount + "-cameraUseCount:" + cameraUseCount
                    + (avFrame.isIFrame() ? (", isEncrypted:" + isEncrypted + "-" + decryptKey) : ""));

            if (listenr != null) {
                listenr.receiveVideoFrameData(avFrame);
            }
        }


        public void onDataRate(int bytesPersecond) {
            // if (listenr != null) {
            // listenr.receiveDataRate(bytesPersecond);
            // }
        }


        @Override
        protected void onAVStreamFormat(byte[] data) {
            super.onAVStreamFormat(data);
        }

        public void onReConnecting() {
            AntsLog.d(TAG, "onReConnecting");
            isConnected = false;
            setToNotInConnectingState();
            // 浪涛自动重连机制，不需要抛到界面
            // if (listenr != null) {
            // listenr.receiveErrorState(1);
            // }
        }


        @Override
        public void onDisconnected(int errcode) {
            AntsLog.d(TAG, "onDisconnected errcode=" + errcode);
            isConnected = false;
            stopLiveChannel();
            // 认证密码错误或认证失败的处理
            if (errcode == PASSWORD_ERROR) {
                if (listenr != null) {
                    listenr.receiveErrorState(Step.onDisconnected, AVAPIs.AV_ER_WRONG_VIEWACCorPWD);
                }
                return;
            }

            setToNotInConnectingState();

            if (listenr != null) {
                listenr.receiveErrorState(Step.onDisconnected, errcode);
            }
        }

        public void onIOCtrlByManu(int type, byte[] data) {
            AntsLog.i(TAG,
                    (new StringBuilder("onIOCtrlByManu(")).append("0x")
                            .append(Integer.toHexString(type)).append(", ")
                            .append(LangtaoCamera.getHex(data)).append(")").toString());
            if (listenr != null) {
                listenr.receiveIOCtrlData(type, data);
            }
        }

        @Override
        public void onModeChanged(int mode, String ip, int port) {
            mMode = mode;
        }
    };

    private static Object SendIOCtrlWaitObject = new Object();

    private class SendIOCtrlThread extends Thread {

        private boolean isRunning = true;

        @Override
        public void run() {
            while (isRunning) {
                if (!isConnected) {
                    try {
                        Thread.sleep(100);
                        continue;
                    } catch (InterruptedException e) {
                    }
                }

                P2PMessage p2pMessage = ioCtrls.peek();
                if (p2pMessage != null) {
                    if (liveChannel == null) {
                        break;
                    }
                    int ret = liveChannel.sendIOCtrlByManu(p2pMessage.reqId, p2pMessage.data);
                    AntsLog.d(TAG,
                            "sendIOCtrlByManu: ret= " + ret + ", type="
                                    + Integer.toHexString(p2pMessage.reqId) + ", data="
                                    + getHex(p2pMessage.data));

                    if (ret == -1) {
                        if (listenr != null) {
                            p2pMessage.error = ret;
                            listenr.receiveSendP2PMessageError(p2pMessage);
                            listenr.receiveErrorState(Step.sendIOCtrlByManu, ret);
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        ioCtrls.remove();
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void stopThread() {
            isRunning = false;
        }
    }

    private Object SEND_AUDIO_LOCK = new Object();

    private boolean m_bOnlyAliveThreadRecordAudio = false;

    private class ThreadRecordAudio extends Thread {

        boolean m_bIsRunning = false;

        public ThreadRecordAudio() {
        }

        public void stopThread() {
            m_bIsRunning = false;
        }
    }

    private class ThreadRecordAudioG726 extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 8000;

        private long lTimestampKey = 1;

        public ThreadRecordAudioG726(){
            super();
        }

        public void run() {
            this.m_bIsRunning = true;
            EncG726.g726_enc_state_create((byte) 0, (byte) 2);
            int bufferSize = 640;
            long[] encodePCMLength = new long[1];
            // int bufferSize = 8000;
            byte[] tempBuffer = new byte[bufferSize];
            byte[] outProcessBuffer = new byte[bufferSize * 2];

            try {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 8192);
                mAudioRecord.startRecording();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                lTimestampKey = 1;
                return;
            }

            // mAVChannel.mRecordAudioQueue.clear();
            while (this.m_bIsRunning && isConnected) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                if (bufferRead <= 0) {
                    break;
                }
                int processLenth = AudioProcOut
                        .Process(tempBuffer, bufferRead, outProcessBuffer, 2);
                byte[] encodePCM = new byte[320];
                if (bufferRead > 0) {
                    AntsLog.d("record", "bufferRead=" + bufferRead);
                    // localPCM =
                    // AACAudioEncoder.getInstance().encode(tempBuffer);
                    EncG726.g726_encode(outProcessBuffer, (long) processLenth, encodePCM,
                            encodePCMLength);
                    AntsLog.d("record", "endcodePCMLength=" + encodePCMLength[0]);
                    // localPCM = tempBuffer;
                }

                if (encodePCM != null) {
                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    byte[] sendAudioInf = AVIOCTRLDEFs.SFrameInfo.parseContent((short) 138,
                            (byte) 2, (byte) 0, (byte) 0, lTimeStamp, isByteOrderBig);
                    RecordData data = new RecordData();
                    data.info = sendAudioInf;
                    data.data = encodePCM;
                    data.length = encodePCMLength[0];
                    data.type = 1;
                    AntsLog.d("record", "record data");
                    AntsLog.d(TAG,
                            (new StringBuilder("Langtao-sendAudioInf.info:"))
                                    .append(LangtaoCamera.getHex(data.info)).append(")").toString());
                    AntsLog.d(TAG,
                            (new StringBuilder("Langtao-sendAudioInf.info-time:"))
                                    .append(lTimeStamp).append(")").toString());
                    synchronized (SEND_AUDIO_LOCK) {
                        mRecordAudioQueue.add(data);
                        SEND_AUDIO_LOCK.notifyAll();
                    }
                }
            }

            EncG726.g726_enc_state_destroy();

            if (mAudioRecord != null) {
                try {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                } catch (Exception e) {

                }
            }

            lTimestampKey = 1;
        }

    }

    private class ThreadRecordAudioAAC extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 16000;
        private int bitRate = 32000;

        private long lTimestampKey = 1;

        public ThreadRecordAudioAAC() {
            super();
        }

        public void run() {
            if (m_bOnlyAliveThreadRecordAudio) {
                AntsLog.i(TAG, "=== got multi m_bOnlyAliveThreadRecordAudio="
                        + m_bOnlyAliveThreadRecordAudio);
                return;
            }

            m_bOnlyAliveThreadRecordAudio = true;

            this.m_bIsRunning = true;

            int bufferSize = 2048;

            byte[] tempBuffer = new byte[bufferSize];

            VoAACEncoder vo = new VoAACEncoder();
            vo.Init(sampleRate, bitRate, (short)1, (short)1);

            int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(min < bufferSize){
                min = bufferSize;
            }

            try{
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
                mAudioRecord.startRecording();
            }catch (Exception e){
                e.printStackTrace();
            }

            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                lTimestampKey = 1;
                return;
            }

            while (this.m_bIsRunning && isConnected) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                if (bufferRead <= 0) {
                    break;
                }

                byte[] encodePCM = vo.Enc(tempBuffer);


                if (encodePCM != null) {
                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    byte[] sendAudioInf = AVIOCTRLDEFs.SFrameInfo.parseContent((short) 138,
                            (byte) 2, (byte) 0, (byte) 0, lTimeStamp, isByteOrderBig);
                    RecordData data = new RecordData();
                    data.info = sendAudioInf;
                    data.data = encodePCM;
                    data.length = encodePCM[0];
                    data.type = 1;
                    AntsLog.d("record", "record data");
                    AntsLog.d(TAG,
                            (new StringBuilder("Langtao-sendAudioInf.info:"))
                                    .append(LangtaoCamera.getHex(data.info)).append(")").toString());
                    AntsLog.d(TAG,
                            (new StringBuilder("Langtao-sendAudioInf.info-time:"))
                                    .append(lTimeStamp).append(")").toString());
                    synchronized (SEND_AUDIO_LOCK) {
                        mRecordAudioQueue.add(data);
                        SEND_AUDIO_LOCK.notifyAll();
                    }
                }
            }

            vo.Uninit();

            if (mAudioRecord != null) {
                try {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                } catch (Exception e) {

                }
            }

            lTimestampKey = 1;

            m_bOnlyAliveThreadRecordAudio = false;

        }

    }


    private class SendAudioThread extends Thread {

        private boolean isRunning = true;

        @Override
        public void run() {
            while (isConnected && isRunning) {

                if (mRecordAudioQueue.size() > 0) {
                    RecordData recordData = null;
                    synchronized (SEND_AUDIO_LOCK) {
                        recordData = mRecordAudioQueue.poll();
                    }
                    if (recordData == null) continue;
                    liveChannel.sendAudioDataByManu(recordData.info, recordData.data);

                    AntsLog.d(TAG, (new StringBuilder("Langtao-avSendAudioData-recordData.info:"))
                            .append(LangtaoCamera.getHex(recordData.info)).append(")").toString());
                } else {
                    synchronized (SEND_AUDIO_LOCK) {
                        try {
                            SEND_AUDIO_LOCK.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }

        public void stopThread() {
            isRunning = false;
        }
    }



    private class RecordData {

        byte[] info;

        byte[] data;

        long length;

        byte type;

    }

    static String getHex(byte[] data) {
        if (data == null) return null;
        StringBuilder localStringBuilder = new StringBuilder(2 * data.length);
        for (int k = 0; k < data.length; k++) {
            int m = data[k];
            localStringBuilder.append("0123456789ABCDEF".charAt((m & 0xF0) >> 4))
                    .append("0123456789ABCDEF".charAt(m & 0xF)).append(" ");

        }
        return localStringBuilder.toString();
    }

    public int getMode() {
        return mMode;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

}
