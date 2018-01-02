package com.xiaoyi.camera.sdk;

import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.Packet;
import com.tutk.IOTC.St_SInfoEx;
import com.tutk.IOTC.TutkCamera;
import com.xiaoyi.log.AntsLog;

/**
 * 播放控制类，提供开启播放,暂停，恢复，结束播放等功能， 通过设置回调setAntsCameraCallback，返回音视频数据
 * 
 * @author chenyc
 * 
 */
public class AntsCameraTutk extends AntsCamera {
    private final String TAG = "AntsCameraTutk";

    private TutkCamera mCamera;

    private int mChannelIndex;

    private boolean mIsPaused;

    private boolean mIsRegistered = false;

    // 是否需要重启Camera
    private boolean mShouldRestartCamera = false;

    /**
     * AntsCamera初始化
     * 
     */
    protected AntsCameraTutk(P2PDevice p2pDevice) {
        super(p2pDevice);
        mCamera = new TutkCamera(p2pDevice.p2pid, p2pDevice.account, p2pDevice.pwd, p2pDevice.isEncrypted, p2pDevice.model, isByteOrderBig());
        mCamera.registerIOTCListener(this);
    }

    /**
     * 连接摄像机
     */
    public void connect() {
        // setFirmwareVersion(null);
        AntsLog.d(TAG,"connect()");
        if (mCamera != null) {
            if (!mIsRegistered) {
                mCamera.registerIOTCListener(this);
                mIsRegistered = true;
            }

            if (mShouldRestartCamera) {
                mShouldRestartCamera = false;
                // mCamera.disconnect();
                openCamera(mCamera);
            } else {
                openCamera(mCamera);
            }

//            for (P2PMessage p2pMessage : mP2PMessages.values()) {
//                sendP2PMessage(p2pMessage);
//            }

        }
    }

    public void doStartPlayVideo() {
        mChannelIndex = 0;
        if (mCamera != null) {
            AntsLog.i(TAG, "ivSpeak-mAntsCamera-startPlay:" + mChannelIndex);
            // connect();
            mCamera.startShow();
            sendStartPlayCommand();
            // if (this.isListening) {
            // mCamera.startListening();
            // }
        }
        mCameraState = CAMERA_STATE_PLAY;
    }

    /**
     * 开启接收声音
     */
    protected void doStartListening() {
        mCamera.startListening();
    }

    /**
     * 关闭接收声音
     */
    protected void doStopListening() {
        mCamera.stopListening();
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        mCamera.stopShow();
        mIsPaused = true;
        mCameraState = CAMERA_STATE_PAUSED;
    }

    /**
     * 恢复播放
     */
    protected void doResumePlayVideo() {
        if (!mIsPaused) { return; }
        AntsLog.i(TAG, "ivSpeak-mAntsCamera-resume:" + mChannelIndex);
        sendStartPlayCommand();
        mCamera.startShow();
        // if (isListening) {
        // mCamera.startListening();
        // }
        mIsPaused = false;
        mCameraState = CAMERA_STATE_PLAY;
    }

