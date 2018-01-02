package com.xiaoyi.camera.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.p2p.pppp_api.PPPP_APIs;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.IOTCAPIs;
import com.xiaoyi.log.AntsLog;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import glnk.client.GlnkClient;
import glnk.io.GlnkCode.ResponseCode;

/**
 * 播放控制类，提供开启播放,暂停，恢复，结束播放等功能， 通过设置回调setAntsCameraCallback，返回音视频数据
 *
 * @author chenyc
 */
public abstract class AntsCamera implements IRegisterCameraListener {

    private static final String TAG = "AntsCamera";

    public static final int CONNECTION_STATE_GET_SID = 5;
    public static final int CONNECTION_STATE_CONNECT_BY_UID_SUCCESS = 25;
    public static final int CONNECTION_STATE_START_AV_CLIENT_FAIL = 50;
    public static final int CONNECTION_STATE_START_AV_CLIENT_SUCCESS = 100;

    public static final String P2P_TYPE_UNKNOWN = "Unknown";
    public static final String P2P_TYPE_P2P = "P2P";
    public static final String P2P_TYPE_RELAY = "Relay";
    public static final String P2P_TYPE_LAN = "LAN";
    public static final String P2P_TYPE_TCP = "TCP";
    public static final String P2P_TYPE_SDEV = "SDEV";


    public final static int MIC_MODE = 1;       //双工 免提模式
    public final static int VOIP_MODE = 2;      //双工 电话模式
    public final static int SINGLE_MODE = 0;    //单工

    // 设备已不是该用户的了
    public static final int REASON_NOT_YOUR_DEVICE = -997;

    // 密码错误
    public static final int REASON_PASSWORD_ERROR = -998;

    // 不用处理的错误
    public static final int REASON_NOTHING_TO_DO = -999;

    // 其他未区分的错误
    public static final int REASON_OTHER = -1000;

    // 设备不在线
    public static final int REASON_OFFLINE = -1001;

    // 需要重试的错误
    public static final int REASON_SHOULD_RETRY = -1002;

    // 手机端网络错误
    public static final int REASON_NETWORK_ERROR = -1003;

    // 摄像机账号被禁掉了
    public static final int REASON_FORBID_BY_SERVER = -1004;

    // 连接超时
    public static final int REASON_CONNECT_TIMEOUT = -1005;

    // 达到最大连接数
    public static final int REASON_CONNECT_MAX = -1006;

    //达到app所允许连接的最大TNPSession数限制
    public static final int REASON_TNP_MAX_SESSION = -1007;

    // 最大连续重连次数
    public static final int MAX_RETRY = 3;

    // 最大密码重试次数
    public static final int MAX_PASSWORD_RETRY = 5;

    protected int passwordRetryCount = 0;

    private final static int RECEIVE_SPEAK_DISABLE_MESSAGE = -99;
    private final static int RECEIVE_SPEAK_ENABLE_MESSAGE = -100;
    private final static int RECEIVE_VIDEO_FRAME_DATA = -101;
    private final static int RECEIVE_AUDIO_FRAME_DATA = -102;
    private final static int RECEIVE_ERROR_STATE = -103;
    private final static int RECEIVE_CONNECTING_PROGRESS = -104;
    private final static int RECEIVE_SENDIO_ERROR = -106;
    private final static int RECEIVE_CONNECTION_SUCCESS = -107;
    private final static int RECEIVE_P2P_COMMAND_MESSAGE = -108;
    private final static int RECEIVE_P2P_NETWORK_CHECK_MESSAGE = -109;
    private final static int RECEIVE_P2P_ONLINE_STATUS_MESSAGE = -110;


    public static final int DEVICE_ONLINE_STATUS_NOT_SUPPORT = -2;
    public static final int DEVICE_ONLINE_STATUS_UNKNOWN     = -1;
    public static final int DEVICE_ONLINE_STATUS_OFFLINE     = 0;
    public static final int DEVICE_ONLINE_STATUS_ONLINE      = 1;



    protected final int CAMERA_STATE_INIT   = 0;
    protected final int CAMERA_STATE_PLAY   = 1;
    protected final int CAMERA_STATE_PAUSED = 2;
    protected final int CAMERA_STATE_STOPED = 3;

    protected int mCameraState = CAMERA_STATE_INIT;

    protected int retryCount = 0;

