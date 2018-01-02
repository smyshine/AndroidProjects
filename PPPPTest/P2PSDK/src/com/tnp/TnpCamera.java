package com.tnp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.audio.handle.AudioProcOut;
import com.encoder.util.EncG726;
import com.p2p.pppp_api.PPPP_APIs;
import com.sinaapp.bashell.VoAACEncoder;
import com.tnp.model.TNPFrameHead;
import com.tnp.model.TNPHead;
import com.tnp.model.TNPIOCtrlHead;
import com.tnp.model.st_PPPP_Session;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Packet;
import com.xiaoyi.camera.sdk.AntsCamera;
import com.xiaoyi.camera.sdk.AudioUtil;
import com.xiaoyi.camera.sdk.IRegisterCameraListener;
import com.xiaoyi.camera.sdk.P2PDevice;
import com.xiaoyi.camera.sdk.P2PMessage;
import com.xiaoyi.camera.sdk.Step;
import com.xiaoyi.camera.util.AntsUtil;
import com.xiaoyi.log.AntsLog;
import com.xiaoyi.p2pservertest.audio.ByteRingBuffer;
import com.xiaoyi.p2pservertest.audio.MobileAEC;
import com.xiaoyi.p2pservertest.audio.MobileAGC;
import com.xiaoyi.p2pservertest.audio.MobileNS;
import com.xiaoyi.p2pservertest.audio.MobileVAD;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Chuanlong on 2015/11/12.
 */
public class TnpCamera {

    private static final String TAG = "TnpCamera";

    public static final int ERROR_PPPP_OTHER_MANUAL_RETRY       = -9000;

    public static final int ERROR_PPPP_DEVICE_KICK 				= -3100; //被设备踢下线
    public static final int ERROR_PPPP_DEVICE_KICK_MAX_SESSION 	= -3101; //因视频Session数超限被设备踢下线


    public static final byte CHANNEL_COMMAND = 0;
    public static final byte CHANNEL_AUDIO = 1;
    public static final byte CHANNEL_VIDEO_REALTIME_IFRAME = 2;
    public static final byte CHANNEL_VIDEO_REALTIME_PFRAME = 3;
    public static final byte CHANNEL_VIDEO_RECORD_IFRAME = 4;
    public static final byte CHANNEL_VIDEO_RECORD_PFRAME = 5;
    public static final byte CHANNEL_MAX = 8;


    private String uid;
    private String p2pid;
    private String serverString;
    private String licenseDeviceKey;

    private String account;
    private String password;
    private String model;
    private boolean isEncrypted;
    private String decryptKey;
    private boolean isByteOrderBig;

    private int mHandleSession = -1;
    private IRegisterCameraListener mCameraListener;
    private Queue<P2PMessage> mIOCtrlQueue = new LinkedBlockingQueue<P2PMessage>();
    private Object mSendAudioLock = new Object();
    private Queue<RecordData> mRecordAudioQueue = new LinkedBlockingQueue<RecordData>();

    private final static int QUEUE_AV_FRAME_MAX_SIZE = 100;
    private AVFrame mLastReceiveAVFrame = null;
    private Queue<AVFrame> mAVFrameQueue = new LinkedList<AVFrame>();
    private int mConnectStatus = 0;     // 0 disconnect, 1 connecting, 2 connected

    private Object mConnectLock = new Object();
    private ExecutorService mThreadPoolConnect;
    private RunnableConnect mConnectRunnable;
    private RunnableDisconnect mDisconnectRunnable;

    private ThreadSendIOCtrl mThreadSendIOCtrl;
    private ThreadSendAudio mThreadSendAudio;
    private ThreadRecordAudio mThreadRecordAudio;
    private ThreadNetworkCheckInfo mThreadNetworkCheckInfo;
    private ThreadOnlineStatus mThreadOnlineStatus;

    private ThreadRecvAudio mThreadRecvAudio;
    private ThreadRecvIOCtrl mThreadRecvIOCtrl;
    private ThreadRecvVideoRealTimeIFrame mThreadRecvVideoRealTimeIFrame;
    private ThreadRecvVideoRealTimePFrame mThreadRecvVideoRealTimePFrame;
    private ThreadRecvVideoRecordIFrame mThreadRecvVideoRecordIFrame;
    private ThreadRecvVideoRecordPFrame mThreadRecvVideoRecordPFrame;
    private MobileAEC mMobileAEC;
    private int talkMode;
    private boolean isFactoryTest;

    private byte tnpHeaderVersion = TNPHead.VERSION_ONE;

    private long sessionEstablishedTimestamp = -1;  //Session连接成功建立时的时间戳

//    public final static int MIC_MODE = 1;       //双工 免提模式
//    public final static int VOIP_MODE = 2;      //双工 电话模式
//    public final static int SINGLE_MODE = 0;    //单工


    public TnpCamera(String uid, String p2pid, String serverString, String licenseDeviceKey,
                     String account, String password, String model,
                     boolean isEncrypted, boolean isByteOrderBig, boolean isFactoryTest) {
        this.uid = uid;
        this.p2pid = p2pid;
        this.serverString = serverString;
        this.licenseDeviceKey = licenseDeviceKey;
        this.account = account;
        this.password = password;
        this.model = model;
        this.isEncrypted = isEncrypted;
        this.decryptKey = password + "0";
        this.isByteOrderBig = isByteOrderBig;
        this.mMobileAEC = MobileAEC.getInstance();
        this.talkMode = AntsCamera.SINGLE_MODE;
        this.isFactoryTest = isFactoryTest;
    }

    public void registerCameraListener(IRegisterCameraListener listener){
        this.mCameraListener = listener;
    }

    public void unregisterCameraListener() {
        mCameraListener = null;
    }

    public void connect(){
        AntsLog.D(TAG + ", connect");

        synchronized (mConnectLock) {
            if (mConnectRunnable == null && (!isConnected() || mDisconnectRunnable != null)){
                if (mThreadPoolConnect == null) {
                    mThreadPoolConnect = Executors.newSingleThreadExecutor();
                }
                mConnectRunnable = new RunnableConnect();
                mThreadPoolConnect.submit(mConnectRunnable);
            }
            if(mDisconnectRunnable != null){
                mDisconnectRunnable.cancelRunnable();
                mDisconnectRunnable = null;
            }
        }

    }

