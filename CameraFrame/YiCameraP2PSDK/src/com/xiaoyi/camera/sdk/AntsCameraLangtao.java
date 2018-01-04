package com.xiaoyi.camera.sdk;

import com.langtao.LangtaoCamera;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Packet;

public class AntsCameraLangtao extends AntsCamera {

    private LangtaoCamera mCamera;

    private boolean mIsPaused;

    protected AntsCameraLangtao(P2PDevice p2pDevice) {
        super(p2pDevice);
        this.mCamera = new LangtaoCamera(p2pDevice.p2pid, p2pDevice.account, p2pDevice.pwd, p2pDevice.model, p2pDevice.isEncrypted, isByteOrderBig());
    }

    /**
     * 连接摄像机
     */
    @Override
    public void connect() {
        // setFirmwareVersion(null);
        this.mCamera.setListener(this);
        if (!this.mCamera.isConnected() && !this.mCamera.isInConnecting()) {
            this.mCamera.connect();
        }

//        for (P2PMessage p2pMessage : mP2PMessages.values()) {
//            sendP2PMessage(p2pMessage);
//        }
    }


    @Override
    public void disconnect() {
        stopPlay();
        mCamera.disconnect();
    }

    @Override
    public void doStartPlayVideo() {
//        mCamera.startLiveChannel();
//        mCamera.startSendIOCtrlThread();
        sendStartPlayCommand();
        mCameraState = CAMERA_STATE_PLAY;
    }

    @Override
    protected void doStartListening() {
        mCamera.sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, new byte[8]);
    }

    @Override
    public void doStopListening() {
        mCamera.sendIOCtrl(AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, new byte[8]);
    }

    @Override
    public void pausePlay() {
        mCamera.sendStopRecordVideoCommand();
        mCamera.sendStopPlayVideoCommand();
        mIsPaused = true;
        mCameraState = CAMERA_STATE_PAUSED;
    }

    @Override
    protected void doResumePlayVideo() {
        if (!mIsPaused) { return; }
        // 开启视频和音频
        sendStartPlayCommand();
        // if (isListening) {
        // startListening();
        // }

        mIsPaused = false;
        mCameraState = CAMERA_STATE_PLAY;
    }

    /**
     * 停止播放，关闭连接
     */
    @Override
    protected void doStopPlay() {
        mCameraState = CAMERA_STATE_STOPED;
        if (mCamera != null) {
            mCamera.setListener(null);
            mCamera.disconnect();
            // mCamera.stopLiveChannel();//必须在mCamera.setListener(null);之后，否则可能导致无响应的问题。
        }

    }

    @Override
    public boolean isRecordingEnabled() {
        return true;
    }

    @Override
    public void startSpeaking(int talkMode) {
        mCamera.startTalking();
    }

    @Override
    public void stopSpeaking() {
        mCamera.stopTalking();
    }

    @Override
    public boolean isConnected() {
        return mCamera.isConnected();
    }

    @Override
    public void doSeekTo(long time) {
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

    @Override
    public void doGoLive() {
        int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START;
        if (p2pDevice.isEncrypted) {
            command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START2;
        }
        addUseCount();
        mCamera.sendIOCtrl(command, Packet.intToByteArray(getUseCount(), isByteOrderBig()));
    }

    @Override
    public void updatePassword(String password) {
        if (mCamera != null) {
            mCamera.setPassword(password);
        }
    }

    @Override
    public int getConnectingProgress() {
        return mCamera.isConnected() ? 100 : 50;
    }

    public void sendP2PMessage(P2PMessage p2pMessage) {
        if (p2pMessage.needWaitResponse) {
            mP2PMessages.put(p2pMessage.resId, p2pMessage);
        }
        if (mCamera != null) {
            mCamera.sendIOCtrl(p2pMessage);
        }
    }

    @Override
    public void stopRecordPlay() {
        mCamera.sendStopRecordVideoCommand();
    }

    @Override
    public int getSInfoMode() {
        int nMode = mCamera.getMode();

        // 为了与TUTK的返回值相同
        if (nMode == 1) {// p2p
            return 2;
        }
        if (nMode == 2) {// relay
            return 1;
        }

        return nMode;
    }

    @Override
    public SessionInfo getSessionInfo() {
        SessionInfo info = new SessionInfo();
        info.type = getCameraType();
        info.typeDes = getCameraTypeDes();
        info.version = getLangtaoVersion();

        //浪涛的mode：1: p2p, 2: relay ip
        info.mode = (byte)mCamera.getMode();
        if(info.mode == 0x1){
            info.modeDes = P2P_TYPE_P2P;
        }else if (info.mode == 0x2){
            info.modeDes = P2P_TYPE_RELAY;
        }else{
            info.modeDes = P2P_TYPE_UNKNOWN;
        }
        return info;
    }

    public void setEncrypted(boolean isEncrypted) {
        p2pDevice.isEncrypted = isEncrypted;
        if (mCamera != null) {
            mCamera.setEncrypted(isEncrypted);
        }
    }

    @Override
    protected void doUnRegister() {
        if (mCamera != null) {
            mCamera.setListener(null);
        }
    }

    @Override
    protected int doReceiveErrorState(ErrorState errorState) {
        int reason = REASON_OTHER;

        if (Step.sendIOCtrlByManu.equals(errorState.step)) {
            reason = REASON_NOTHING_TO_DO;
        } else if (Step.onDisconnected.equals(errorState.step)) {
            switch (errorState.state) {
            // 设备离线
            // case -2:
            // reason = REASON_OFFLINE;
            // break;
            case -5400:
                reason = REASON_NETWORK_ERROR;
                break;
            case -2:
                reason = REASON_OFFLINE;
                break;
            case -9999:
            case 5001:
            case 5002:
            //case 5530:
            case 5540:
            case 5550:
            case 5110:
            case 6110:
            case 7110:
            case 0:
            case -20:
            case -10:
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
        }
        return reason;
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