    /**
     * 停止播放
     */
    protected void doStopPlay() {
        mCameraState = CAMERA_STATE_STOPED;
        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mIsRegistered = false;
            if (mCamera.isAvClientStartError()) {
                mCamera.stopChannel();
            }
            // CameraManager.closeCamera(mCamera);
        }
    }

    public void sendP2PMessage(P2PMessage p2pMessage) {
        if (p2pMessage.needWaitResponse) {
            mP2PMessages.put(p2pMessage.resId, p2pMessage);
        }
        if (mCamera != null) {
            mCamera.sendIOCtrl(p2pMessage);
        }
    }

    public void startSpeaking(int talkMode) {
        if(mCamera != null) {
            mCamera.startSpeaking(talkMode);
        }

    }

    public void stopSpeaking() {
        if (mCamera != null) {
            mCamera.stopSpeaking();
        }
    }

    public boolean isConnected() {
        if (mCamera != null) { return mCamera.isCameraOpen(); }
        return false;
    }

    public static interface DoSeekCallback {
        void onResult(boolean isSuccess);

        void onError(int errorCode);
    }

    /**
     * 跳转到历史视频
     */
    public void doSeekTo(long time) {
        if (mCamera != null) {
            AVIOCTRLDEFs.STimeDay currentPlaySTimeDay = new AVIOCTRLDEFs.STimeDay(time, isByteOrderBig());
            int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL;
            if (p2pDevice.isEncrypted) {
                reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL2;
            }
            addUseCount();
            byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(0,
                    AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, currentPlaySTimeDay.toByteArray(),
                    getUseCount(), isByteOrderBig());
            mCamera.sendIOCtrl(reqType, input);

        }
    }

    public void stopRecordPlay() {
        mCamera.sendStopRecordVideoCommand();
    }

    public void sendOnlyStopPlayCommand() {
        mCamera.sendStopRecordVideoCommand();
    }

    /**
     * 回到实时播放
     */
    protected void doGoLive() {
        if (mCamera != null) {
            int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START;
            if (p2pDevice.isEncrypted) {
                command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START2;
            }
            addUseCount();
            mCamera.sendIOCtrl(command, Packet.intToByteArray(getUseCount(), isByteOrderBig()));

        }
    }

    public void updatePassword(String password) {
        if (mCamera != null) {
            p2pDevice.pwd = password;
            mCamera.setPassword(password);
        }
    }

    @Override
    public void disconnect() {
        stopPlay();
        mCamera.disconnect();
    }

    @Override
    public int getSInfoMode() {
        return mCamera.getSInfoMode();
    }

    public void setEncrypted(boolean isEncrypted) {
        p2pDevice.isEncrypted = isEncrypted;
        if (mCamera != null) {
            mCamera.setEncrypted(isEncrypted);
        }
    }

    @Override
    public void doUnRegister() {
        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mIsRegistered = false;
            // CameraManager.closeCamera(mCamera);
        }
    }

    @Override
    public SessionInfo getSessionInfo() {
        SessionInfo info = new SessionInfo();
        St_SInfoEx stInfo = mCamera.getSessionInfo();
        info.type = getCameraType();
        info.typeDes = getCameraTypeDes();
        info.version = getTutkVersion();
        if (stInfo != null) {
            //tutk的mode 0 p2p 1 relay 2 lan
            info.mode = stInfo.Mode;
            if(info.mode == 0x0){
                info.modeDes = P2P_TYPE_P2P;
            }else if (info.mode == 0x1){
                info.modeDes = P2P_TYPE_RELAY;
            }else if (info.mode == 0x2){
                info.modeDes = P2P_TYPE_LAN;
            }else{
                info.modeDes = P2P_TYPE_UNKNOWN;
            }
            info.localNatType = stInfo.LocalNatType;
            info.remoteNatType = stInfo.RemoteNatType;
            info.relayType = stInfo.RelayType;
        }
        return info;
    }

    @Override
    protected int doReceiveErrorState(ErrorState errorState) {
        if (mCamera != null && mCamera.isInConnecting()) { return REASON_OTHER; }

        int reason = REASON_OTHER;

        switch (errorState.state) {
        case 0:
            reason = REASON_NOTHING_TO_DO;
            break;
        case IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE:
        case IOTCAPIs.IOTC_ER_DEVICE_OFFLINE:
            reason = REASON_OFFLINE;
            break;
        case IOTCAPIs.IOTC_ER_NETWORK_UNREACHABLE:
            reason = REASON_NETWORK_ERROR;
            break;
        case IOTCAPIs.IOTC_ER_DEVICE_EXCEED_MAX_SESSION:
            reason = REASON_CONNECT_MAX;
            break;
        case IOTCAPIs.IOTC_ER_FAIL_CONNECT_SEARCH:
        case IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE:
        case IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT:
        case IOTCAPIs.IOTC_ER_FAIL_SETUP_RELAY:
        case IOTCAPIs.IOTC_ER_DEVICE_NOT_LISTENING:
        case IOTCAPIs.IOTC_ER_INVALID_SID:
        case AVAPIs.AV_ER_INVALID_ARG:
        case AVAPIs.AV_ER_INVALID_SID:
        case AVAPIs.AV_ER_TIMEOUT:
        case AVAPIs.AV_ER_DATA_NOREADY:
        case AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE:
        case AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT:
        case AVAPIs.AV_ER_CLIENT_EXIT:
        case AVAPIs.AV_ER_SENDIOCTRL_EXIT:
            retryCount++;
            if (retryCount < MAX_RETRY) {
                reason = REASON_SHOULD_RETRY;
            } else {
                reason = REASON_OTHER;
                retryCount = 0;
            }
            break;
        default:
            break;
        }
        mShouldRestartCamera = true;
        return reason;
    }

    public void openCamera(TutkCamera myCamera) {

        // 已连接，不作处理
        if (myCamera.isCameraOpen()) { return; }

        if (!myCamera.isConnectAlive()) {
            if (!myCamera.isInConnecting()) {
                if (myCamera.getSID() != TutkCamera.DEFAULT_SID) {
                    myCamera.disconnect();
                }
                myCamera.connect(myCamera.getUID());
            }
        }

        if (!myCamera.isChannelConnected()) {
//            if (myCamera.isAvClientStartError()) {
//                myCamera.stopChannel();
//            }
            myCamera.start();
        }
    }

    @Override
    protected void getNetworkInfo(P2PMessage p2pMessage) {
        mNetworkCheckP2PMessage = p2pMessage;
        receiveNetworkCheck(-2, "".getBytes());
    }

    @Override
    protected void getOnlineStatus(P2PMessage p2pMessage) {
        mOnlineStatusQueue.add(p2pMessage);
        receiveOnlineStatus(-2, AVIOCTRLDEFs.SMsgAVIoctrlOnlineStatusResp.parseContent(AntsCamera.DEVICE_ONLINE_STATUS_NOT_SUPPORT, -1, isByteOrderBig()));
    }

}