    public void start(){
        AntsLog.D(TAG + ", start");

        synchronized (mConnectLock) {

            // 启动发送指令线程
            if (mThreadSendIOCtrl == null || !mThreadSendIOCtrl.isRunning()) {
                mThreadSendIOCtrl = new ThreadSendIOCtrl();
                mThreadSendIOCtrl.start();
            }

            // 启动接收指令线程
            if (mThreadRecvIOCtrl == null || !mThreadRecvIOCtrl.isRunning()) {
                mThreadRecvIOCtrl = new ThreadRecvIOCtrl();
                mThreadRecvIOCtrl.start();
            }

            // 启动接收音频
            if (mThreadRecvAudio == null || !mThreadRecvAudio.isRunning()) {
                mThreadRecvAudio = new ThreadRecvAudio();
                mThreadRecvAudio.start();
            }

            // 启动接收实时视频I帧
            if (mThreadRecvVideoRealTimeIFrame == null || !mThreadRecvVideoRealTimeIFrame.isRunning()) {
                mThreadRecvVideoRealTimeIFrame = new ThreadRecvVideoRealTimeIFrame();
                mThreadRecvVideoRealTimeIFrame.start();
            }

            // 启动接收实时视频P帧
            if (mThreadRecvVideoRealTimePFrame == null || !mThreadRecvVideoRealTimePFrame.isRunning()) {
                mThreadRecvVideoRealTimePFrame = new ThreadRecvVideoRealTimePFrame();
                mThreadRecvVideoRealTimePFrame.start();
            }

            // 启动接收历史视频I帧
            if (mThreadRecvVideoRecordIFrame == null || !mThreadRecvVideoRecordIFrame.isRunning()) {
                mThreadRecvVideoRecordIFrame = new ThreadRecvVideoRecordIFrame();
                mThreadRecvVideoRecordIFrame.start();
            }

            // 启动接收历史视频P帧
            if (mThreadRecvVideoRecordPFrame == null || !mThreadRecvVideoRecordPFrame.isRunning()) {
                mThreadRecvVideoRecordPFrame = new ThreadRecvVideoRecordPFrame();
                mThreadRecvVideoRecordPFrame.start();
            }


            // 启动发送音频/视频线程
            if (mThreadSendAudio == null || !mThreadSendAudio.isRunning()) {
                mThreadSendAudio = new ThreadSendAudio();
                mThreadSendAudio.start();
            }

            mLastReceiveAVFrame = null;
            mAVFrameQueue = new LinkedList<AVFrame>();

            if(mConnectStatus != 2){
                mConnectStatus = 1;
            }
        }

    }

    public void disconnect(){
        AntsLog.D(TAG + ", disconnect");

        synchronized (mConnectLock) {

            if(mDisconnectRunnable == null && mHandleSession >= 0) {
                if (mThreadPoolConnect == null) {
                    mThreadPoolConnect = Executors.newSingleThreadExecutor();
                }
                mDisconnectRunnable = new RunnableDisconnect();
                mThreadPoolConnect.submit(mDisconnectRunnable);
            }

            if(mThreadSendIOCtrl != null){
                mThreadSendIOCtrl.stopThread();
                mThreadSendIOCtrl = null;
            }

            if(mThreadRecvIOCtrl != null){
                mThreadRecvIOCtrl.stopThread();
                mThreadRecvIOCtrl = null;
            }

            if(mThreadRecvAudio != null){
                mThreadRecvAudio.stopThread();
                mThreadRecvAudio = null;
            }

            if(mThreadRecvVideoRealTimeIFrame != null){
                mThreadRecvVideoRealTimeIFrame.stopThread();
                mThreadRecvVideoRealTimeIFrame = null;
            }

            if(mThreadRecvVideoRealTimePFrame != null){
                mThreadRecvVideoRealTimePFrame.stopThread();
                mThreadRecvVideoRealTimePFrame = null;
            }

            if(mThreadRecvVideoRecordIFrame != null){
                mThreadRecvVideoRecordIFrame.stopThread();
                mThreadRecvVideoRecordIFrame = null;
            }

            if(mThreadRecvVideoRecordPFrame != null){
                mThreadRecvVideoRecordPFrame.stopThread();
                mThreadRecvVideoRecordPFrame = null;
            }

            if(mThreadSendAudio != null){
                mThreadSendAudio.stopThread();
                mThreadSendAudio = null;
            }

            mIOCtrlQueue.clear();

            mConnectStatus = 0;

            System.gc();
        }

    }

    public void pause(){
        AntsLog.D(TAG + ", pause");

        sendStopPlayVideoCommand();
        sendStopRecordVideoCommand();
    }