    public static PasswordInvalidProcesser passwordInvalidProcesser;

    public static void registerPasswordErrorHandler(
            PasswordInvalidProcesser passwordInvalidProcesser) {
        AntsCamera.passwordInvalidProcesser = passwordInvalidProcesser;
    }

    private CameraCommandHelper commandHelper;

    private int mConnectingProgress = 0;
    protected boolean mIsRecordingEnabled = false;

    private boolean isNewUseCount = false;
    protected byte useCount = 0;

    protected Map<Integer, P2PMessage> mP2PMessages = new LinkedHashMap<Integer, P2PMessage>();
    protected Queue<P2PMessage> mOnlineStatusQueue = new ConcurrentLinkedQueue<P2PMessage>();
    protected P2PMessage mNetworkCheckP2PMessage = null;

    private AntsCameraListener antsCameraListener;

    private AVFrame mLastAvFrame;

    private long mLastAVFrameTimeStamp;

    private boolean enableListening;

    private CameraInfo cameraInfo;

    protected P2PDevice p2pDevice;

    private boolean isLiveVideo = true;

    protected int resolutionType = 0;


    protected AntsCamera(P2PDevice p2pDevice) {
        this.p2pDevice = p2pDevice;
    }

    /**
     * 设置设备的信息
     *
     * @return
     */
    public CameraInfo getCameraInfo() {
        if (cameraInfo == null) {
            cameraInfo = new CameraInfo();
        }
        return cameraInfo;
    }

    /**
     * 恢复播放
     */
    protected abstract void doResumePlayVideo();

    /**
     * 停止播放
     */
    protected abstract void doStopPlay();

    /**
     * 断开连接
     */
    public abstract void disconnect();

    /**
     * 录音是否可用
     *
     * @return
     */
    public boolean isRecordingEnabled() {
        return mIsRecordingEnabled;
    }

    /**
     * 打开对讲
     *
     */
    public void startSpeaking(){
        startSpeaking(AntsCamera.SINGLE_MODE);
    }

    /**
     * 打开对讲
     *
     */
    public abstract void startSpeaking(int talkMode);

    /**
     * 关闭对讲
     */
    public abstract void stopSpeaking();


    /**
     * 获取UID
     *
     * @return
     */
    public String getUID() {
        return p2pDevice.uid;
    }

    public String getP2PID() {
        return p2pDevice.p2pid;
    }

    public String getDeviceModel(){
        return p2pDevice.model;
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        return p2pDevice.pwd;
    }

    public String getTnpServerString() {
        return p2pDevice.tnpServerString;
    }

    public String getTnpLicenseDeviceKey(){
        return p2pDevice.tnpLicenseDeviceKey;
    }


    /**
     * 检查密码是否与上一次一致
     *
     * @param strNewPw
     * @return
     */
    public boolean isSamePasswrod(String strNewPw) {
        return ((p2pDevice.pwd != null) && (strNewPw != null) && (p2pDevice.pwd.compareTo(strNewPw) == 0));
    }

    /**
     * 是否已连接
     *
     * @return
     */
    public abstract boolean isConnected();

    /**
     * 跳转到历史视频
     */
    protected abstract void doSeekTo(long time);

    /**
     * 跳转到历史视频
     */
    public void seekTo(long time) {
        mLastAVFrameTimeStamp = time;
        isLiveVideo = false;
        doSeekTo(time);
    }

    /**
     * 回到实时播放
     */
    protected abstract void doGoLive();

    /**
     * 回到实时播放
     */
    public void goLive() {
        mLastAVFrameTimeStamp = 0;
        isLiveVideo = true;
        doGoLive();
    }

    /**
     * 更新密码
     *
     * @param password
     */
    public abstract void updatePassword(String password);

    public void updateTnpConnectInfo(String tnpServerString, String tnpLicenseDeviceKey){
        p2pDevice.tnpServerString = tnpServerString;
        p2pDevice.tnpLicenseDeviceKey = tnpLicenseDeviceKey;
    }

    /**
     * 获取连接进度
     *
     * @return
     */
    public int getConnectingProgress() {
        return mConnectingProgress;
    }

    /**
     * 发送P2P指令
     *
     * @param p2pMessage
     */
    public abstract void sendP2PMessage(P2PMessage p2pMessage);

