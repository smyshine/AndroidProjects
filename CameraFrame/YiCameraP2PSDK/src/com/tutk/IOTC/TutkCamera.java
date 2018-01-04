package com.tutk.IOTC;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.text.TextUtils;

import com.audio.handle.AudioProcOut;
import com.encoder.util.EncG726;
import com.sinaapp.bashell.VoAACEncoder;
import com.xiaoyi.camera.sdk.AntsCamera;
import com.xiaoyi.camera.sdk.AudioUtil;
import com.xiaoyi.camera.sdk.IRegisterCameraListener;
import com.xiaoyi.camera.sdk.P2PDevice;
import com.xiaoyi.camera.sdk.P2PMessage;
import com.xiaoyi.camera.sdk.P2PParams;
import com.xiaoyi.camera.sdk.Step;
import com.xiaoyi.camera.util.AntsUtil;
import com.xiaoyi.log.AntsLog;
import com.xiaoyi.p2pservertest.audio.ByteRingBuffer;
import com.xiaoyi.p2pservertest.audio.MobileAEC;
import com.xiaoyi.p2pservertest.audio.MobileAGC;
import com.xiaoyi.p2pservertest.audio.MobileNS;
import com.xiaoyi.p2pservertest.audio.MobileVAD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TutkCamera {
    private static final String TAG = "TutkCamera";

    public static final int DEFAULT_AV_CHANNEL = 0;
    private static int mDefaultMaxCameraLimit = 2;

    private static int IOTCRet = -1;
    private static int AVRet = -1;
    private volatile int[] bResend = new int[1];
    private AudioTrack mAudioTrack = null;
    private String mDevUID = "";
    private IRegisterCameraListener mIOTCListener;
    private boolean mInitAudio = false;
    public static int DEFAULT_SID = -10000;
    private volatile int mSID = DEFAULT_SID;
    private volatile int mSessionMode = -1;
    private ThreadConnectDev mThreadConnectDev = null;
    // private ThreadSendAudio mThreadSendAudio = null;
    private final Object mWaitObjectForConnected = new Object();
    private volatile int nDispFrmPreSec;
    private volatile int nRecvFrmPreSec;

    private boolean mInConnecting;

    private AVChannel avChannel;

    private String mUID;
    private String mAcc;
    private String mPwd;
    private String mModel;
    private boolean isEncrypted;
    private boolean isByteOrderBig;


    private MobileAEC mMobileAEC;
    private int talkMode;


    public TutkCamera(String mUID, String mAcc, String mPwd, boolean isEncrypted, String mModel, boolean isByteOrderBig) {
        this.mUID = mUID;
        this.mAcc = mAcc;
        this.mPwd = mPwd;
        this.mModel = mModel;
        this.isEncrypted = isEncrypted;
        this.isByteOrderBig = isByteOrderBig;
        this.mMobileAEC = MobileAEC.getInstance();
        this.talkMode = AntsCamera.SINGLE_MODE;
    }

    public String getUID() {
        return mUID;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    /**
     * 区域网搜索设备
     *
     * @return
     */
    public static st_LanSearchInfo[] SearchLAN() {
        try {
            st_LanSearchInfo[] arrayOfst_LanSearchInfo = IOTCAPIs
                    .IOTC_Lan_Search(new int[32], 3000);
            return arrayOfst_LanSearchInfo;
        } finally {
        }
    }

    static String getHex(byte[] bytes, int length) {
        if (bytes == null) return null;
        StringBuilder stringBuilder = new StringBuilder(2 * bytes.length);
        for (int k = 0; k < length; k++) {
            int m = bytes[k];
            stringBuilder.append("0123456789ABCDEF".charAt((m & 0xF0) >> 4))
                    .append("0123456789ABCDEF".charAt(m & 0xF)).append(" ");
        }
        return stringBuilder.toString();
    }

    public static void IOTCInit() {
        if (IOTCRet < 0) {
            IOTCRet = IOTCAPIs.IOTC_Initialize2(0);

            // IOTCRet = IOTCAPIs.IOTC_Initialize(0, "61.188.37.216",
            // "122.226.84.253", "50.19.254.134", "122.248.234.207");

            // IOTCRet = IOTCAPIs.IOTC_Initialize(0, "61.188.37.216",
            // "61.188.37.216", "61.188.37.216", "61.188.37.216");
            AntsLog.i(TAG, "IOTC_Initialize2() returns " + IOTCRet);
        }
    }

    private static int initCount = 0;

    public static void init() {
        if(initCount == 0){
            IOTCInit();
            if (IOTCRet >= 0) {
                AVRet = AVAPIs.avInitialize(16 * mDefaultMaxCameraLimit);
                AntsLog.i(TAG, "avInitialize() returns   " + AVRet);
            }
        }
        initCount++;
    }

    public static void setMaxCameraLimit(int paramInt) {
        mDefaultMaxCameraLimit = paramInt;
    }

    public static void uninit() {
        if(initCount == 1){
            if (IOTCRet >= 0) {
                if (AVRet >= 0) {
                    int m = AVAPIs.avDeInitialize();
                    AVRet = -1;
                    AntsLog.i(TAG, "avDeInitialize() returns " + m);
                }
                int j = IOTCAPIs.IOTC_DeInitialize();
                IOTCRet = -1;
                AntsLog.i(TAG, "IOTC_DeInitialize() returns " + j);
            }
        }
        initCount--;
        if(initCount < 0){
            initCount = 0;
        }
    }

    public void disconnect() {
        AntsLog.d(TAG, "disconnect");
        stopChannel();
        stopConnect();
        System.gc();
    }

    public void stopChannel() {
        if (avChannel != null) {
            stopShow();
            stopListening();
            stopSpeaking();
            stop();
            avChannel = null;
        }
    }

    public void connect(String uid) {
        this.mDevUID = uid;
        if (!mInConnecting && this.mThreadConnectDev == null) {
            this.mThreadConnectDev = new ThreadConnectDev();
            this.mThreadConnectDev.start();
        }
    }

    public synchronized void stopConnect() {
        AntsLog.i(TAG, "stopConnect");
        if (this.mThreadConnectDev != null) {
            this.mThreadConnectDev.stopThread();
            this.mThreadConnectDev.interrupt();
            this.mThreadConnectDev = null;
            this.mSessionMode = -1;
        }
        if (mSID >= 0) {
            IOTCAPIs.IOTC_Session_Close(mSID);
            AntsLog.i(TAG, "IOTC_Session_Close(nSID = " + mSID + ")");
        }
        mSID = DEFAULT_SID;
    }

    public long getChannelServiceType(int paramInt) {
        if (avChannel != null) {
            return avChannel.getServiceType();
        }
        return 0L;
    }

    public int getDispFrmPreSec() {
        return this.nDispFrmPreSec;
    }

    public int getSID() {
        return this.mSID;
    }

    public int getRecvFrmPreSec() {
        return this.nRecvFrmPreSec;
    }

    public int getSessionMode() {
        return this.mSessionMode;
    }

    public int getbResend() {
        return this.bResend[0];
    }

    public boolean isChannelConnected() {
        boolean result = false;
        if (this.mSID >= 0 && avChannel != null && avChannel.getAVIndex() >= 0) {
            if (avChannel.threadSendIOCtrl != null && avChannel.threadSendIOCtrl.bIsRunning) {
                result = true;
            }
        }
        return result;
    }

    public boolean isAvClientStartError() {
        boolean started = false;
        if (avChannel != null) {
            started = avChannel.isAvClientStartError();
        }
        return started;
    }

    public boolean isSessionConnected() {
        return this.mSID >= 0;
    }

    public void registerIOTCListener(// 注册TIOC监听
                                     IRegisterCameraListener iOTCListener) {
        AntsLog.i(TAG, "register IOTC listener");
        mIOTCListener = iOTCListener;
    }

    public void sendIOCtrl(P2PMessage p2pMessage) {
        if (avChannel != null) {
            avChannel.IOCtrlQueue.add(p2pMessage);
        }
    }

    public void sendIOCtrl(int reqId, byte[] data) {
        P2PMessage p2pMessage = new P2PMessage(reqId, data);
        sendIOCtrl(p2pMessage);

    }

    public void start() {// 0,uuid,pwd
        try {
            if (avChannel == null) {
                avChannel = new AVChannel(0);
            }

            if (avChannel.threadStartDev == null) {
                avChannel.inStarting = true;
                avChannel.threadStartDev = new ThreadStartDev(avChannel);
                avChannel.threadStartDev.start();
            }

            if (avChannel.threadRecvIOCtrl == null) {
                avChannel.threadRecvIOCtrl = new ThreadRecvIOCtrl(avChannel);
                avChannel.threadRecvIOCtrl.start();
            }

            if (avChannel.threadSendIOCtrl == null) {
                avChannel.threadSendIOCtrl = new ThreadSendIOCtrl(avChannel);
                avChannel.threadSendIOCtrl.start();
            }
            startSendReport();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isInStarting() {
        if (avChannel != null) {
            return avChannel.inStarting;
        }
        return false;
    }

    public void startListening() {
        // 发送音频开始指令
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, new byte[8]);
        if (avChannel == null) {
            return;
        }
        avChannel.AudioFrameQueue.removeAll();
        if (avChannel.threadRecvAudio == null) {
            avChannel.threadRecvAudio = new ThreadRecvAudio(avChannel);
            avChannel.threadRecvAudio.setPriority(Thread.MAX_PRIORITY);
            avChannel.threadRecvAudio.start();
        }

    }

    public void stopListening() {
        if (avChannel == null) {
            return;
        }

        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, new byte[8]);

        if (avChannel.threadRecvAudio != null) {
            avChannel.threadRecvAudio.stopThread();
            avChannel.threadRecvAudio.interrupt();
            avChannel.threadRecvAudio = null;
        }

    }

    public void startShow() {
        if (avChannel == null) {
            return;
        }

        avChannel.VideoFrameQueue.removeAll();
        if (avChannel.threadStartDev == null) {
            avChannel.mChannel = 0;
            if (avChannel.threadStartDev == null) {
                avChannel.threadStartDev = new ThreadStartDev(avChannel);
                avChannel.threadStartDev.start();
            } else {
                AntsLog.i(TAG, "avChannel.threadStartDev is null");
            }
        }

        if (avChannel.threadRecvVideo == null) {
            AntsLog.i("TAG", "#############ThreadRecvVideo2 start#########" + this.mDevUID);
            avChannel.threadRecvVideo = new ThreadRecvVideo(avChannel);
            avChannel.threadRecvVideo.setPriority(Thread.MIN_PRIORITY);
            avChannel.threadRecvVideo.start();
        }

    }

    public void stop() {
        AntsLog.i(TAG, "cameraTUTK-stop:" + TutkCamera.this.mSID + ", " + 0);
        if (avChannel == null) {
            return;
        }

        try {
            // TutkCamera.this.sendIOCtrl(0,
            // AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTOP,
            // AVIOCTRLDEFs.SMsgAVIoctrlAVStream.parseContent(4));

            if (avChannel.threadRecvIOCtrl != null) {
                avChannel.threadRecvIOCtrl.stopThread();
                avChannel.threadRecvIOCtrl.interrupt();
                avChannel.threadRecvIOCtrl = null;

            }
            if (avChannel.threadSendIOCtrl != null) {
                avChannel.threadSendIOCtrl.stopThread();
                avChannel.threadSendIOCtrl.interrupt();
                avChannel.threadSendIOCtrl = null;
            }

            // 关闭反向通道，及停止数据上报
            stopSendReport();

            if (avChannel.threadStartDev != null) {
                avChannel.threadStartDev.stopThread();
                avChannel.threadStartDev.interrupt();
                avChannel.threadStartDev = null;
            }

            if (avChannel.getAVIndex() >= 0) {
                AVAPIs.avClientStop(avChannel.getAVIndex());
                AntsLog.d(TAG, "AVAPIs.avClientStop avIndex=" + avChannel.getAVIndex() + ",uid="
                        + mDevUID);
            } else if (TutkCamera.this.mSID >= 0) {
                AVAPIs.avClientExit(TutkCamera.this.mSID, avChannel.getChannel());
                AntsLog.d(TAG, "AVAPIs.avClientExit");
            }

            avChannel.IOCtrlQueue.clear();

        } catch (Exception e) {
        }
    }

    public void stopShow() {
        AntsLog.i(TAG, "stop current channel");

        if (avChannel == null) {
            return;
        }

        if (avChannel.threadRecvAudio != null) {
            avChannel.threadRecvAudio.stopThread();
            avChannel.threadRecvAudio.interrupt();
            avChannel.threadRecvAudio = null;
        }

        if (avChannel.threadRecvVideo != null) {
            avChannel.threadRecvVideo.stopThread();
            avChannel.threadRecvVideo.interrupt();
            avChannel.threadRecvVideo = null;
        }

        if (avChannel.threadRecordAudio != null) {
            avChannel.threadRecordAudio.stopThread();
            avChannel.threadRecordAudio.interrupt();
            avChannel.threadRecordAudio = null;
        }
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, new byte[8]);

        sendStopRecordVideoCommand();
    }

    public void setPassword(String password) {
        mPwd = password;
        if (avChannel != null) {
            avChannel.updateEncryptedKey();
            if (avChannel.threadStartDev != null) {
                avChannel.threadStartDev.retry();
            }
        }
    }

    private void stopAvServ(AVChannel avChannel) {
        if (avChannel.avIndexForSendAudio >= 0) {
            AVAPIs.avServStop(avChannel.avIndexForSendAudio);
        } else if (TutkCamera.this.mSID >= 0) {
            AVAPIs.avServExit(TutkCamera.this.mSID, 4);
        }
    }

    public void unregisterIOTCListener(IRegisterCameraListener registerIOTCListener) {
        mIOTCListener = null;
    }

    private class RecordData {

        byte[] info;

        byte[] data;

        long length;

        byte type;

    }

    private class AVChannel {
        public boolean inStarting;
        public int avIndexForSendAudio = -1;
        public AVFrameQueue AudioFrameQueue;
        private Queue<P2PMessage> IOCtrlQueue;
        public LinkedList<RecordData> mRecordAudioQueue;
        public AVFrameQueue VideoFrameQueue;
        private volatile int mAVIndex = -1;
        private volatile int mChannel = -1;
        private long mServiceType = -1L;
        public TutkCamera.ThreadRecvAudio threadRecvAudio = null;
        public TutkCamera.ThreadRecvIOCtrl threadRecvIOCtrl = null;
        public TutkCamera.ThreadRecvVideo threadRecvVideo = null;
        public TutkCamera.ThreadSendIOCtrl threadSendIOCtrl = null;
        public TutkCamera.ThreadStartDev threadStartDev = null;
        public TutkCamera.ThreadRecordAudio threadRecordAudio;
        public TutkCamera.ThreadSendAudio threadSendAudio;

        public int kbps;

        public int avClientStarted = 0; // 0 未连接，1 已连接, -1连接失败
        private String encryptedKey;

        public AVChannel(int channel) {
            this.mChannel = channel;
            this.mServiceType = -1L;
            this.IOCtrlQueue = new LinkedBlockingQueue<P2PMessage>();
            this.VideoFrameQueue = new AVFrameQueue();
            this.AudioFrameQueue = new AVFrameQueue();
            this.mRecordAudioQueue = new LinkedList<TutkCamera.RecordData>();
            this.encryptedKey = mPwd + "0";
        }

        public int getAVIndex() {
            return this.mAVIndex;
        }

        public boolean isAvClientStartError() {
            return avClientStarted < 0;
        }

        public int getChannel() {
            return this.mChannel;
        }

        public long getServiceType() {
            return mServiceType;
        }

        public void updateEncryptedKey() {
            this.encryptedKey = mPwd + "0";
        }

        public void setAVIndex(int avIndex) {
            this.mAVIndex = avIndex;
        }

        public void setServiceType(long type) {
            this.mServiceType = type;
        }

        public String getEncryptedKey() {
            return encryptedKey;
        }
    }

    public boolean isInConnecting() {
        return mInConnecting;
    }

    public boolean isConnectAlive() {
        return checkOnlineState();
    }

    public boolean checkOnlineState() {
        if (TutkCamera.this.mSID < 0) {
            return false;
        }

        St_SInfo localSt_SInfo = new St_SInfo();
        int ret = IOTCAPIs.IOTC_Session_Check(TutkCamera.this.mSID, localSt_SInfo);
        if (ret >= 0) {
            return true;
        }

        return false;
    }

    private class ThreadConnectDev extends Thread {
        private int nGet_SID = -1;

        public void run() {
            AntsLog.i(TAG, "########ThreadConnectDev START#####");
            mInConnecting = true;

            long start = new Date().getTime();

            AntsLog.i(TAG, "start IOTC_Get_SessionID");

            nGet_SID = IOTCAPIs.IOTC_Get_SessionID();

            AntsLog.i(TAG, "IOTC_Get_SessionID SID = " + nGet_SID);
            if (nGet_SID < 0) {
                if (mIOTCListener != null) {
                    mIOTCListener.receiveSessionInfo(Step.IOTC_Get_SessionID, nGet_SID);
                }
                mInConnecting = false;
                disconnect();
                return;
            }

            if (nGet_SID >= 0) {
                setConnectingProgress(AntsCamera.CONNECTION_STATE_GET_SID);
                AntsLog.i(TAG, "start call IOTCAPIs.IOTC_Connect_ByUID_Parallel");
                //IOTCAPIs.IOTC_Setup_LANConnection_Timeout(0);
                mSID = IOTCAPIs.IOTC_Connect_ByUID_Parallel(mDevUID, nGet_SID);
                AntsLog.D("IOTC_Connect_ByUID_Parallel mSID = " + mSID + ";mDevUID="
                        + mDevUID);

                nGet_SID = -1;
                long end = new Date().getTime();
                AntsLog.d(TAG, "IOTC_Connect_ByUID_Parallel use: " + ((end - start) / 1000.0)
                        + " sec");

                if (mIOTCListener != null) {
                    mIOTCListener.receiveSessionInfo(Step.IOTC_Connect_ByUID_Parallel, mSID);
                }

                if (mSID >= 0) {
                    setConnectingProgress(AntsCamera.CONNECTION_STATE_CONNECT_BY_UID_SUCCESS);
                } else {
                    mInConnecting = false;
                    disconnect();
                }

            }
            // mSID = IOTCAPIs.IOTC_Connect_ByUID(mDevUID);
        }

        public void stopThread() {
            mInConnecting = false;
            AntsLog.i(TAG, "start call ThreadConnectDev stopThread");
            if ((nGet_SID >= 0) && (mSID == DEFAULT_SID)) {
                AntsLog.i(TAG, "start call IOTCAPIs.IOTC_Connect_Stop_BySID");

                IOTCAPIs.IOTC_Connect_Stop_BySID(nGet_SID);

                AntsLog.i(TAG, "end call IOTCAPIs.IOTC_Connect_Stop_BySID");
            }
        }
    }

    private void setConnectingProgress(int state) {
        if (mIOTCListener != null) {
            mIOTCListener.receiveConnectingProgress(state);
        }
    }

    private void audioDev_stop(int paramInt) {
        try {
            if (this.mInitAudio) {
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.stop();
                    this.mAudioTrack.release();
                    this.mAudioTrack = null;
                }
                // DecG726.g726_dec_state_destroy();
                mInitAudio = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ThreadRecvAudio extends Thread {
        static final int AUDIO_BUF_SIZE = 1024;
        static final int FRAME_INFO_SIZE = 24;
        private boolean bIsRunning = false;
        private AVChannel mAVChannel;

        public ThreadRecvAudio(TutkCamera.AVChannel channel) {
            this.mAVChannel = channel;
        }

        @Override
        public void run() {
            bIsRunning = true;
            byte[] frameInfo = new byte[FRAME_INFO_SIZE];
            byte[] audioArray = new byte[AUDIO_BUF_SIZE];

            // AVAPIs.avClientCleanAudioBuf(this.mAVChannel.getAVIndex());
            // int ret = 0;
            AntsLog.i(TAG, "===ThreadRecvAudio start===");
            while (bIsRunning) {
                if (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                    synchronized (mWaitObjectForConnected) {
                        try {
                            mWaitObjectForConnected.wait(100L);
                        } catch (InterruptedException e) {
                        }
                    }
                    continue;
                }
                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                    AVAPIs.avClientCleanAudioBuf(this.mAVChannel.getAVIndex());
                    mAVChannel.AudioFrameQueue.removeAll();
                    break;
                }
            }

            while (true) {
                if (!bIsRunning) {
                    audioDev_stop(mAVChannel.getChannel());
                    AntsLog.i(TAG, "===ThreadRecvAudio exit===");
                    return;
                }

                int[] frameNumber = new int[1];
                int nReadSize = AVAPIs.avRecvAudioData(mAVChannel.getAVIndex(), audioArray,
                        AUDIO_BUF_SIZE, frameInfo, FRAME_INFO_SIZE, frameNumber);

                if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {
                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException e) {
                    }

                } else if (nReadSize == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                    AntsLog.i("audio", "AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE");
                    // break;
                } else if (nReadSize == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                    AntsLog.i("audio", "AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT");
                    break;
                } else if (nReadSize == AVAPIs.AV_ER_INVALID_SID) {
                    // break;
                } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                    AntsLog.i("audio", "AVAPIs.AV_ER_LOSED_THIS_FRAME");
                    continue;
                } else if (nReadSize == AVAPIs.AV_ER_INCOMPLETE_FRAME) {
                    AntsLog.i("audio", "AVAPIs.AV_ER_INCOMPLETE_FRAME");
                    continue;
                } else if (nReadSize > 0) {
                    byte[] frmdata = new byte[nReadSize];
                    System.arraycopy(audioArray, 0, frmdata, 0, nReadSize);
                    AVFrame avFrame = new AVFrame(frameNumber[0], (byte) 0, frameInfo, frmdata,
                            nReadSize, isByteOrderBig);
                    // Log.d("AUDIO", "rev:" + avFrame.getOldfrmNo());

                    if (mIOTCListener != null) {
                        mIOTCListener.receiveAudioFrameData(avFrame);
                    }
                }
            }

        }

        public void stopThread() {
            bIsRunning = false;
        }

    }

    private class ThreadRecvIOCtrl extends Thread {
        private final int TIME_OUT = 0;
        private boolean bIsRunning = false;
        private TutkCamera.AVChannel mAVChannel;

        public ThreadRecvIOCtrl(TutkCamera.AVChannel arg2) {
            this.mAVChannel = arg2;
        }

        public void run() {
            this.bIsRunning = true;
            AntsLog.i(TAG, "===ThreadRecvIOCtrl start===");

            while (true) {
                if (!this.bIsRunning) {
                    AntsLog.i(TAG, "===ThreadRecvIOCtrl exit===");
                    break;
                }
                if ((TutkCamera.this.mSID >= 0) && (this.mAVChannel.getAVIndex() >= 0)) {
                    int[] ioType = new int[1];
                    byte[] recData = new byte[1024];
                    int ret = AVAPIs.avRecvIOCtrl(this.mAVChannel.getAVIndex(), ioType, recData,
                            recData.length, 0);
                    // arrayofint:IO控制类型,
                    // arrayOfByte1:接受数据
                    // i:返回的接受数据的长度
                    if (ret >= 0) {
                        byte[] data = new byte[ret];
                        System.arraycopy(recData, 0, data, 0, ret);
                        AntsLog.i(TAG,
                                (new StringBuilder("avRecvIOCtrl("))
                                        .append(mAVChannel.getAVIndex()).append(", 0x")
                                        .append(Integer.toHexString(ioType[0])).append(", ")
                                        .append(TutkCamera.getHex(recData, ret)).append(")")
                                        .toString());

                        if (mIOTCListener != null) {
                            mIOTCListener.receiveIOCtrlData(ioType[0], data);
                        }
                    }
                    try {
                        Thread.sleep(50L);
                    } catch (Exception e) {
                    }
                } else {
                    try {
                        Thread.sleep(50L);
                    } catch (Exception e) {
                    }
                }
            }

        }

        public void stopThread() {
            this.bIsRunning = false;
        }
    }

    final static int IOTYPE_USER_IPCAM_I_FRAME_REQ = 0x0394;
    final static int IOTYPE_USER_IPCAM_I_FRAME_RESP = 0x0395;

    static class SMsgAVIoctrlIFrameReq {
        int channel; // camera index
        byte[] reserved = new byte[4];

        public static int GetStructSize() {
            return 8;
        }

        public byte[] serialize() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.write(int2bytes(channel), 0, 4);

            dos.write(reserved, 0, 4);

            baos.close();
            dos.close();
            return baos.toByteArray();
        }
    }

    public static byte[] int2bytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public static int bytes2int(byte[] b) {
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res <<= 8;
            temp = b[3 - i] & mask;
            res |= temp;
        }
        return res;
    }

    static class SMsgAVIoctrlIFrameResp {
        int result; // 0: success; otherwise: failed.
        byte[] reserved = new byte[4];

        public static int GetStructSize() {
            return 8;
        }

        public static SMsgAVIoctrlIFrameResp deserialize(byte[] data, int iReadBefore)
                throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data, iReadBefore, data.length
                    - iReadBefore);
            DataInputStream dis = new DataInputStream(bais);
            SMsgAVIoctrlIFrameResp daHead = new SMsgAVIoctrlIFrameResp();

            byte[] testbyte = new byte[4];

            dis.read(testbyte, 0, 4);
            daHead.result = bytes2int(testbyte);
            dis.read(daHead.reserved, 0, 4);

            bais.close();
            dis.close();

            return daHead;
        }
    }

    private static byte[] iFrameRequest() {
        SMsgAVIoctrlIFrameReq request = new SMsgAVIoctrlIFrameReq();
        request.channel = 0;
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bas);
        try {
            dos.write(request.serialize());
            bas.close();
            dos.close();
        } catch (IOException e) {
            return null;
        }

        byte[] data = bas.toByteArray();
        return data;
    }

    private class ThreadRecvVideo extends Thread {
        private boolean bIsRunning = false;
        private TutkCamera.AVChannel mAVChannel = null;

        static final int VIDEO_BUF_SIZE = 300000;
        static final int FRAME_INFO_SIZE = 24;// 16;

        private int frameNotReadyCount = 0;
        private int frameNotReadyCountForRetry = 0;
        private long lastReportTime = 0;
        private SMsgAVIoctrlStatisticReport report;

        static final int REPORT_DURATION = 2000;

        boolean receivedVideo = false;
        AVFrame lastAvFrame = null;

        boolean lostFrame = false;

        int[] outFrmSize = new int[1];
        int[] pFrmInfoBuf = new int[1];
        int[] outFrmInfoBufSize = new int[1];

        byte[] frameInfo = new byte[FRAME_INFO_SIZE];
        byte[] videoBuffer = new byte[VIDEO_BUF_SIZE];

        public ThreadRecvVideo(TutkCamera.AVChannel channel) {
            this.mAVChannel = channel;
        }

        void handleFrame(byte[] framedata, int size, int frameNum) {
            report.got++;
            report.bitCount += size;
            byte[] data = new byte[size];
            System.arraycopy(framedata, 0, data, 0, size);
            AVFrame avFrame = new AVFrame(frameNum, (byte) 0, frameInfo, data, size, isByteOrderBig);
            handVideoFrame(avFrame);
        }

        void sendData(int port, byte[] data) {
            if (data == null || data.length == 0) return;

            long lTimestamp = System.currentTimeMillis();

            RecordData recordData = new RecordData();
            recordData.info = AVIOCTRLDEFs.SFrameInfo.parseContent((short) port, (byte) 2,
                    (byte) 0, (byte) 0, lTimestamp, isByteOrderBig);
            recordData.data = data;
            recordData.length = data.length;
            synchronized (SEND_AUDIO_LOCK) {
                mAVChannel.mRecordAudioQueue.add(recordData);
                SEND_AUDIO_LOCK.notifyAll();
            }
        }

        private void report(AVChannel avChannel) {
            long now = System.currentTimeMillis();
            if (now - lastReportTime > REPORT_DURATION && report != null) {
                report.duration = (int) (now - lastReportTime);
                byte[] msgBuffer = report.toByte();
                sendData(254, msgBuffer);
                avChannel.kbps = (int) (report.bitCount * 1.0 / 1024 * 8 / report.duration * 1000);
                report = null;
            }
            if (report == null) {
                lastReportTime = System.currentTimeMillis();
                report = new SMsgAVIoctrlStatisticReport(isByteOrderBig);
            }
        }

        private int lastFrameNo;

        // 收到AvFrame回调
        void handVideoFrame(AVFrame avFrame) {
            if (avFrame.isIFrame()) {
                lostFrame = false;
                lastFrameNo = avFrame.getFrmNo();
            } else {
                if (avFrame.getFrmNo() - lastFrameNo > 1) {
                    lostFrame = true;
                    return;
                }
                lastFrameNo = avFrame.getFrmNo();
            }

            if (lostFrame) {
                AntsLog.d(TAG, "tutk drop frame " + avFrame.toFrameString());
                return;
            }

            if (isEncrypted && avFrame.isIFrame()) {
                AntsUtil.decryptIframe(avFrame, mAVChannel.getEncryptedKey());
            }

            // for log print purpose
            int cameraUseCount = 0;
            if(mIOTCListener != null && mIOTCListener instanceof AntsCamera){
                cameraUseCount = ((AntsCamera) mIOTCListener).getUseCount();
            }

            AntsLog.d("frame", "tutk receive video "
                    + avFrame.toFrameString()
                    + ", useCount:" + avFrame.useCount + "-cameraUseCount:" + cameraUseCount
                    + (avFrame.isIFrame() ? (", isEncrypted:" + isEncrypted + "-" + mAVChannel.getEncryptedKey()) : ""));

            if (mIOTCListener != null) {
                mIOTCListener.receiveVideoFrameData(avFrame);
            }
        }

        public void run() {
            this.bIsRunning = true;
            int[] frameNumber = new int[1];
            AntsLog.i(TAG, "== ThreadRecvVideo start == ");
            // short lastFrameNumber = 0;

            AVAPIs.avClientCleanVideoBuf(this.mAVChannel.getAVIndex());

            // int lostCount = 0;

            while (true) {
                if (!this.bIsRunning) {
                    AntsLog.i(TAG, "===ThreadRecvVideo exit===");
                    receivedVideo = false;
                    break;
                }

                if (mSID < 0 || mAVChannel.getAVIndex() < 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }

                report(mAVChannel);

                if (!receivedVideo) {
                    // receiveStartPlayCallback(mAVChannel);
                    receivedVideo = true;
                }

                int ret = AVAPIs.avRecvFrameData2(this.mAVChannel.getAVIndex(), videoBuffer,
                        VIDEO_BUF_SIZE, outFrmSize, pFrmInfoBuf, frameInfo, FRAME_INFO_SIZE,
                        outFrmInfoBufSize, frameNumber);

                if (ret > 0) {
                    // Log.i(TAG,"ThreadRecvVideo-mAVChannel:"+mAVChannel+";channel:"+mAVChannel.getChannel()
                    // +";avindex:"+mAVChannel.getAVIndex()+";frameNotReadyCountForRetry="+frameNotReadyCountForRetry+";UID:"+Camera.this.mDevUID);

                    frameNotReadyCount = 0;
                    frameNotReadyCountForRetry = 0;

                    if (ret > VIDEO_BUF_SIZE) {
                        AntsLog.e(TAG, "recv video frame too big to recv:" + ret);
                        continue;
                    }

                    handleFrame(videoBuffer, ret, frameNumber[0]);
                } else {

                    if (ret == AVAPIs.AV_ER_DATA_NOREADY) {
                        AntsLog.d(TAG, "AV_ER_DATA_NOREADY=" + frameNotReadyCountForRetry);
                        // noReadyCount++;
                        frameNotReadyCount++;
                        frameNotReadyCountForRetry++;

                        // if(frameNotReadyCount >= 150){
                        // AVAPIs.avClientCleanVideoBuf(this.mAVChannel.getAVIndex());
                        // frameNotReadyCount = 0;
                        // if (BuildConfig.DEBUG) {
                        // Log.d(TAG, "avClientCleanVideoBuf");
                        // }
                        // }

                        if (frameNotReadyCountForRetry > 300) {
                            AntsLog.i(TAG, "frameNotReadyCountForRetry > 300:"
                                    + frameNotReadyCountForRetry);

                            closeCamera(Step.avRecvFrameData, ret);
                            frameNotReadyCount = 0;
                            frameNotReadyCountForRetry = 0;
                            break;
                        }
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            break;
                        }
                        continue;
                    } else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                        report.lost++;
                        lostFrame = true;
                        AntsLog.d(TAG, "receive AV_ER_LOSED_THIS_FRAME");

                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                        }

                        // AVAPIs.avSendIOCtrl(this.mAVChannel.getAVIndex(),
                        // IOTYPE_USER_IPCAM_I_FRAME_REQ, iFrameRequest, 8);
                        continue;
                    } else if (ret == AVAPIs.AV_ER_INCOMPLETE_FRAME) {
                        AntsLog.d(TAG, "receive AV_ER_INCOMPLETE_FRAME");
                        lostFrame = true;
                        // if ((0x1 & frameInfo[2]) == 1) {
                        // IFrameLostCount++;
                        // Log.d("IFrameLostCount", IFrameLostCount + "" +
                        // "size="
                        // + outFrmInfoBufSize[0]);
                        // }
                        report.lost++;

                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    } else if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                        System.out.printf("[%s] AV_ER_SESSION_CLOSE_BY_REMOTE\n", Thread
                                .currentThread().getName());
                        AntsLog.i(TAG, "AV_ER_SESSION_CLOSE_BY_REMOTE");
                        closeCamera(Step.avRecvFrameData, ret);
                        break;
                    } else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                        System.out.printf("[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n", Thread
                                .currentThread().getName());
                        AntsLog.i(TAG, "AV_ER_REMOTE_TIMEOUT_DISCONNECT");
                        closeCamera(Step.avRecvFrameData, ret);
                        break;
                    } else if (ret == AVAPIs.AV_ER_INVALID_SID) {
                        AntsLog.i(TAG, "AV_ER_INVALID_SID");
                        closeCamera(Step.avRecvFrameData, ret);
                        break;
                    } else if (ret == AVAPIs.AV_ER_INVALID_ARG) {
                        AntsLog.i(TAG, "AV_ER_INVALID_ARG");
                        closeCamera(Step.avRecvFrameData, ret);
                        break;
                    }
                }
            }

            receivedVideo = false;

            mAVChannel.threadRecvVideo = null;
        }

        public void stopThread() {
            AntsLog.i(TAG, "===ThreadRecvVideo stopThread===");
            this.bIsRunning = false;
        }
    }

    // public void receiveStartPlayCallback(AVChannel aVChannel) {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "receiveVideoCallback");
    // }
    // if (mIOTCListener != null) {
    // mIOTCListener.receiveStartPlay();
    // }
    // }

    // 断开连接
    private void closeCamera(String step, int ret) {
        AntsLog.d(TAG, "closeCamera:" + ret);
        disconnect();
        if (mIOTCListener != null) {
            mIOTCListener.receiveErrorState(step, ret);
        }
    }

    public int getCameraKbps(int channel) {
        if (avChannel != null) {
            return avChannel.kbps;
        }
        return 0;
    }

    private class ThreadSendIOCtrl extends Thread {
        private boolean bIsRunning = false;
        private TutkCamera.AVChannel mAVChannel;

        public ThreadSendIOCtrl(TutkCamera.AVChannel avChannel) {
            this.mAVChannel = avChannel;
            // AVAPIs.avSendIOCtrl(mAVChannel.getAVIndex(), 255,
            // Packet.intToByteArray(0), 4, false);
        }

        public void run() {
            this.bIsRunning = true;
            AntsLog.i(TAG, "===ThreadSendIOCtrl start===");

            // if ((!this.bIsRunning)
            // || ((Camera.this.mSID >= 0) && (this.mAVChannel
            // .getAVIndex() >= 0))) {
            // Log.i(TAG, "avSendIOCtrl>>255");
            // AVAPIs.avSendIOCtrl(this.mAVChannel.getAVIndex(), 255,
            // Packet.intToByteArray(0), 4, false);
            // }

            while (true) {
                if (!this.bIsRunning) {
                    AntsLog.i(TAG, "===ThreadSendIOCtrl exit===");
                    break;
                }

                if ((TutkCamera.this.mSID >= 0) && (this.mAVChannel.getAVIndex() >= 0)
                        && (!this.mAVChannel.IOCtrlQueue.isEmpty())) {

                    P2PMessage p2pMessage = this.mAVChannel.IOCtrlQueue.poll();
                    if ((this.bIsRunning) && (p2pMessage != null) && p2pMessage.data != null) {
                        int ret = AVAPIs.avSendIOCtrl(this.mAVChannel.getAVIndex(),
                                p2pMessage.reqId, p2pMessage.data, p2pMessage.data.length);
                        if (ret >= 0) {
                            AntsLog.i(
                                    TAG,
                                    "avSendIOCtrl("
                                            + this.mAVChannel.getAVIndex()
                                            + ", 0x"
                                            + Integer.toHexString(p2pMessage.reqId)
                                            + ", "
                                            + TutkCamera.getHex(p2pMessage.data,
                                            p2pMessage.data.length) + ")");
                        }
                        // else if (ret == AVAPIs.AV_ER_SENDIOCTRL_EXIT) {
                        // // 忽略掉此返回
                        // }
                        else {
                            AntsLog.i(TAG, "avSendIOCtrl failed  type= " + p2pMessage.reqId
                                    + ",ret=" + ret);
                            if (mIOTCListener != null) {
                                p2pMessage.error = ret;
                                mIOTCListener.receiveSendP2PMessageError(p2pMessage);
                            }
                            closeCamera(Step.avSendIOCtrl, ret);
                            break;
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                    }
                }

            }

        }

        public void stopThread() {
            this.bIsRunning = false;
            if (this.mAVChannel.getAVIndex() >= 0) {
                AntsLog.i(TAG, "avSendIOCtrlExit(" + this.mAVChannel.getAVIndex() + ")");
                AVAPIs.avSendIOCtrlExit(this.mAVChannel.getAVIndex());
            }
        }
    }

    public boolean isCameraOpen() {
        return isConnectAlive() && isChannelConnected();
    }

    private class ThreadStartDev extends Thread {
        private TutkCamera.AVChannel mAVChannel;
        private boolean mIsRunning = false;
        private Object mWaitObject = new Object();

        private int[] resend = new int[1];
        private boolean waiting;

        public ThreadStartDev(TutkCamera.AVChannel channel) {
            this.mAVChannel = channel;
        }

        public void run() {
            AntsLog.i(TAG, "===ThreadStartDev start===");
            this.mIsRunning = true;
            while (true) {
                if (!mIsRunning) {
                    AntsLog.i(TAG, "===ThreadStartDev exit===");
                    break;
                }
                if (TutkCamera.this.mSID < 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
            }

            int[] pservTypes = new int[1];
            while (mIsRunning) {
                // 如果密码错误，等待获取密码
                if (waiting) {
                    synchronized (this.mWaitObject) {
                        try {
                            this.mWaitObject.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                if (TextUtils.isEmpty(mPwd)) {
                    if (mIOTCListener != null) {
                        mIOTCListener.receiveChannelInfo(AVAPIs.AV_ER_WRONG_VIEWACCorPWD);
                        waiting = true;
                    }
                    continue;
                }

                long start = new Date().getTime();
                int ret = 0;
                int timeout = P2PParams.getInstance().getAvClientStartTimeOut();
                if (isEncrypted) {
                    String account = AntsUtil.genNonce(15);
                    // account = "123456789012345";
                    String password = AntsUtil.getPassword(account, mPwd);

                    AntsLog.i(TAG, "start call avClientStart2(" + mSID + ", " + account + ", "
                            + password + ") in Session(" + mSID + ")," + "pwd=" + mPwd + ", UID=" + mUID);


                    ret = AVAPIs.avClientStart2(mSID, account, password, timeout, pservTypes, 0, resend);

                } else {
                    AntsLog.i(
                            TAG,
                            "start call avClientStart2(" + mSID + ", "
                                    + mAcc + ", "
                                    + mPwd + ") in Session(" + mSID);

                    ret = AVAPIs.avClientStart2(mSID, mAcc,
                            mPwd, timeout, pservTypes, 0, resend);
                }

                AntsLog.D("call avClientStart2  returns=" + ret + " ,resend=" + resend[0]
                        + " ,uid=" + mDevUID);
                long end = new Date().getTime();

                AntsLog.d(TAG, "avClientStart2 use: " + ((end - start) / 1000.0) + " sec");

                this.mAVChannel.inStarting = false;

                if (ret >= 0) {
                    long type = pservTypes[0];
                    this.mAVChannel.setAVIndex(ret);
                    this.mAVChannel.avClientStarted = 1;
                    this.mAVChannel.setServiceType(type);
                    AntsLog.i(TAG,
                            "mAVChannel AVINDEX=" + ret + ",ServiceType=" + String.valueOf(type));
                    setConnectingProgress(AntsCamera.CONNECTION_STATE_START_AV_CLIENT_SUCCESS);
                    mInConnecting = false;
                    break;
                } else {
                    this.mAVChannel.avClientStarted = -1;
                    setConnectingProgress(AntsCamera.CONNECTION_STATE_START_AV_CLIENT_FAIL);
                    if (mIOTCListener != null) {
                        mIOTCListener.receiveChannelInfo(ret);
                    }

                    if (ret == AVAPIs.AV_ER_WRONG_VIEWACCorPWD) {
                        // mInConnecting = false;
                        waiting = true;
                    } else {
                        disconnect();
                        mInConnecting = false;
                    }
                }


            }
        }

        public void stopThread() {
            this.mIsRunning = false;
            synchronized (this.mWaitObject) {
                this.mWaitObject.notify();
                return;
            }
        }

        public void retry() {
            waiting = false;
            synchronized (this.mWaitObject) {
                this.mWaitObject.notify();
                return;
            }
        }
    }

    private void startSendReport() {
        if (avChannel == null) {
            return;
        }

        AntsLog.i(TAG, "cameraTUTK-startSendReport-size:" + 1 + ";avchannel:" + avChannel
                + ";channel:" + 0 + ";UID:" + TutkCamera.this.mDevUID);
        if (avChannel.threadSendAudio == null) {
            avChannel.threadSendAudio = new ThreadSendAudio(avChannel);
            avChannel.threadSendAudio.start();
        }
    }

    private void stopSendReport() {
        if (avChannel == null) {
            return;
        }

        AntsLog.i(TAG, "cameraTUTK-stopSendReport-size:" + 1 + ";avchannel:" + avChannel
                + ";channel:" + 0 + ";UID:" + TutkCamera.this.mDevUID);

        if (avChannel.threadSendAudio != null) {
            AntsLog.i(TAG, "cameraTUTK-stopSendReport");
            avChannel.threadSendAudio.interrupt();
            avChannel.threadSendAudio.stopThread();
            avChannel.threadSendAudio = null;
        }
    }

    public void startSpeaking(int talkMode) {
        byte[] data = Packet.intToByteArray(talkMode, isByteOrderBig);
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART, data);
        this.talkMode = talkMode;
        if(avChannel != null){
            if (avChannel.threadRecordAudio == null) {
                avChannel.mRecordAudioQueue.clear();
                if(P2PDevice.MODEL_V2.equals(mModel)
                        || P2PDevice.MODEL_H19.equals(mModel)
                        || P2PDevice.MODEL_H20.equals(mModel)
                        || P2PDevice.MODEL_M20.equals(mModel)
                        || P2PDevice.MODEL_Y10.equals(mModel)
                        || P2PDevice.MODEL_Y20.equals(mModel)) {

                    if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.VOIP_MODE){
                        avChannel.threadRecordAudio = new ThreadRecordAudioAACAEC(avChannel,mMobileAEC);
                    //    avChannel.threadRecordAudio = new ThreadRecordAudioAAC(avChannel);
                    }else{
                        avChannel.threadRecordAudio = new ThreadRecordAudioAAC(avChannel);
                    }

                } else {
                    avChannel.threadRecordAudio = new ThreadRecordAudioG726(avChannel);
                }
                avChannel.threadRecordAudio.start();
            }
        }
    }

    public void stopSpeaking() {
        if (avChannel != null) {
            if (avChannel.threadRecordAudio != null) {
                avChannel.threadRecordAudio.stopThread();
                avChannel.threadRecordAudio = null;
            }
        }
    }

    private boolean m_bOnlyAliveThreadRecordAudio = false;

    private class ThreadRecordAudio extends Thread {
        AVChannel mAVChannel;

        boolean m_bIsRunning = false;

        public ThreadRecordAudio(TutkCamera.AVChannel avChannel) {
            this.mAVChannel = avChannel;
        }

        public void stopThread() {
            m_bIsRunning = false;
        }
    }

    private class ThreadRecordAudioG726 extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 8000;

        private long lTimestampKey = 1;

        public ThreadRecordAudioG726(TutkCamera.AVChannel avChannel) {
            super(avChannel);
        }

        public void run() {
            if (m_bOnlyAliveThreadRecordAudio) {
                AntsLog.i(TAG, "=== got multi m_bOnlyAliveThreadRecordAudio="
                        + m_bOnlyAliveThreadRecordAudio);
                return;
            }

            m_bOnlyAliveThreadRecordAudio = true;

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
            while (this.m_bIsRunning && mAVChannel.avIndexForSendAudio >= 0) {
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
                    AntsLog.d(
                            TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info:"))
                                    .append(TutkCamera.getHex(data.info, data.info.length))
                                    .append(")").toString());
                    AntsLog.d(TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info-time:")).append(lTimeStamp)
                                    .append(")").toString());
                    synchronized (SEND_AUDIO_LOCK) {
                        mAVChannel.mRecordAudioQueue.add(data);
                        AntsLog.d(
                                TAG,
                                (new StringBuilder("TUTK-sendAudioInf.info-add:"))
                                        .append(TutkCamera.getHex(data.info, data.info.length))
                                        .append(")").toString());
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

            m_bOnlyAliveThreadRecordAudio = false;

            mAVChannel.threadRecordAudio = null;
        }

    }

    private class ThreadRecordAudioAAC extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 16000;
        private int bitRate = 32000;

        private long lTimestampKey = 1;

        public ThreadRecordAudioAAC(TutkCamera.AVChannel avChannel) {
            super(avChannel);
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

            while (this.m_bIsRunning && mAVChannel.avIndexForSendAudio >= 0) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                if (bufferRead <= 0) {
                    break;
                }

                byte[] encodePCM = vo.Enc(tempBuffer);


                if (encodePCM != null) {
                    AntsLog.d("record", "tutk aac buffer size:" + encodePCM.length);

                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    byte[] sendAudioInf = AVIOCTRLDEFs.SFrameInfo.parseContent((short) 138,
                            (byte) 2, (byte) 0, (byte) 0, lTimeStamp, isByteOrderBig);
                    RecordData data = new RecordData();
                    data.info = sendAudioInf;
                    data.data = encodePCM;
                    data.length = encodePCM.length;
                    data.type = 1;
                    AntsLog.d(
                            TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info:"))
                                    .append(TutkCamera.getHex(data.info, data.info.length))
                                    .append(")").toString());
                    AntsLog.d(TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info-time:")).append(lTimeStamp)
                                    .append(")").toString());
                    synchronized (SEND_AUDIO_LOCK) {
                        mAVChannel.mRecordAudioQueue.add(data);
                        AntsLog.d(
                                TAG,
                                (new StringBuilder("TUTK-sendAudioInf.info-add:"))
                                        .append(TutkCamera.getHex(data.info, data.info.length))
                                        .append(")").toString());
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

            mAVChannel.threadRecordAudio = null;
        }


    }




    private class ThreadRecordAudioAACAEC extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 16000;
        private int bitRate = 32000;

        private long lTimestampKey = 1;

        private ByteRingBuffer RingBuffer = null;
        private MobileAEC mobileAEC;

        public ThreadRecordAudioAACAEC(TutkCamera.AVChannel avChannel,MobileAEC mobileAEC) {
            super(avChannel);
            this.mobileAEC = mobileAEC;
            this.mobileAEC.reset();
        }


        @Override
        public void stopThread() {
            super.stopThread();
            if(mobileAEC != null){
                mobileAEC.mRecordFlag = false;
            }
        }

        private double getVolume(short[] buffer, int read){
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < read; i++) {
                v += buffer[i] * buffer[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double)read ;
            double volume = 10 * Math.log10(mean);
            AntsLog.d(TAG, "audio volume:" + volume);
            return  volume;
        }

        public void run() {
            if (m_bOnlyAliveThreadRecordAudio) {
                AntsLog.i(TAG, "=== got multi m_bOnlyAliveThreadRecordAudio="
                        + m_bOnlyAliveThreadRecordAudio);
                return;
            }
            m_bOnlyAliveThreadRecordAudio = true;

            this.m_bIsRunning = true;

            int CaptureBufferSize = 640 * 4 ;
            int bufferSize = 320;
            int packetCount = 1;
            boolean AfterfirstFrameFlag = false;
            int offset = 0;
            int Encoffset = 0;
            int Packetoffset = 0;
            int first = 0;
            final int AAC_Decode_Length = 2048;
            final int AEC_Process_Short_Length = 160;
            final int Talk_Mode_length = 4;
            short delay ;
            byte[] tempBufferOneInstace = new byte[bufferSize];
            short[] tempBufferShortOneInstance = new short[bufferSize / 2];
            short[] OutBufferShortOneInstance = new short[CaptureBufferSize / 2];
            short[] outTempShortBuffer = new short[AEC_Process_Short_Length];
            short[] outTempAgcShortBuffer = new short[AEC_Process_Short_Length];
            short[] inTempShortBuffer = new short[AEC_Process_Short_Length];
            short[] outMAecTempShortBuffer = new short[AEC_Process_Short_Length];
            byte[]  aacPorcessData = new byte[AAC_Decode_Length];
            byte[]  tempBuffer =  new byte[bufferSize];
            byte[]  recordData = new byte[CaptureBufferSize];
            byte[]  header = new byte[Talk_Mode_length];
            byte mode = 0;
            //AEC ADD END
			header[0] = 0;
            VoAACEncoder vo = new VoAACEncoder();
            vo.Init(sampleRate, bitRate, (short)1, (short)1);

            MobileNS mobileNS = new MobileNS();
            mobileNS.init(sampleRate);
            mobileNS.setPolicyMode(2);

            MobileVAD mobileVAD = new MobileVAD();
            mobileVAD.init();
            mobileVAD.setPolicyMode(1);

            MobileAGC mobileAGC = new MobileAGC();

            if(mModel.equals(P2PDevice.MODEL_V2)){
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_LOW);
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_LOW);
            }else if(mModel.equals(P2PDevice.MODEL_H19)){
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_HIGH);
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_HIGH);
            }else{
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_DEFAULT);
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_DEFAULT);
            }

            RingBuffer = new ByteRingBuffer(2* 1024 *1024);

            int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(min < bufferSize){
                min = bufferSize;
            }

            try{
         //       mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
         //       mAudioRecord.startRecording();

                //AEC
                if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.SINGLE_MODE) {
                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
                }else if(talkMode == AntsCamera.VOIP_MODE) {
                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
                }
                mAudioRecord.startRecording();

            }catch (Exception e){
                e.printStackTrace();
            }

            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                lTimestampKey = 1;
                return;
            }

            mobileAEC.mRecordFlag = true;
            while (this.m_bIsRunning && mAVChannel.avIndexForSendAudio >= 0) {

                if(!mobileAEC.mPlayerFlag){
                    first = 16;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if(first > 0){
                    while(first > 0){
                        first --;
                        int bufferRead = mAudioRecord.read(tempBufferOneInstace, 0, bufferSize);
                        AntsLog.e(TAG, "Packetoffset bufferSize is " + bufferRead);
                        if (bufferRead <= 0) {
                            break;
                        }
                    }
                }

                int bufferRead = mAudioRecord.read(tempBufferOneInstace, 0, bufferSize);
                AntsLog.e(TAG, "Packetoffset bufferSize is " + bufferRead);
                if (bufferRead <= 0) {
                    break;
                }


                if(talkMode == AntsCamera.MIC_MODE){
                    mode = 1;
                    header[0] = 0x4;
                } else if(talkMode == AntsCamera.VOIP_MODE){
                    mode = 2;
                    header[0] = 0x8;
                }

                if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.VOIP_MODE)
                {
                    try {
                           /* if(Build.MODEL.contains("MI 5")){
                                AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
                            }
                            else if(Build.MODEL.contains("MI 4")){
                                AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
                            }
                            else {
                                AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
                            }*/
                        AudioUtil.AmplifyPCMData(tempBufferOneInstace);

                        ByteBuffer.wrap(tempBufferOneInstace).order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer().get(tempBufferShortOneInstance);
                        //mobileAGC.Process(tempBufferShortOneInstance, 1, sampleRate, outTempAgcShortBuffer, 0, (short) 0);
//                            ByteBuffer.wrap(test12).order(ByteOrder.LITTLE_ENDIAN)
//                                    .asShortBuffer().put(outTempAgcShortBuffer);
//                            try{
//                                fin.write(test12, 0, test12.length);
//                            }catch (Exception e){
//
//                            }
                        mobileAEC.echoCancellation(tempBufferShortOneInstance, null, outMAecTempShortBuffer, (short) outMAecTempShortBuffer.length,
                                delay);
                        mobileAGC.Process(outMAecTempShortBuffer, 1, sampleRate, outTempAgcShortBuffer, 0, (short) 0);
//                            ByteBuffer.wrap(test12).order(ByteOrder.LITTLE_ENDIAN)
//                                    .asShortBuffer().put(outMAecTempShortBuffer);
//                            AmplifyPCMData(test12, test12.length, (float) 3.3);
//                            ByteBuffer.wrap(test12).order(ByteOrder.LITTLE_ENDIAN)
//                                    .asShortBuffer().get(outMAecTempShortBuffer);
//                            try{
//                                fout.write(test12, 0, test12.length);
//                            }catch (Exception e){
//
//                            }

//                            if(AudioVolume(outMAecTempShortBuffer, outMAecTempShortBuffer.length) < 40){
//                                Arrays.fill(outMAecTempShortBuffer, (short)0);
//                            }
                        //   mobileAGC.Process(outMAecTempShortBuffer, 1, sampleRate, outTempAgcShortBuffer, 0, (short) 0);
                        mobileNS.NsProcess(outTempAgcShortBuffer, 1, outTempShortBuffer);

                        mobileVAD.VADProcess(outTempShortBuffer, outTempShortBuffer.length, sampleRate);
                        int isVoice = mobileVAD.VADProcess(outTempShortBuffer, outTempShortBuffer.length, sampleRate);

                        if(isVoice == 0) {
                            AmplifyPCMData(outTempShortBuffer, outTempShortBuffer.length, 0.5f);
                        }
                        ByteBuffer.wrap(tempBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outTempShortBuffer);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }  else if(talkMode == AntsCamera.SINGLE_MODE){
                    System.arraycopy(tempBufferOneInstace, 0, tempBuffer, 0, tempBuffer.length);
                }
                RingBuffer.write(tempBuffer, 0, tempBuffer.length);

                if(RingBuffer.getUsed() < AAC_Decode_Length)
                    continue;

                RingBuffer.read(aacPorcessData, 0, AAC_Decode_Length);


                byte[] encodePCM = vo.Enc(aacPorcessData);

                //AEC ADD
                byte[] sendData = new byte[encodePCM.length + header.length];
                System.arraycopy(header, 0, sendData, 0, header.length);
                System.arraycopy(encodePCM, 0, sendData, header.length,encodePCM.length);

                //END
                if (encodePCM != null) {
                    AntsLog.d("record", "tutk aac buffer size:" + encodePCM.length);

                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    byte[] sendAudioInf = AVIOCTRLDEFs.SFrameInfo.parseContent((short) 138,
                            (byte) 2, (byte) 0, (byte) 0, lTimeStamp, isByteOrderBig);
                    RecordData data = new RecordData();
                    data.info = sendAudioInf;

                    data.data = sendData;
                    data.length = sendData.length;
                    data.type = 1;
                    AntsLog.d(
                            TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info:"))
                                    .append(TutkCamera.getHex(data.info, data.info.length))
                                    .append(")").toString());
                    AntsLog.d(TAG,
                            (new StringBuilder("TUTK-sendAudioInf.info-time:")).append(lTimeStamp)
                                    .append(")").toString());
                    synchronized (SEND_AUDIO_LOCK) {
                        mAVChannel.mRecordAudioQueue.add(data);
                        AntsLog.d(
                                TAG,
                                (new StringBuilder("TUTK-sendAudioInf.info-add:"))
                                        .append(TutkCamera.getHex(data.info, data.info.length))
                                        .append(")").toString());
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
                    //mobileAEC.close();
                    mobileNS.close();
                    mobileVAD.close();
                } catch (Exception e) {

                }
            }

            lTimestampKey = 1;

            m_bOnlyAliveThreadRecordAudio = false;

            mAVChannel.threadRecordAudio = null;
        }



        public void AmplifyPCMData(short[] temp, int nLen, float multiple){
            if(multiple == 1)return;
            int nCur = 0;
            //short[] temp = new short[nLen / 2];

//            ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
//                    .asShortBuffer().get(temp);

            while (nCur < nLen)
            {
                //    short* volum = (short*)(pData + nCur);
                short volume = temp[nCur];
                temp[nCur] = (short)(volume * multiple);

                if (temp[nCur] < -0x8000)
                {
                    temp[nCur] = -0x8000;
                }

                if (temp[nCur] > 0x7FFF)//爆音的处理
                {
                    temp[nCur] = 0x7FFF;
                }

                nCur++;
            }

//            ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
//                    .asShortBuffer().put(temp);

        }
    }
    private Object SEND_AUDIO_LOCK = new Object();

    private class ThreadSendAudio extends Thread {

        private int chIndexForSendAudio = -1;
        private TutkCamera.AVChannel mAVChannel = null;
        private boolean m_bIsRunning = false;

        public ThreadSendAudio(TutkCamera.AVChannel avChannel) {
            this.mAVChannel = avChannel;
        }

        public void run() {
            super.run();
            AntsLog.i(TAG, " == = ThreadSendAudio start == = ");
            this.m_bIsRunning = true;

            while (TutkCamera.this.mSID < 0 || mAVChannel.getAVIndex() < 0) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                }
            }

            // 发送时区信息
            // sendTimeZone(mAVChannel.getChannel());

            int tryTime = 0;

            while (m_bIsRunning && tryTime < 3) {
                this.chIndexForSendAudio = 4;

                byte[] sendAudioMsg = AVIOCTRLDEFs.SMsgAVIoctrlAVStream
                        .parseContent(chIndexForSendAudio, isByteOrderBig);
                sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART, sendAudioMsg);

                AntsLog.i(TAG, "start avServerStart " + "UID:" + TutkCamera.this.mDevUID);

                mAVChannel.avIndexForSendAudio = AVAPIs.avServStart(TutkCamera.this.mSID, null, null,
                        0, 0, chIndexForSendAudio);

                AntsLog.i(TAG, "avServerStart(" + TutkCamera.this.mSID + ", "
                        + this.chIndexForSendAudio + ") : " + mAVChannel.avIndexForSendAudio
                        + ";UID:" + TutkCamera.this.mDevUID);

                // AVAPIs.avServExit(Camera.this.mSID, chIndexForSendAudio);

                tryTime++;

                AntsLog.d(TAG, "avIndexForSendAudio=" + mAVChannel.avIndexForSendAudio + ";UID:"
                        + TutkCamera.this.mDevUID);

                if (mAVChannel.avIndexForSendAudio >= 0) {
                    if (mIOTCListener != null) {
                        mIOTCListener.receiveSpeakEnableInfo(true);
                    }
                    break;
                }
            }

            if (mAVChannel.avIndexForSendAudio < 0) {
                return;
            }

            while (this.m_bIsRunning && (TutkCamera.this.mSID >= 0) && (mAVChannel.getAVIndex() >= 0)) {

                if (mAVChannel.mRecordAudioQueue.size() > 0) {

                    RecordData recordData = null;

                    synchronized (SEND_AUDIO_LOCK) {
                        recordData = mAVChannel.mRecordAudioQueue.poll();
                    }
                    if (recordData == null) continue;

                    AVAPIs.avSendAudioData(mAVChannel.avIndexForSendAudio, recordData.data,
                            (int) recordData.length, recordData.info, 16);

                    // if (BuildConfig.DEBUG) {
                    // Log.d("record", "AVAPIs.avSendAudioData:" + mAVChannel.avIndexForSendAudio
                    // + ";UID:" + TutkCamera.this.mDevUID);
                    // }

                } else {
                    synchronized (SEND_AUDIO_LOCK) {
                        try {
                            SEND_AUDIO_LOCK.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            AntsLog.i(TAG, "===ThreadSendAudio exit===" + ";UID:" + TutkCamera.this.mDevUID);

            mAVChannel.threadSendAudio = null;
            mAVChannel = null;
        }

        public void stopThread() {
            // IOTCAPIs.IOTC_Session_Channel_OFF(TutkCamera.this.mSID, 4);
            AntsLog.i(TAG, "===stopThread ThreadSendAudio ===" + ";UID:" + TutkCamera.this.mDevUID);

            stopAvServ(this.mAVChannel);

            this.m_bIsRunning = false;
            synchronized (SEND_AUDIO_LOCK) {
                SEND_AUDIO_LOCK.notifyAll();
            }
            this.interrupt();
        }

    }

    public void sendStopRecordVideoCommand() {
        int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL;
        if (isEncrypted) {
            command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL2;
        }
        sendIOCtrl(command, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(0,
                AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0, new byte[8], (byte) 0, isByteOrderBig));
    }

    public int getSInfoMode() {
        if (!isCameraOpen()) {
            return -1;
        }

        St_SInfo localSt_SInfo = new St_SInfo();
        int ret = IOTCAPIs.IOTC_Session_Check(getSID(), localSt_SInfo);

        if (ret < 0) {
            return ret;
        }

        return localSt_SInfo.Mode;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public St_SInfoEx getSessionInfo() {
        if (!isCameraOpen()) {
            return null;
        }


        St_SInfoEx localSt_SInfo = new St_SInfoEx();
        int ret = IOTCAPIs.IOTC_Session_Check_Ex(getSID(), localSt_SInfo);

        int[] version = new int[1];
        IOTCAPIs.IOTC_Get_Version(version);
        localSt_SInfo.IOTCVersion = (int) version[0];

        if (ret < 0) {
            return null;
        }

        return localSt_SInfo;
    }
}