    public void startSpeaking(int talkMode) {
        byte[] data = Packet.intToByteArray(talkMode, isByteOrderBig);
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART, data);
        this.talkMode = talkMode;
        mRecordAudioQueue.clear();
        if(mThreadRecordAudio == null){
            if(P2PDevice.MODEL_V1.equals(model)){
                mThreadRecordAudio = new ThreadRecordAudioG726();
            }else if(P2PDevice.MODEL_H19.equals(model)
                    || P2PDevice.MODEL_H20.equals(model)
                    || P2PDevice.MODEL_M20.equals(model)
                    || P2PDevice.MODEL_Y20.equals(model)
                    || P2PDevice.MODEL_V2.equals(model)
                    || P2PDevice.MODEL_Y10.equals(model)
                    || P2PDevice.MODEL_D11.equals(model)){
                if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.VOIP_MODE){
                    mThreadRecordAudio = new ThreadRecordAudioAACAEC(mMobileAEC);
                }else{
                    mThreadRecordAudio = new ThreadRecordAudioAAC();
                }
            }else{
                mThreadRecordAudio = new ThreadRecordAudioG726();
            }
            mThreadRecordAudio.start();
        }
    }

    public void stopSpeaking() {
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTOP, new byte[8]);
        if(mThreadRecordAudio != null){
            mThreadRecordAudio.stopThread();
            mThreadRecordAudio = null;
        }
    }

    public synchronized void getNetworkInfo() {
        if (mThreadNetworkCheckInfo == null) {
            mThreadNetworkCheckInfo = new ThreadNetworkCheckInfo();
            mThreadNetworkCheckInfo.start();
        }
    }

    public synchronized void getOnlineStatus() {
        if (mThreadOnlineStatus == null) {
            mThreadOnlineStatus = new ThreadOnlineStatus();
            mThreadOnlineStatus.start();
        }
    }


    public void sendStartListeningCommand(){
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, new byte[8]);
    }

    public void sendStopListeningCommand(){
        sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, new byte[8]);
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

    public boolean isConnected() {
        st_PPPP_Session sInfo = getTNPSession();
        if(sInfo != null){
            return true;
        }
        return false;
    }

    public long getSessionEstablishedTimestamp(){
        return this.sessionEstablishedTimestamp;
    }

    public st_PPPP_Session getTNPSession(){
        if (mHandleSession < 0) {
            return null;
        }

        st_PPPP_Session sInfo = new st_PPPP_Session();
        int ret = PPPP_APIs.PPPP_Check(mHandleSession, sInfo);
        if(ret == PPPP_APIs.ERROR_PPPP_SUCCESSFUL){
            return sInfo;
        }

        return null;
    }

    public void sendIOCtrl(P2PMessage p2pMessage) {
        AntsLog.d(TAG, "sendIOCtrl add p2pMessage to queue, "
                + ", 0x" + Integer.toHexString(p2pMessage.reqId)
                + ", " + AntsUtil.getHex(p2pMessage.data, p2pMessage.data.length) + ")");
        mIOCtrlQueue.add(p2pMessage);
    }

    public void sendIOCtrl(int reqId, byte[] data) {
        P2PMessage p2pMessage = new P2PMessage(reqId, data);
        sendIOCtrl(p2pMessage);
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public void setPassword(String password){
        AntsLog.d(TAG, "setPassword:" + password);
        this.password = password;
        this.decryptKey = password + "0";
        // 如果修改了密码，需要触发UI界面自动重连
        sendErrorState(Step.PPPP_Manual, ERROR_PPPP_OTHER_MANUAL_RETRY);
    }

    public void updateTnpConnectInfo(String serverString, String licenseDeviceKey){
        AntsLog.d(TAG, "tnpServerString:" + serverString + ", licenseDeviceKey:" + licenseDeviceKey);
        this.serverString = serverString;
        this.licenseDeviceKey = licenseDeviceKey;
    }

    // 断开连接
    private void closeWithError(String step, int ret) {
        AntsLog.D(TAG + "closeWithError, step:" + step + ", ret:" + ret+", p2pid:"+p2pid);
        disconnect();
        sendErrorState(step, ret);
    }

    private int handleTNPResult(String step, int ret){
        int isGood = 1;
        if(ret < 0){
            if (ret == PPPP_APIs.ERROR_PPPP_TIME_OUT || ret == PPPP_APIs.ERROR_PPPP_SESSION_CLOSED_CALLED) {
                // do nothing
            }else{
                closeWithError(step, ret);
                isGood = -1;
            }

            TnpStatistic.onErrorXiaoyiEvent(uid, p2pid, ret);
            TnpStatistic.onErrorUmengEvent(ret);
        }

        return isGood;
    }

    private synchronized void handleVideoFrame(byte[] data, int size, byte type, boolean isRealtime){

        if(type == TNPHead.IO_TYPE_VIDEO){
            AVFrame avFrame = new AVFrame(data, size, isByteOrderBig, false);

            if (isEncrypted && avFrame.isIFrame()) {
                AntsUtil.decryptIframe(avFrame, decryptKey);
            }

            // for log print purpose
            int cameraUseCount = -1;
            if(mCameraListener != null && mCameraListener instanceof AntsCamera){
                cameraUseCount = ((AntsCamera) mCameraListener).getUseCount();
            }

            AntsLog.d(TAG, "tnp receive video "
                    + avFrame.toFrameString()
                    + ", useCount:" + avFrame.useCount + "-cameraUseCount:" + cameraUseCount
                    + (avFrame.isIFrame() ? (", isEncrypted:" + isEncrypted + "-" + decryptKey) : ""));

            if(avFrame.useCount < cameraUseCount){
                return;
            }

            if(mConnectStatus != 2){
                mConnectStatus = 2;
                if (mCameraListener != null) {
                    mCameraListener.receiveConnectingProgress(AntsCamera.CONNECTION_STATE_START_AV_CLIENT_SUCCESS);
                }
            }

            if(mCameraListener != null){

                if(avFrame.isIFrame()){
                    mLastReceiveAVFrame = avFrame;
                    mCameraListener.receiveVideoFrameData(avFrame);
                }else{
                    if(mLastReceiveAVFrame != null
                            && mLastReceiveAVFrame.getFrmNo()+1 == avFrame.getFrmNo()){
                        mLastReceiveAVFrame = avFrame;
                        if(mCameraListener != null) {
                            mCameraListener.receiveVideoFrameData(avFrame);
                        }
                    }else{
                        if(mAVFrameQueue.size() >= QUEUE_AV_FRAME_MAX_SIZE){
                            mAVFrameQueue.poll();
                        }
                        mAVFrameQueue.add(avFrame);
                    }
                }

                if(mLastReceiveAVFrame != null){

                    while(!mAVFrameQueue.isEmpty()){

                        AVFrame queueAVFrame = mAVFrameQueue.peek();

                        if(queueAVFrame.getFrmNo() <= mLastReceiveAVFrame.getFrmNo()){
                            mAVFrameQueue.poll();
                        }else if(queueAVFrame.getFrmNo() == mLastReceiveAVFrame.getFrmNo()+1){
                            mAVFrameQueue.poll();
                            mLastReceiveAVFrame = queueAVFrame;
                            if(mCameraListener != null) {
                                mCameraListener.receiveVideoFrameData(queueAVFrame);
                            }
                        }else{
                            break;
                        }
                    }

                }

            }

        }else{
            AntsLog.d(TAG, "receive not video type data");
        }

    }

    private class RecordData {
        TNPFrameHead info;
        byte[] data;
        int length;
        byte type;
    }


    private abstract class ThreadRecv extends Thread {
        protected static final int MAX_VIDEO_IFRAME_BUFFER_SIZE = 1024 * 1024;
        protected static final int MAX_VIDEO_PFRAME_BUFFER_SIZE = 100 * 1024;
        protected static final int MAX_AUDIO_BUFFER_SIZE = 5 * 1024;
        protected static final int MAX_COMMAND_BUFFER_SIZE = 100 * 1024;

        private volatile boolean bIsRunning = false;
        byte mThreadChannel;
        String mThreadName;

        int nMaxBufferSize = MAX_VIDEO_IFRAME_BUFFER_SIZE;
        private byte[] nBuffer;
        private int[] nSize = new int[1];
        private byte nIOType = 0;
        private int nRet = 0;
        TNPHead nTNPHead;
        private boolean isTNPHeadChecked = false;


        public ThreadRecv(byte channelNum){
            this.mThreadChannel = channelNum;
            this.mThreadName = getClass().getSimpleName();

            nMaxBufferSize = MAX_VIDEO_IFRAME_BUFFER_SIZE;
            if(channelNum == CHANNEL_COMMAND){
                nMaxBufferSize = MAX_COMMAND_BUFFER_SIZE;
            }else if(channelNum == CHANNEL_AUDIO){
                nMaxBufferSize = MAX_AUDIO_BUFFER_SIZE;
            }else if(channelNum == CHANNEL_VIDEO_RECORD_PFRAME || channelNum == CHANNEL_VIDEO_REALTIME_PFRAME){
                nMaxBufferSize = MAX_VIDEO_PFRAME_BUFFER_SIZE;
            }else if(channelNum == CHANNEL_VIDEO_RECORD_IFRAME || channelNum == CHANNEL_VIDEO_REALTIME_IFRAME){
                nMaxBufferSize = MAX_VIDEO_IFRAME_BUFFER_SIZE;
            }
            nBuffer = new byte[nMaxBufferSize];
        }

        public boolean isRunning(){
            return bIsRunning;
        }

        public void stopThread(){
            this.bIsRunning = false;
            this.interrupt();
        }

        @Override
        public void run() {
            AntsLog.d(TAG, mThreadName + " start");
            bIsRunning = true;
            while(bIsRunning){

                // 等待session建立成功
                if (mHandleSession < 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }

                nSize[0] = TNPHead.LEN_HEAD;
                nRet = PPPP_APIs.PPPP_Read(mHandleSession, mThreadChannel, nBuffer, nSize, 0xFFFFFFFF);

                if(handleTNPResult(Step.PPPP_Read, nRet) < 0){
                    break;
                }

                if(nSize[0] > 0){
                    nTNPHead = TNPHead.parse(nBuffer, isByteOrderBig);

                    if(!isTNPHeadChecked){
                        isTNPHeadChecked = true;

                        if(nTNPHead.version != tnpHeaderVersion){
                            if (nTNPHead.version == TNPHead.VERSION_ONE) {
                                AntsLog.d(TAG, "tnp head version should be:" + TNPHead.VERSION_ONE + ", need re-connect.");
                                tnpHeaderVersion = TNPHead.VERSION_ONE;
                                sendErrorState(Step.PPPP_Manual, ERROR_PPPP_OTHER_MANUAL_RETRY);
                                break;
                            } else if(nTNPHead.version >= TNPHead.VERSION_TWO) {
                                if(tnpHeaderVersion == TNPHead.VERSION_ONE){
                                    AntsLog.d(TAG, "tnp head version should be:" + TNPHead.VERSION_TWO + ", need re-connect.");
                                    tnpHeaderVersion = TNPHead.VERSION_TWO;
                                    sendErrorState(Step.PPPP_Manual, ERROR_PPPP_OTHER_MANUAL_RETRY);
                                    break;
                                }
                            }
                        }

                    }

                    if(nTNPHead.dataSize <= 0 || nTNPHead.dataSize > nMaxBufferSize){
                        AntsLog.d(TAG, "tnp read head is dirty data, need re-connect.");
                        sendErrorState(Step.PPPP_Manual, ERROR_PPPP_OTHER_MANUAL_RETRY);
                        break;
                    }

                    nIOType = nTNPHead.ioType;
                    nSize[0] = nTNPHead.dataSize;
                    nRet = PPPP_APIs.PPPP_Read(mHandleSession, mThreadChannel, nBuffer, nSize, 0xFFFFFFFF);

                    if(nRet < 0
                            || nTNPHead.dataSize > nMaxBufferSize
                            || nTNPHead.dataSize != nSize[0]
                            || !isIOTypeCorrect(nTNPHead.ioType)){

                        TnpStatistic.onReadErrorXiaoyiEvent(uid, p2pid, nRet,
                                mThreadChannel, nTNPHead.version, nTNPHead.ioType,
                                nTNPHead.dataSize, nSize[0]);
                    }

                    if(handleTNPResult(Step.PPPP_Read, nRet) < 0){
                        break;
                    }

                    if(nSize[0] > 0){
                        if(nSize[0] > nMaxBufferSize){
                            AntsLog.d(TAG, mThreadName + " read size bigger than buffer"
                                    + ", read:" + nSize[0] + ", max:" + nMaxBufferSize);
                        }else{
                            handleData(nBuffer, nSize[0], nIOType);
                        }
                    }else{
                        AntsLog.d(TAG, mThreadName + " read fail size:" + nSize[0]);
                    }
                }
            }

            AntsLog.d(TAG, mThreadName + " stop");
            bIsRunning = false;
        }

        private boolean isIOTypeCorrect(byte ioType){
            if(ioType == TNPHead.IO_TYPE_COMMAND && CHANNEL_COMMAND == mThreadChannel){
                return true;
            }

            if(ioType == TNPHead.IO_TYPE_AUDIO && CHANNEL_AUDIO == mThreadChannel){
                return true;
            }

            if(ioType ==TNPHead.IO_TYPE_VIDEO
                    && (CHANNEL_VIDEO_REALTIME_PFRAME == mThreadChannel
                    || CHANNEL_VIDEO_REALTIME_IFRAME == mThreadChannel
                    || CHANNEL_VIDEO_RECORD_IFRAME == mThreadChannel
                    || CHANNEL_VIDEO_RECORD_PFRAME == mThreadChannel)){
                return true;
            }

            return false;
        }

        protected abstract void handleData(byte[] data, int size, byte type);

    }

    private class ThreadRecvAudio extends ThreadRecv{

        public ThreadRecvAudio(){
            super(CHANNEL_AUDIO);
        }

        @Override
        protected void handleData(byte[] data, int size, byte type) {

            AntsLog.d(TAG,"ThreadRecvAudio size="+size);

            if(type == TNPHead.IO_TYPE_AUDIO){
                AVFrame avFrame = new AVFrame(data, size, isByteOrderBig, true);

                int cameraUseCount = -1;
                if(mCameraListener != null && mCameraListener instanceof AntsCamera){
                    cameraUseCount = ((AntsCamera) mCameraListener).getUseCount();
                }

                AntsLog.d("frame", "tnp receive audio frame:"
                        + avFrame.getFrmNo() + "-" + avFrame.getTimeStamp()
                        + "-" + avFrame.getFrmSize() + "-" + avFrame.getFlags() + "-codecid:" + avFrame.getCodecId());

                if (mCameraListener != null) {
                    mCameraListener.receiveAudioFrameData(avFrame);
                }

            }else{
                AntsLog.d(TAG, mThreadName + " receive not audio type data");
            }

        }
    }

    private class ThreadRecvVideoRealTimeIFrame extends ThreadRecv{

        public ThreadRecvVideoRealTimeIFrame(){
            super(CHANNEL_VIDEO_REALTIME_IFRAME);
        }

        @Override
        protected void handleData(byte[] data, int size, byte type) {

            handleVideoFrame(data, size, type, true);
        }

    }

    private class ThreadRecvVideoRealTimePFrame extends ThreadRecv{

        public ThreadRecvVideoRealTimePFrame(){
            super(CHANNEL_VIDEO_REALTIME_PFRAME);
        }

        @Override
        protected void handleData(byte[] data, int size, byte type) {

            handleVideoFrame(data, size, type, true);
        }

    }

    private class ThreadRecvVideoRecordIFrame extends ThreadRecv{

        public ThreadRecvVideoRecordIFrame(){
            super(CHANNEL_VIDEO_RECORD_IFRAME);
        }

        @Override
        protected void handleData(byte[] data, int size, byte type) {

            handleVideoFrame(data, size, type, false);
        }

    }

    private class ThreadRecvVideoRecordPFrame extends ThreadRecv{

        public ThreadRecvVideoRecordPFrame(){
            super(CHANNEL_VIDEO_RECORD_PFRAME);
        }

        @Override
        protected void handleData(byte[] data, int size, byte type) {

            handleVideoFrame(data, size, type, false);
        }

    }

    private class ThreadRecvIOCtrl extends ThreadRecv{

        private TNPIOCtrlHead nTNPIOCtrlHead;

        public ThreadRecvIOCtrl(){
            super(CHANNEL_COMMAND);
        }

        @Override
        protected void handleData(byte[] buffer, int size, byte type) {

            if(type == TNPHead.IO_TYPE_COMMAND){

                nTNPIOCtrlHead = TNPIOCtrlHead.parse(buffer, isByteOrderBig);
                int dataSize = size-TNPIOCtrlHead.LEN_HEAD-nTNPIOCtrlHead.exHeaderSize;
                if(dataSize < 0){
                    AntsLog.d(TAG, mThreadName + " receive command type dataSize is negative:" + dataSize);
                    return;
                }

                byte[] data = new byte[dataSize];
                System.arraycopy(buffer, TNPIOCtrlHead.LEN_HEAD, data, 0, dataSize);

                AntsLog.d(TAG, "PPPP_Read IOCTRL"
                        + ", ioType:" + type
                        + ", bufferSize:" + size
                        + ", dataSize:" + dataSize
                        + ", head.cmdNum:" + nTNPIOCtrlHead.commandNumber
                        + ", head.extSize:" + nTNPIOCtrlHead.exHeaderSize
                        + ", head.dataSize:" + nTNPIOCtrlHead.dataSize
                        + ", head.auth:" + nTNPIOCtrlHead.authResult
                        + ", recv(" + mHandleSession
                        + ", 0x" + Integer.toHexString((0x0000FFFF & (int)nTNPIOCtrlHead.commandType))
                        + ", " + AntsUtil.getHex(data, size-TNPIOCtrlHead.LEN_HEAD) + ")");


                // 发送统计日志
                if(nTNPIOCtrlHead.authResult == 0){
                    TnpStatistic.onCommandXiaoyiEvent(uid, p2pid, false, Integer.toHexString((0x0000FFFF & (int)nTNPIOCtrlHead.commandType)), true, nTNPIOCtrlHead.authResult, nTNPIOCtrlHead.commandNumber);
                    TnpStatistic.onCommandUmengEvent("RecvOK");
                }else{
                    TnpStatistic.onCommandXiaoyiEvent(uid, p2pid, false, Integer.toHexString((0x0000FFFF & (int)nTNPIOCtrlHead.commandType)), false, nTNPIOCtrlHead.authResult, nTNPIOCtrlHead.commandNumber);

                    if(nTNPIOCtrlHead.authResult >= 1 && nTNPIOCtrlHead.authResult <= 3){
                        TnpStatistic.onCommandUmengEvent("RecvError:" + nTNPIOCtrlHead.authResult);
                    }else{
                        TnpStatistic.onCommandUmengEvent("RecvError:other");
                    }
                }

                if (nTNPIOCtrlHead.authResult == 0) {
                    if(nTNPIOCtrlHead.commandType == AVIOCTRLDEFs.IOTYPE_USER_TNP_IPCAM_KICK){
                        AVIOCTRLDEFs.SMsgAUIoctrlTNPIpcamKickResp resp
                                = AVIOCTRLDEFs.SMsgAUIoctrlTNPIpcamKickResp.parse(data, isByteOrderBig);

                        AntsLog.d(TAG, "kick off by device command, reason:" + resp.reason);
                        if(resp.reason == 1){
                            // 因视频Session数超限被设备踢下线
                            sendErrorState(Step.PPPP_Manual, ERROR_PPPP_DEVICE_KICK_MAX_SESSION);
                        }else{
                            // 被设备踢下线
                            sendErrorState(Step.PPPP_Manual, ERROR_PPPP_DEVICE_KICK);
                        }
                    }else{
                        if (mCameraListener != null) {
                            mCameraListener.receiveIOCtrlData(nTNPIOCtrlHead.commandType, data);
                        }
                    }
                } else if (nTNPIOCtrlHead.authResult == 1 || nTNPIOCtrlHead.authResult == 2) {
                    // 每个指令都会发密码进行验证，如果authResult返回是1表示密码验证错误
                    sendErrorState(Step.PPPP_Read, AVAPIs.AV_ER_WRONG_VIEWACCorPWD);
                }else{
                    // 收到的指令错误状态
                }


            }else{
                AntsLog.d(TAG, mThreadName + " receive not command type data");
            }

        }
    }


    private class ThreadSendIOCtrl extends Thread {
        private volatile boolean bIsRunning = false;

        public boolean isRunning(){
            return bIsRunning;
        }

        public void stopThread() {
            this.bIsRunning = false;
            this.interrupt();
        }

        private short cmdNum = 0;

        @Override
        public void run(){
            AntsLog.d(TAG, "ThreadSendIOCtrl start");
            bIsRunning = true;
            while(bIsRunning){

                // 等待session建立成功
                if (mHandleSession < 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }

                if(!mIOCtrlQueue.isEmpty()){
                    P2PMessage p2pMessage = mIOCtrlQueue.poll();
                    if (p2pMessage != null && p2pMessage.data != null) {

                        String mAccount = account, mPassword = password;
                        if (isEncrypted) {
                            mAccount = AntsUtil.genNonce(15);
                            mPassword = AntsUtil.getPassword(mAccount, password);
                        }

                        TNPIOCtrlHead mTNPIOCtrlHead = new TNPIOCtrlHead((short)p2pMessage.reqId, ++cmdNum,
                                (short)p2pMessage.data.length, mAccount, mPassword, -1, isByteOrderBig);

                        TNPHead mTNPHead = new TNPHead(tnpHeaderVersion, TNPHead.IO_TYPE_COMMAND,
                                TNPIOCtrlHead.LEN_HEAD + mTNPIOCtrlHead.exHeaderSize + p2pMessage.data.length, isByteOrderBig);


                        int nSize = TNPHead.LEN_HEAD + mTNPHead.dataSize;
                        byte[] data = new byte[nSize];

                        System.arraycopy(mTNPHead.toByteArray(), 0, data, 0, TNPHead.LEN_HEAD);
                        System.arraycopy(mTNPIOCtrlHead.toByteArray(), 0, data, TNPHead.LEN_HEAD, TNPIOCtrlHead.LEN_HEAD);
                        System.arraycopy(p2pMessage.data, 0, data, TNPHead.LEN_HEAD + TNPIOCtrlHead.LEN_HEAD + mTNPIOCtrlHead.exHeaderSize, p2pMessage.data.length);

                        int ret = PPPP_APIs.PPPP_Write(mHandleSession, CHANNEL_COMMAND, data, nSize);

                        AntsLog.d(TAG, "PPPP_Write IOCTRL, ret:"+ ret
                                + ", cmdNum:" + mTNPIOCtrlHead.commandNumber
                                + ", extSize:" + mTNPIOCtrlHead.exHeaderSize
                                + ", send(" + mHandleSession
                                + ", 0x" + Integer.toHexString(p2pMessage.reqId)
                                + ", " + AntsUtil.getHex(p2pMessage.data, p2pMessage.data.length) + ")");

                        TnpStatistic.onCommandXiaoyiEvent(uid, p2pid, true, Integer.toHexString(p2pMessage.reqId), ret >= 0, ret, mTNPIOCtrlHead.commandNumber);
                        TnpStatistic.onCommandUmengEvent(ret >= 0 ? "SendOK" : ("SendError:" + ret));

                        if (ret < 0) {
                            TnpStatistic.onErrorXiaoyiEvent(uid, p2pid, ret);
                            TnpStatistic.onErrorUmengEvent(ret);

                            if (mCameraListener != null) {
                                p2pMessage.error = ret;
                                mCameraListener.receiveSendP2PMessageError(p2pMessage);
                                sendErrorState(Step.PPPP_Write, ret);
                            }

                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                    }
                }

            }

            AntsLog.d(TAG, "ThreadSendIOCtrl stop");
            bIsRunning = false;

        }

    }

    private class ThreadSendAudio extends Thread {
        private volatile boolean bIsRunning = false;

        public boolean isRunning(){
            return bIsRunning;
        }

        public void stopThread() {
            this.bIsRunning = false;
            this.interrupt();
        }

        @Override
        public void run() {
            AntsLog.d(TAG, "ThreadSendAudio start");
            bIsRunning = true;

            while(bIsRunning){

                // 等待session建立成功
                if (mHandleSession < 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }

                // send audio
                if(mRecordAudioQueue.size() > 0){
                    RecordData recordData = null;
                    synchronized (mSendAudioLock) {
                        recordData = mRecordAudioQueue.poll();
                    }
                    if (recordData == null) continue;


                    //AntsLog.d("sendData", recordData.data[0] + "," + recordData.data[1]+","+recordData.data[2]+","+recordData.data[3]);

                    TNPHead mTNPHead = new TNPHead(tnpHeaderVersion, TNPHead.IO_TYPE_AUDIO, TNPFrameHead.LEN_HEAD + recordData.length, isByteOrderBig);
                    TNPFrameHead mTNPFrameHead = recordData.info;
                    int nSize = TNPHead.LEN_HEAD + TNPFrameHead.LEN_HEAD + recordData.length;
                    byte[] data = new byte[nSize];

                    System.arraycopy(mTNPHead.toByteArray(), 0, data, 0, TNPHead.LEN_HEAD);
                    System.arraycopy(mTNPFrameHead.toByteArray(), 0, data, TNPHead.LEN_HEAD, TNPFrameHead.LEN_HEAD);
                    System.arraycopy(recordData.data, 0, data, TNPHead.LEN_HEAD + TNPFrameHead.LEN_HEAD, recordData.length);

                    int ret = PPPP_APIs.PPPP_Write(mHandleSession, CHANNEL_AUDIO, data, nSize);

                    AntsLog.d(TAG, "PPPP_Write AVDATA, ret:" + ret
                            + ", recordSize:" + recordData.length
                            + ", time:" + mTNPFrameHead.timestamp
                            + ", info:" + AntsUtil.getHex(mTNPFrameHead.toByteArray(), TNPFrameHead.LEN_HEAD) + " , currentTime " + System.currentTimeMillis());


                    if(ret < 0){
                        TnpStatistic.onErrorXiaoyiEvent(uid, p2pid, ret);
                        TnpStatistic.onErrorUmengEvent(ret);
                    }

                } else {
                    synchronized (mSendAudioLock) {
                        try {
                            mSendAudioLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            AntsLog.d(TAG, "ThreadSendAudio stop");
            bIsRunning = false;
        }
    }


    private boolean m_bOnlyAliveThreadRecordAudio = false;

    private class ThreadRecordAudio extends Thread {

        volatile boolean m_bIsRunning = false;

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

        public ThreadRecordAudioG726() {
            super();
        }

        public void run() {
            if (m_bOnlyAliveThreadRecordAudio) {
                AntsLog.i(TAG, "=== got multi m_bOnlyAliveThreadRecordAudio="
                        + m_bOnlyAliveThreadRecordAudio);
                return;
            }

            AntsLog.d(TAG, "ThreadRecordAudioG726 run");

            m_bOnlyAliveThreadRecordAudio = true;

            this.m_bIsRunning = true;
            EncG726.g726_enc_state_create((byte) 0, (byte) 2);
            int bufferSize = 640;
            long[] encodePCMLength = new long[1];
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

            while (m_bIsRunning && mHandleSession >= 0) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                if (bufferRead <= 0) {
                    break;
                }
                int processLenth = AudioProcOut.Process(tempBuffer, bufferRead, outProcessBuffer, 2);
                byte[] encodePCM = new byte[320];
                if (bufferRead > 0) {
                    EncG726.g726_encode(outProcessBuffer, (long) processLenth, encodePCM, encodePCMLength);
                    AntsLog.d("record", "bufferRead=" + bufferRead + ", endcodePCMLength=" + encodePCMLength[0]);
                }

                if (encodePCM != null) {
                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    TNPFrameHead mTNPFrameHead = new TNPFrameHead((short)138, (byte)2, (byte)0, (byte)0, (int)lTimeStamp, isByteOrderBig);

                    RecordData data = new RecordData();
                    data.info = mTNPFrameHead;
                    data.data = encodePCM;
                    data.length = (int) encodePCMLength[0];
                    data.type = 1;

                    synchronized (mSendAudioLock) {
                        mRecordAudioQueue.add(data);
                        mSendAudioLock.notifyAll();
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

            AntsLog.d(TAG, "ThreadRecordAudioAAC run");

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

            while (this.m_bIsRunning && mHandleSession >= 0) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                if (bufferRead <= 0) {
                    break;
                }

                byte[] encodePCM = vo.Enc(tempBuffer);

                if (encodePCM != null) {
                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    TNPFrameHead mTNPFrameHead = new TNPFrameHead((short)138, (byte)2, (byte)0, (byte)0, (int)lTimeStamp, isByteOrderBig);

                    RecordData data = new RecordData();
                    data.info = mTNPFrameHead;
                    data.data = encodePCM;
                    data.length = encodePCM.length;
                    data.type = 1;

                    synchronized (mSendAudioLock) {
                        mRecordAudioQueue.add(data);
                        mSendAudioLock.notifyAll();
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

    private class ThreadRecordAudioAACAEC extends ThreadRecordAudio {

        private AudioRecord mAudioRecord;

        private int sampleRate = 16000;
        private int bitRate = 32000;

        private long lTimestampKey = 1;
        private ByteRingBuffer RingBuffer = null;

        private MobileAEC mobileAEC;

        public ThreadRecordAudioAACAEC(MobileAEC mobileAEC) {
            super();
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
            AntsLog.d(TAG, "分贝值:" + volume);
            return  volume;
        }

        public void run() {
            if (m_bOnlyAliveThreadRecordAudio) {
                AntsLog.i(TAG, "=== got multi m_bOnlyAliveThreadRecordAudio="
                        + m_bOnlyAliveThreadRecordAudio);
                return;
            }

            AntsLog.d(TAG, "ThreadRecordAudioAACAEC run");


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
            short delay ;
            //  byte[]  encodePCM = new byte[bufferSize / 2 ];
            //  G711Encoder g711Encoder = new G711Encoder();

            header[0] = 0;
            VoAACEncoder vo = new VoAACEncoder();
            vo.Init(sampleRate, bitRate, (short) 1, (short) 1);

            MobileNS mobileNS = new MobileNS();
            mobileNS.init(sampleRate);
            mobileNS.setPolicyMode(2);

            MobileVAD mobileVAD = new MobileVAD();
            mobileVAD.init();
            mobileVAD.setPolicyMode(1);

            MobileAGC  mobileAGC = new MobileAGC();

            /*if(Build.MODEL.contains("MI 5")){
                mobileAGC.init(sampleRate, 9, 1);
            }
            else if(Build.MODEL.contains("MI 4")){
                mobileAGC.init(sampleRate, 18, 1);
            }
            else {
                mobileAGC.init(sampleRate, 9, 1);
            }*/


            if(model.equals(P2PDevice.MODEL_V2)){
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_LOW);
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_LOW);
            }else if(model.equals(P2PDevice.MODEL_H19) || model.equals(P2PDevice.MODEL_H20)){
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_HIGH);
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_HIGH);
            }else{
                AudioUtil.RecordMobileAGCInt(mobileAGC, sampleRate,AudioUtil.CAMERA_MODULE_GAIN_DEFAULT);
                delay = AudioUtil.getDelay(AudioUtil.CAMERA_MODULE_GAIN_DEFAULT);
            }

            RingBuffer = new ByteRingBuffer(2* 1024 *1024);

            int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(min < bufferSize){
                min = bufferSize;
            }

            if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.SINGLE_MODE) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
            }else if(talkMode == AntsCamera.VOIP_MODE) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
            }
            mAudioRecord.startRecording();

            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                lTimestampKey = 1;
                return;
            }

            mobileAEC.mRecordFlag = true;
            while (this.m_bIsRunning && mHandleSession >= 0) {
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

                        mobileAEC.echoCancellation(tempBufferShortOneInstance, null, outMAecTempShortBuffer, (short) outMAecTempShortBuffer.length,
                                delay);
                        mobileAGC.Process(outMAecTempShortBuffer, 1, sampleRate, outTempAgcShortBuffer, 0, (short) 0);

                        mobileNS.NsProcess(outTempAgcShortBuffer, 1, outTempShortBuffer);

                        mobileVAD.VADProcess(outTempShortBuffer, outTempShortBuffer.length, sampleRate);
                        int isVoice = mobileVAD.VADProcess(outTempShortBuffer, outTempShortBuffer.length, sampleRate);

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

                //        AmplifyPCMData(aacPorcessData, aacPorcessData.length, (float) 0.6);

             /*     try{
                       fin.write(aacPorcessData, 0, AAC_Decode_Length);
                     }catch (Exception e){

                   } */

                byte[] encodePCM =  vo.Enc(aacPorcessData);
                byte[] sendData = new byte[encodePCM.length + header.length];
                System.arraycopy(header, 0, sendData, 0, header.length);
                System.arraycopy(encodePCM, 0, sendData, header.length,encodePCM.length);

                if (encodePCM != null) {
                    long lTimeStamp = AVIOCTRLDEFs.SFrameInfo.createAudioTimestamp(lTimestampKey);
                    lTimestampKey++;

                    TNPFrameHead mTNPFrameHead = new TNPFrameHead((short) 138, (byte) 2, (byte) 0, (byte) 0, (int) lTimeStamp, isByteOrderBig);

                    RecordData data = new RecordData();
                    data.info = mTNPFrameHead;

                    if(P2PDevice.MODEL_H19.equals(model)
                            || P2PDevice.MODEL_H20.equals(model)
                            || P2PDevice.MODEL_M20.equals(model)
                            || P2PDevice.MODEL_V2.equals(model)
                            || P2PDevice.MODEL_Y20.equals(model)
                            || P2PDevice.MODEL_Y10.equals(model)
                            || P2PDevice.MODEL_D11.equals(model)){
                        data.data = sendData;
                        data.length = sendData.length;
                    } else {
                        data.data = encodePCM;
                        data.length = encodePCM.length;
                    }
                    data.type = 1;

                    synchronized (mSendAudioLock) {
                        AntsLog.e(TAG, "nDecode mRecordAudioQueue " + System.currentTimeMillis());
                        mRecordAudioQueue.add(data);
                        mSendAudioLock.notifyAll();
                    }
                }

            }

            vo.Uninit();

            if (mAudioRecord != null) {
                try {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    //mobileAEC.close();
                    mobileNS.close();
                    mobileVAD.close();
                    mAudioRecord = null;
                } catch (Exception e) {
                }
            }

            lTimestampKey = 1;

            m_bOnlyAliveThreadRecordAudio = false;
        }

        public double AudioVolume(short[] buffer, int length){
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < length; i++) {
                v += buffer[i] * buffer[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double) length;
            double volume = 10 * Math.log10(mean);
            return  volume;
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

    private class ThreadNetworkCheckInfo extends Thread {

        @Override
        public void run() {
            AntsLog.d(TAG, "ThreadNetworkCheckInfo start");

            final int timeoutSeconds = 15;
            final int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int nRet = PPPP_APIs.PPPP_Probe(serverString, timeoutSeconds, buffer, bufferSize);

            int code = -1;
            byte[] info = "".getBytes();
            if (nRet > 0 && nRet <= bufferSize) {
                code = 1;
                info = new byte[nRet];
                System.arraycopy(buffer, 0, info, 0, nRet);
            }

            if (mCameraListener != null) {
                mCameraListener.receiveNetworkCheck(code, info);
            }

            mThreadNetworkCheckInfo = null;
            AntsLog.d(TAG, "ThreadNetworkCheckInfo stop");
        }

    }

    private class ThreadOnlineStatus extends Thread {
        @Override
        public void run() {
            AntsLog.d(TAG, "ThreadOnlineStatus start");

            final int timeoutSeconds = 2;
            int[] nLastOnlineTime = new int[1];


            /**
             *  PPPP_CheckDevOnline
             *
             *  @param TargetID     Device ID
             *  @param ServerString Server Info
             *  @param TimeOutSec   TimeOutSec
             *  @param LastLgnTime  Last Login Timestamp
             *
             *  @return 1:Online 0:Offline <0:Error
             */
            int nRet = PPPP_APIs.PPPP_CheckDevOnline(p2pid, serverString, timeoutSeconds, nLastOnlineTime);

            int code = -1;
            int onlineStatus = AntsCamera.DEVICE_ONLINE_STATUS_UNKNOWN;
            if (nRet == 0 || nRet == 1) {
                code = 1;
                if (nRet == 0) {
                    onlineStatus = AntsCamera.DEVICE_ONLINE_STATUS_OFFLINE;
                } else {
                    onlineStatus = AntsCamera.DEVICE_ONLINE_STATUS_ONLINE;
                }
            }

            AntsLog.d(TAG, "ThreadOnlineStatus"
                    + ", ret:" + nRet
                    + ", online:" + onlineStatus
                    + ", lastLoginTime:" + nLastOnlineTime[0]
                    + ", p2pid:" + p2pid
                    + ", serverString:" + serverString);

            byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlOnlineStatusResp.parseContent(onlineStatus, nLastOnlineTime[0], isByteOrderBig);

            if (mCameraListener != null) {
                mCameraListener.receiveOnlineStatus(code, data);
            }

            mThreadOnlineStatus = null;
            AntsLog.d(TAG, "ThreadOnlineStatus stop");
        }
    }


    private class RunnableConnect implements Runnable {
        @Override
        public void run() {
            AntsLog.d(TAG, "RunnableConnect start");

            // do connect
            long startTick = System.currentTimeMillis();
            long costTick;

            if (mCameraListener != null) {
                mCameraListener.receiveConnectingProgress(AntsCamera.CONNECTION_STATE_GET_SID);
            }

            /**
             if bEnableLanSearch = 0x7F —> Connect() is used to detect if Device is on-line.
             Return Value:
             ERROR_PPPP_SUCCESSFUL —> Device of DID is on line
             ERROR_PPPP_INVALID_PREFIX —> Invalid Prefix of DID
             ERROR_PPPP_INVALID_ID —> Invalid DID
             ERROR_PPPP_DEVICE_NOT_ONLINE —> Device is not On-line (Not Login in last 5 minute)
             ERROR_PPPP_TIME_OUT —> No Response from Server
             else Connect() Connect is used to connect Device.
             Bit 0 [LanSearch] , 0: Disable Lan search, 1: Enable Lan Search
             Bit 1~4 [P2P Try time]:
             0 (0b0000): 5 second (default)
             1 (0b0001): 1 second
             2 (0b0010): 2 second
             3 (0b0011): 3 second
             ….
             14 (0b1110): 14 second
             15 (0b1111): 0 second, No P2P trying
             Bit 5 [RelayOff], 0: Relay mode is allowed, 1: No Relay connection
             Bit 6 [ServerRelayOnly], 0: Device Relay is allowed, 1: Only Server relay (if Bit 5 = 1, this value is ignored)
             **/

            // 75, 94
            byte enableLanSearch = (byte) 1;
            byte p2pTryTime =      (byte) 5;
            byte relayOff =        (byte) 0;
            byte serverRelayOnly = (byte) 1;
            byte connectFlag = (byte) (enableLanSearch | (p2pTryTime << 1) | (relayOff << 5) | (serverRelayOnly << 6) );
//            mHandleSession = PPPP_APIs.PPPP_Connect(p2pid, connectFlag, 0);
            if (isFactoryTest) {
                AntsLog.d(TAG, "factory test enabled");
                mHandleSession = PPPP_APIs.PPPP_ConnectOnlyLanSearch(p2pid);
            } else {
                if (P2PDevice.MODEL_D11.equals(model)) {
                    mHandleSession = PPPP_APIs.PPPP_ConnectForDoolBell(p2pid, connectFlag, 0, serverString, licenseDeviceKey);
                } else {
                    mHandleSession = PPPP_APIs.PPPP_ConnectByServer(p2pid, connectFlag, 0, serverString, licenseDeviceKey);
                }
            }

            costTick = System.currentTimeMillis() - startTick;
            AntsLog.d(TAG, "PPPP_Connect, session:" + mHandleSession
                    + ", p2pid:" + p2pid
                    + ", flag:" + connectFlag
                    + ", time:" + costTick + "ms" );

            if(mHandleSession >= 0) {
                sessionEstablishedTimestamp = System.currentTimeMillis();
                st_PPPP_Session sInfo = new st_PPPP_Session();
                int ret = PPPP_APIs.PPPP_Check(mHandleSession, sInfo);
                if (ret == PPPP_APIs.ERROR_PPPP_SUCCESSFUL) {

                    String modeDes = AntsCamera.P2P_TYPE_P2P;
                    if(sInfo.getMode() == 1){
                        modeDes = AntsCamera.P2P_TYPE_RELAY;
                    }else if(sInfo.getMode() == 2){
                        modeDes = AntsCamera.P2P_TYPE_TCP;
                    }else if(sInfo.getMode() == 3){
                        modeDes = AntsCamera.P2P_TYPE_SDEV;
                    }

                    AntsLog.d(TAG, "--TNP Session Ready--"
                            + ", Mode:" + modeDes
                            + ", Socket:" + sInfo.getSkt()
                            + ", Remote Addr:" + sInfo.getRemoteIP() + ":" + sInfo.getRemotePort()
                            + ", My Lan Addr:" + sInfo.getMyLocalIP() + ":" + sInfo.getMyLocalPort()
                            + ", My Wan Addr:" + sInfo.getMyWanIP() + ":" + sInfo.getMyWanPort()
                            + ", Connection time:" + sInfo.getConnectTime()
                            + ", Connection P2P time:" + sInfo.getConnectTimeP2P()
                            + ", Connection Relay time:" + sInfo.getConnectTimeRelay()
                            + ", DID:" + sInfo.getDID()
                            + ", I am:" + ((sInfo.getCorD() == 0) ? "Client" : "Device"));


                    if (mCameraListener != null) {
                        mCameraListener.receiveConnectingProgress(AntsCamera.CONNECTION_STATE_CONNECT_BY_UID_SUCCESS);
                    }

                    TnpStatistic.onConnectXiaoyiEvent(uid, p2pid, true, mHandleSession, modeDes, sInfo.getConnectTime());
                    TnpStatistic.onConnectUmengEvent("OK:" + modeDes);
                    TnpStatistic.onConnectUmengTimeEvent(sInfo.getConnectTime());
                    TnpStatistic.onConnectModeUmengEvent(modeDes);
                    if(modeDes.equals(AntsCamera.P2P_TYPE_P2P)){
                        TnpStatistic.onConnectP2PUmengTimeEvent(sInfo.getConnectTimeP2P());
                    }else{
                        TnpStatistic.onConnectRelayUmengTimeEvent(sInfo.getConnectTimeRelay());
                    }

                } else {
                    closeWithError(Step.PPPP_Check, ret);

                    TnpStatistic.onConnectXiaoyiEvent(uid, p2pid, false, mHandleSession, "", costTick);
                    TnpStatistic.onConnectUmengEvent("Error:" + ret);
                    TnpStatistic.onConnectErrorUmengEvent(ret);
                }
            } else {
                sessionEstablishedTimestamp =-1;
                closeWithError(Step.PPPP_Connect, mHandleSession);

                TnpStatistic.onConnectXiaoyiEvent(uid, p2pid, false, mHandleSession, "", costTick);
                TnpStatistic.onConnectUmengEvent("Error:" + mHandleSession);
                TnpStatistic.onConnectErrorUmengEvent(mHandleSession);
                TnpStatistic.onErrorXiaoyiEvent(uid, p2pid, mHandleSession);
                TnpStatistic.onErrorUmengEvent(mHandleSession);
            }


            mConnectRunnable = null;

            AntsLog.d(TAG, "RunnableConnect stop");
        }
    }

    private class RunnableDisconnect implements Runnable {
        private volatile boolean isCanceled = false;

        private void cancelRunnable(){
            isCanceled = true;
        }

        @Override
        public void run() {
            AntsLog.d(TAG, "RunnableDisconnect start");

            if(!isCanceled){
                long startTick = System.currentTimeMillis();
                int closedSession = mHandleSession;

                AntsLog.d(TAG, "PPPP_Connect_Break:" + p2pid);
                PPPP_APIs.PPPP_Connect_Break(p2pid);

                int nRet = PPPP_APIs.ER_ANDROID_NULL;
                if(mHandleSession >= 0){
                    nRet=PPPP_APIs.PPPP_ForceClose(mHandleSession);
                    mHandleSession = -1;
                }
                AntsLog.d(TAG, "PPPP_Close ret:" + nRet + ", session:" + closedSession
                        + ", time:" + (System.currentTimeMillis() - startTick) + "ms" );

                synchronized (mConnectLock) {
                    mDisconnectRunnable = null;
                    if(mConnectRunnable == null && mDisconnectRunnable == null){
                        mThreadPoolConnect.shutdown();
                        mThreadPoolConnect = null;
                    }
                    System.gc();
                }
            }

            AntsLog.d(TAG, "RunnableDisconnect stop");
        }
    }


    private int lastReceiveErrorState = -1;
    private long lastReceiveErrorMilliseconds = -1;
    private synchronized void sendErrorState(String step, int state){

        long currentMilliSeconds = System.currentTimeMillis();
        if(mCameraListener != null
                && (lastReceiveErrorState != state || (currentMilliSeconds - lastReceiveErrorMilliseconds) > 500)){
            mCameraListener.receiveErrorState(step, state);
            lastReceiveErrorMilliseconds = currentMilliSeconds;
            lastReceiveErrorState = state;
        }

    }

}