    /**
     * 发送停止历史视频指令
     */
    public abstract void stopRecordPlay();

    /**
     * 仅TUTK有效
     *
     * @return
     */
    public abstract int getSInfoMode();

    protected abstract void doUnRegister();

    protected abstract int doReceiveErrorState(ErrorState errorState);

    public void setDevice2UtcOffsetHour(int device2UtcOffsetHour){
        p2pDevice.device2UtcOffsetHour = device2UtcOffsetHour;
    }

    /**
     * 设置开始播放时间
     *
     * @param time time = 0 实时 time <> 0 历史
     */
    public void setStartPlayTime(long time) {
        mLastAVFrameTimeStamp = time;
        isLiveVideo = mLastAVFrameTimeStamp == 0;
    }

    public void setResolutionType(int type){
        resolutionType = type;
    }


    public void setEnableListening(boolean enableListening) {
        this.enableListening = enableListening;
    }

    public void startPlay() {
        doStartPlayVideo();
        if (enableListening) {
            startListening();
        }
    }

    /**
     * 开始视频播放指令
     */
    protected void sendStartPlayCommand() {
        if (isLiveVideo) {
            goLive();
        } else {
            if (p2pDevice.type == P2PDevice.TYPE_LANGTAO) {
                doGoLive();
            }
            seekTo(mLastAVFrameTimeStamp);
        }
    }

    /**
     * 结束视频播放
     */
    public void stopPlay() {
        doStopPlay();
    }

    /**
     * 检查是否还可以重试自动重连
     *
     * @return
     */
    public boolean canRetryConnect() {
        passwordRetryCount++;
        if (passwordRetryCount < MAX_PASSWORD_RETRY) {
            return true;
        }
        return false;
    }


    public int getPasswordRetryCount() {
        return passwordRetryCount;
    }

    /**
     * 获取读取设备信息的帮助类
     *
     * @return
     */
    public CameraCommandHelper getCommandHelper() {
        if (commandHelper == null) {
            commandHelper = new CameraCommandHelper(this);
        }
        return commandHelper;
    }

    public SessionInfo getSessionInfo() {
        SessionInfo info = new SessionInfo();
        return info;
    }

    /**
     * 重置参数
     */
    public void reset() {
        mLastAvFrame = null;
        mLastAVFrameTimeStamp = 0;
        isLiveVideo = true;
        cameraInfo = null;
    }

    public byte getUseCount() {
        return useCount;
    }

    public void addUseCount() {
        isNewUseCount = true;
        useCount++;
        AntsLog.d(TAG, "addUseCount to:" + useCount);
    }

    /**
     * 摄像机类型
     */
    public int getCameraType() {
        return p2pDevice.type;
    }

    public String getCameraTypeDes(){
        return p2pDevice.getTypeDes();
    }

    /**
     * 连接摄像机
     */
    public abstract void connect();

    /**
     * 开始播放视频
     */
    protected abstract void doStartPlayVideo();

    /**
     * 开启接收声音
     */
    protected abstract void doStartListening();

    /**
     * 开启接收声音
     */
    public void startListening() {
        enableListening = true;
        doStartListening();
    }

    /**
     * 关闭接收声音
     */
    protected abstract void doStopListening();

    /**
     * 开启接收声音
     */
    public void stopListening() {
        enableListening = false;
        doStopListening();
    }

    /**
     * 暂停播放
     */
    public abstract void pausePlay();

    /**
     * 恢复播放
     */
    public void resumePlay() {
        doResumePlayVideo();
        if (enableListening) {
            startListening();
        }
    }

    /**
     * 获取网络信息
     * */
    protected abstract void getNetworkInfo(P2PMessage p2pMessage);

    /**
     * 获取设备在线离线状态
     * */
    protected abstract void getOnlineStatus(P2PMessage p2pMessage);


    /**
     * 设置回调类
     *
     * @param antsCameraListener 回调类
     * @see AntsCameraListener
     */
    public void setAntsCameraListener(AntsCameraListener antsCameraListener) {
        mCameraState = CAMERA_STATE_INIT;
        this.antsCameraListener = antsCameraListener;
    }

    public boolean removeAntsCameraListener(AntsCameraListener antsCameraListener) {
        if (this.antsCameraListener == antsCameraListener) {
            this.antsCameraListener = null;
            mP2PMessages.clear();
            return true;
        }
        return false;
    }

    //只有插件调用
    public void release(){
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        P2PMessage p2pMessage = null;
        P2PCommand p2pCommand = null;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVE_CONNECTION_SUCCESS:
                    retryCount = 0;
                    if (antsCameraListener != null) {
                        antsCameraListener.receiveSpeakEnableInfo(true);
                        antsCameraListener.receiveConnectingProgress(CONNECTION_STATE_START_AV_CLIENT_SUCCESS);
                        antsCameraListener.receiveConnectSuccess();
                    }
                    break;
                case RECEIVE_SPEAK_ENABLE_MESSAGE:
                    mIsRecordingEnabled = true;
                    if (antsCameraListener != null) {
                        antsCameraListener.receiveSpeakEnableInfo(mIsRecordingEnabled);
                    }
                    break;
                case RECEIVE_SPEAK_DISABLE_MESSAGE:
                    mIsRecordingEnabled = false;
                    if (antsCameraListener != null) {
                        antsCameraListener.receiveSpeakEnableInfo(mIsRecordingEnabled);
                    }
                    break;

                case RECEIVE_VIDEO_FRAME_DATA:
                    if (antsCameraListener != null
                            && ((mCameraState == CAMERA_STATE_PLAY) || (mCameraState == CAMERA_STATE_INIT))) {
                        AVFrame avFrame = (AVFrame) msg.obj;
                        antsCameraListener.receiveVideoFrameData(avFrame);
                        if (mLastAvFrame == null
                                || mLastAvFrame.liveFlag != avFrame.liveFlag
                                || (avFrame.useCount == getUseCount() && isNewUseCount)
                                || mLastAvFrame.getVideoWidth() != avFrame.getVideoWidth()) {
                            AntsLog.d(TAG, "receiveVideoInfoChanged");
                            if (antsCameraListener != null) {
                                antsCameraListener.receiveVideoInfoChanged(avFrame);
                            }
                            isNewUseCount = false;
                            isLiveVideo = avFrame.liveFlag == 0;
                            passwordRetryCount = 0;
                        }
                        mLastAvFrame = avFrame;
                        mLastAVFrameTimeStamp = convertLocal2Utc2Device(avFrame.getTimeStamp() * 1000L);
                    }
                    break;

                case RECEIVE_AUDIO_FRAME_DATA:
                    if (antsCameraListener != null
                            && ((mCameraState == CAMERA_STATE_PLAY) || (mCameraState == CAMERA_STATE_INIT))) {
                        antsCameraListener.receiveAudioFrameData((AVFrame) msg.obj);
                    }
                    break;
                case RECEIVE_ERROR_STATE:
                    ErrorState errorState = (ErrorState) msg.obj;

                    // 处理密码错误
                    if (errorState.state == AVAPIs.AV_ER_WRONG_VIEWACCorPWD) {
                        if (AntsCamera.passwordInvalidProcesser != null) {
                            AntsCamera.passwordInvalidProcesser.onPasswordInvalid(AntsCamera.this);
                        }
                        return;
                    }

                    // 处理队列中未发送的指令
                    for (P2PMessage p2pMessage : mP2PMessages.values()) {
                        if (p2pMessage.needWaitResponse && p2pMessage.resp != null) {
                            p2pMessage.resp.onError(errorState.state);
                        }
                    }
                    mP2PMessages.clear();

                    if (antsCameraListener != null) {
                        int reason = doReceiveErrorState(errorState);
                        if (reason != REASON_NOTHING_TO_DO) {
                            AntsLog.d(TAG, "receiveErrorState, step=" + errorState.step
                                    + ", state=" + errorState.state + ", reason=" + reason);
                            antsCameraListener.receiveErrorState(errorState.step, errorState.state, reason);
                        }
                    }
                    break;

                case RECEIVE_CONNECTING_PROGRESS:
                    if (antsCameraListener != null) {
                        int progress = (Integer) msg.obj;
                        antsCameraListener.receiveConnectingProgress(progress);
                        if (progress == CONNECTION_STATE_START_AV_CLIENT_SUCCESS) {
                            antsCameraListener.receiveConnectSuccess();
                            retryCount = 0;
                        }
                    }

                    break;
                case RECEIVE_SENDIO_ERROR:
                    p2pMessage = (P2PMessage) msg.obj;
                    if (p2pMessage != null) {
                        AntsLog.d(TAG, "RECEIVE_SENDIO_ERROR " + p2pMessage.resId);
                        mP2PMessages.remove(p2pMessage.resId);
                        if (p2pMessage.resp != null) {
                            p2pMessage.resp.onError(p2pMessage.error);
                        }
                    }

                    if (antsCameraListener != null) {
                        antsCameraListener.receiveSendIOError(p2pMessage.reqId, p2pMessage.error);
                    }
                    break;
                case RECEIVE_P2P_COMMAND_MESSAGE:
                    p2pCommand = (P2PCommand) msg.obj;
                    p2pMessage = mP2PMessages.get(p2pCommand.type);
                    if (p2pMessage != null) {
                        AntsLog.d(TAG, "p2pMessage, reqId:" + p2pMessage.reqId
                                + ", respId:" + p2pMessage.resId
                                + ", data:" + (p2pMessage.data == null ? "null" : p2pMessage.data.length));
                        if (p2pMessage.resp == null) {
                            mP2PMessages.remove(p2pCommand.type);
                        } else {
                            if (p2pCommand.error >= 0) {
                                if (p2pMessage.resp.onResponse(p2pCommand.data)) {
                                    mP2PMessages.remove(p2pCommand.type);
                                }
                            } else {
                                p2pMessage.resp.onError(p2pCommand.error);
                                mP2PMessages.remove(p2pCommand.type);
                            }
                        }
                    }
                    break;
                case RECEIVE_P2P_NETWORK_CHECK_MESSAGE:
                    p2pCommand = (P2PCommand) msg.obj;
                    if(mNetworkCheckP2PMessage != null){
                        if(mNetworkCheckP2PMessage.resp != null){
                            if(p2pCommand.error >= 0){
                                mNetworkCheckP2PMessage.resp.onResponse(p2pCommand.data);
                            }else{
                                mNetworkCheckP2PMessage.resp.onError(p2pCommand.error);
                            }
                        }
                        mNetworkCheckP2PMessage = null;
                    }
                    break;
                case RECEIVE_P2P_ONLINE_STATUS_MESSAGE:
                    p2pCommand = (P2PCommand) msg.obj;
                    if(mOnlineStatusQueue.size() > 0){
                        P2PMessage mP2PMessage = mOnlineStatusQueue.poll();
                        while(mP2PMessage != null){
                            if(mP2PMessage.resp != null){
                                if(p2pCommand.error >= 0){
                                    mP2PMessage.resp.onResponse(p2pCommand.data);
                                }else{
                                    mP2PMessage.resp.onError(p2pCommand.error);
                                }
                            }
                            mP2PMessage = mOnlineStatusQueue.poll();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void receiveVideoFrameData(AVFrame avFrame) {
        Message msg = Message.obtain();
        msg.what = RECEIVE_VIDEO_FRAME_DATA;
        msg.obj = avFrame;
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveAudioFrameData(AVFrame avFrame) {
        Message msg = Message.obtain();
        msg.what = RECEIVE_AUDIO_FRAME_DATA;
        msg.obj = avFrame;
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveErrorState(String step, int state) {
        AntsLog.d(TAG, "receiveErrorState, step:" + step + ", state:" + state);
        Message msg = Message.obtain();
        msg.what = RECEIVE_ERROR_STATE;
        msg.obj = new ErrorState(step, state);
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveSessionInfo(String step, int state) {
        if (state < 0) {
            AntsLog.i(TAG, "receiveSessionInfo:" + state);
            Message msg = Message.obtain();
            msg.what = RECEIVE_ERROR_STATE;
            msg.obj = new ErrorState(step, state);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void receiveIOCtrlData(int type, byte[] data) {
        Message msg = Message.obtain();
        msg.what = RECEIVE_P2P_COMMAND_MESSAGE;
        msg.obj = new P2PCommand(type, 1, data);
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(int state) {
        if (state < 0) {
            AntsLog.i(TAG, "receiveChannelInfo:" + state);
            Message msg = Message.obtain();
            msg.what = RECEIVE_ERROR_STATE;
            msg.obj = new ErrorState(Step.avClientStart2, state);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void receiveSendP2PMessageError(P2PMessage p2pMessage) {
        Message msg = Message.obtain();
        msg.what = RECEIVE_SENDIO_ERROR;
        msg.obj = p2pMessage;
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveConnectingProgress(int progress) {
        mConnectingProgress = progress;
        Message msg = Message.obtain();
        msg.what = RECEIVE_CONNECTING_PROGRESS;
        msg.obj = (Integer) progress;
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveSpeakEnableInfo(boolean enable) {
        if (enable) {
            mHandler.sendEmptyMessage(RECEIVE_SPEAK_ENABLE_MESSAGE);
        } else {
            mHandler.sendEmptyMessage(RECEIVE_SPEAK_DISABLE_MESSAGE);
        }
    }

    @Override
    public void receiveConnectedSuccess() {
        Message msg = Message.obtain();
        msg.what = RECEIVE_CONNECTION_SUCCESS;
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveNetworkCheck(int code, byte[] info){
        Message msg = Message.obtain();
        msg.what = RECEIVE_P2P_NETWORK_CHECK_MESSAGE;
        msg.obj = new P2PCommand(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_NETWORK_CHECK_RESP, code, info);
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveOnlineStatus(int code, byte[] data) {
        Message msg = Message.obtain();
        msg.what = RECEIVE_P2P_ONLINE_STATUS_MESSAGE;
        msg.obj = new P2PCommand(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_ONLINE_STATUS_RESP, code, data);
        mHandler.sendMessage(msg);
    }


    public void receivePasswordError(int reason) {
        passwordRetryCount = 0;
        if (antsCameraListener != null) {
            int error = AVAPIs.AV_ER_WRONG_VIEWACCorPWD;
            if (p2pDevice.type == P2PDevice.TYPE_LANGTAO) {
                error = ResponseCode.USER_PWD_ERROR;
            }
            antsCameraListener.receivePasswordError(reason, error);
        }
    }

    public abstract void setEncrypted(boolean isEncrypted);

    protected class ErrorState {
        public String step;
        public int state;

        public ErrorState(String step, int state) {
            super();
            this.step = step;
            this.state = state;
        }

    }

    public class SessionInfo {

        public int type;  // 0:tutk; 1:langtao; 2:tnp;
        public String typeDes;  // TUTK, Langtao, TNP

        public byte mode;
        public String modeDes; // P2P, Relay, LAN, Unknown

        public byte localNatType;

        public byte remoteNatType;

        public byte relayType;

        public String	RemoteIP;

        public String version = "";

        public void setTutkVersion(int nVer) {
            version = String.format("%d.%d.%d.%d", (nVer >> 24) & 0xff, (nVer >> 16) & 0xff,
                    (nVer >> 8) & 0xff, nVer & 0xff);
        }

    }

    protected class P2PCommand{
        public int type;        // AVIOCTRLDEFs里面定义的所有P2P指令resType
        public int error;
        public byte[] data;

        public P2PCommand(int type, int error, byte[] data){
            this.type = type;
            this.error = error;
            this.data = data;
        }
    }


    public static String getTutkVersion() {
        int[] version = new int[1];
        IOTCAPIs.IOTC_Get_Version(version);
        int nVer = version[0];
        return String.format("%d.%d.%d.%d", (nVer >> 24) & 0xff, (nVer >> 16) & 0xff,
                (nVer >> 8) & 0xff, nVer & 0xff);

    }

    public static String getLangtaoVersion() {
        return GlnkClient.getGlnkCVersion();
    }

    public static String getTnpVersion() {
        return "0x" + Integer.toHexString(PPPP_APIs.PPPP_GetAPIVersion());
    }

    public boolean isByteOrderBig(){
        if(p2pDevice.type == P2PDevice.TYPE_LANGTAO) {
            return false;
        } else if(p2pDevice.type == P2PDevice.TYPE_TUTK) {
            return false;
        } else if(p2pDevice.type == P2PDevice.TYPE_TNP) {
            return true;
        }

        // default
        return false;
    }


    private long convertLocal2Utc2Device(long localTick){
        if(localTick <= 0){
            return 0;
        }

        // 1、初始化本地时间日历：
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
        cal.setTimeInMillis(localTick);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, 0 - (zoneOffset + dstOffset) + (p2pDevice.device2UtcOffsetHour * 3600 * 1000));

        long deviceTick = cal.getTimeInMillis();
        return deviceTick;
    }


    public AVFrame getLastAvFrame() {
        return mLastAvFrame;
    }
}
