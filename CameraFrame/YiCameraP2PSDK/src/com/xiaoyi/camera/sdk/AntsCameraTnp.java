package com.xiaoyi.camera.sdk;

import com.p2p.pppp_api.PPPP_APIs;
import com.tnp.TnpCamera;
import com.tnp.TnpStatistic;
import com.tnp.model.st_PPPP_Session;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;

/**
 * Created by Chuanlong on 2015/11/12.
 */
public class AntsCameraTnp extends AntsCamera {

    private TnpCamera mCamera;
    private boolean mIsPaused;
    private long lastSendPlayTimestamp = -1;

    protected AntsCameraTnp(P2PDevice p2pDevice) {
        super(p2pDevice);
        mCamera = new TnpCamera(p2pDevice.uid, p2pDevice.p2pid, p2pDevice.tnpServerString, p2pDevice.tnpLicenseDeviceKey, p2pDevice.account, p2pDevice.pwd, p2pDevice.model, p2pDevice.isEncrypted, isByteOrderBig(), p2pDevice.isFactoryTest);
    }


    @Override
    public void connect() {
        registerCameraListener();
        if (!mCamera.isConnected()) {
            this.mCamera.connect();
            this.mCamera.start();
        }
    }

    public void registerCameraListener() {
        this.mCamera.registerCameraListener(this);
    }

    /**
     * 获取Session成功建立时的时间戳
     *
     * @return
     */
    protected long getSessionEstablishedTimestamp() {
        return mCamera.getSessionEstablishedTimestamp();
    }

    @Override
    public void disconnect() {
        stopPlay();
        this.mCamera.disconnect();
        useCount = 0;
    }

    @Override
    protected void doStartPlayVideo() {
        sendStartPlayCommand();
        mCameraState = CAMERA_STATE_PLAY;
    }

    @Override
    protected void doResumePlayVideo() {
        if (!mIsPaused) { return; }

        sendStartPlayCommand();
        mIsPaused = false;
        mCameraState = CAMERA_STATE_PLAY;
    }


    @Override
    public void pausePlay() {
        mCamera.pause();
        mIsPaused = true;
        mCameraState = CAMERA_STATE_PAUSED;
    }

    @Override
    protected void doStopPlay() {
        if(mCameraState != CAMERA_STATE_PAUSED){
            pausePlay();
        }
        mCamera.unregisterCameraListener();
        mCameraState = CAMERA_STATE_STOPED;
    }

    @Override
    public void stopRecordPlay() {
        mCamera.sendStopRecordVideoCommand();
    }


    @Override
    protected void doStartListening() {
        mCamera.sendStartListeningCommand();
    }

    @Override
    protected void doStopListening() {
        mCamera.sendStopListeningCommand();
    }

    @Override
    public void startSpeaking(int talkMode) {
        mCamera.startSpeaking(talkMode);
    }

    @Override
    public void stopSpeaking() {
        mCamera.stopSpeaking();
    }

    @Override
    public boolean isConnected() {
        return mCamera.isConnected();
    }

//    @Override
//    protected void doSeekTo(long time) {
//        if (mCamera != null) {
//            AVIOCTRLDEFs.STimeDay currentPlaySTimeDay = new AVIOCTRLDEFs.STimeDay(time, isByteOrderBig());
//            int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL;
//            if (p2pDevice.isEncrypted) {
//                reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL2;
//            }
//            addUseCount();
//            byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(0,
//                    AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, currentPlaySTimeDay.toByteArray(),
//                    getUseCount(), isByteOrderBig());
//            mCamera.sendIOCtrl(reqType, input);
//
//            lastSendPlayTimestamp = System.currentTimeMillis();
//        }
//    }
//
//    @Override
//    protected void doGoLive() {
//        if (mCamera != null) {
//            int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START;
//            if (p2pDevice.isEncrypted) {
//                command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START2;
//            }
//            addUseCount();
//            mCamera.sendIOCtrl(command, Packet.intToByteArray(getUseCount(), isByteOrderBig()));
//
//            lastSendPlayTimestamp = System.currentTimeMillis();
//        }
//    }

    @Override
    protected void doSeekTo(long time) {
        if (mCamera != null) {
            int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_START_RECORD;
            addUseCount();
            byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlTnpPlayRecord.parseContent(getUseCount(), (byte)resolutionType, (byte) 1, time, isByteOrderBig());
            mCamera.sendIOCtrl(reqType, input);

            lastSendPlayTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    protected void doGoLive() {
        if (mCamera != null) {
            int command = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_START_REALTIME;
            addUseCount();
            byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlTnpPlay.parseContent(getUseCount(), (byte)resolutionType, (byte) 1);
            mCamera.sendIOCtrl(command, input);

            lastSendPlayTimestamp = System.currentTimeMillis();
        }
    }



    @Override
    public void updatePassword(String password) {
        p2pDevice.pwd = password;
        mCamera.setPassword(password);
    }

    @Override
    public void updateTnpConnectInfo(String tnpServerString, String tnpLicenseDeviceKey){
        super.updateTnpConnectInfo(tnpServerString, tnpLicenseDeviceKey);
        mCamera.updateTnpConnectInfo(tnpServerString, tnpLicenseDeviceKey);
    }

    @Override
    public void sendP2PMessage(P2PMessage p2pMessage) {
        if (p2pMessage.needWaitResponse) {
            mP2PMessages.put(p2pMessage.resId, p2pMessage);
        }
        if (mCamera != null) {
            mCamera.sendIOCtrl(p2pMessage);
        }
    }

    @Override
    public int getSInfoMode() {
        return 0;
    }

    @Override
    protected void doUnRegister() {
        this.mCamera.unregisterCameraListener();
    }

    @Override
    protected int doReceiveErrorState(ErrorState errorState) {
        int reason = REASON_OTHER;
        switch (errorState.state){
            case PPPP_APIs.ERROR_PPPP_SUCCESSFUL:
            case PPPP_APIs.ERROR_PPPP_ALREADY_INITIALIZED:
            case PPPP_APIs.ERROR_PPPP_SESSION_CLOSED_CALLED:
                reason = REASON_NOTHING_TO_DO;
                break;
            case PPPP_APIs.ERROR_PPPP_DEVICE_NOT_ONLINE:
                reason = REASON_OFFLINE;
                break;
            case PPPP_APIs.ERROR_PPPP_NOT_INITIALIZED:
            case PPPP_APIs.ERROR_PPPP_INVALID_ID:
            case PPPP_APIs.ERROR_PPPP_INVALID_PARAMETER:
            case PPPP_APIs.ERROR_PPPP_FAIL_TO_RESOLVE_NAME:
            case PPPP_APIs.ERROR_PPPP_INVALID_SERVER_STRING:
                reason = REASON_OTHER;
                break;
//            case PPPP_APIs.ERROR_PPPP_NO_RELAY_SERVER_AVAILABLE:
//                reason = REASON_NETWORK_ERROR;
//                break;
            case PPPP_APIs.ERROR_PPPP_TIME_OUT:
            case PPPP_APIs.ERROR_PPPP_INVALID_PREFIX:
            case PPPP_APIs.ERROR_PPPP_ID_OUT_OF_DATE:
            case PPPP_APIs.ERROR_PPPP_NO_RELAY_SERVER_AVAILABLE:
            case PPPP_APIs.ERROR_PPPP_INVALID_SESSION_HANDLE:
            case PPPP_APIs.ERROR_PPPP_SESSION_CLOSED_REMOTE:
            case PPPP_APIs.ERROR_PPPP_SESSION_CLOSED_TIMEOUT:
            case PPPP_APIs.ERROR_PPPP_REMOTE_SITE_BUFFER_FULL:
            case PPPP_APIs.ERROR_PPPP_USER_LISTEN_BREAK:
            case PPPP_APIs.ERROR_PPPP_UDP_PORT_BIND_FAILED:
            case PPPP_APIs.ERROR_PPPP_USER_CONNECT_BREAK:
            case PPPP_APIs.ERROR_PPPP_SESSION_CLOSED_INSUFFICIENT_MEMORY:
            case PPPP_APIs.ERROR_PPPP_SESSION_SOCKET_ERROR:
            case PPPP_APIs.ERROR_PPPP_SESSION_DATA_ERROR:
            case PPPP_APIs.ERROR_PPPP_NO_AVAILABLE_P2P_SERVER:
            case PPPP_APIs.ERROR_PPPP_TCP_CONNECT_ERROR:
            case PPPP_APIs.ERROR_PPPP_TCP_SOCKET_ERROR:
            case PPPP_APIs.ERROR_PPPP_TICKET_ERROR:
                retryCount++;
                if (retryCount < MAX_RETRY) {
                    reason = REASON_SHOULD_RETRY;
                } else {
                    reason = REASON_OTHER;
                    retryCount = 0;
                }
                break;
            case PPPP_APIs.ERROR_PPPP_MAX_SESSION:
                reason = REASON_TNP_MAX_SESSION;
                break;
            case PPPP_APIs.ERROR_PPPP_DEVICE_MAX_SESSION:
            case TnpCamera.ERROR_PPPP_DEVICE_KICK_MAX_SESSION:
                reason = REASON_CONNECT_MAX;
                break;
            case TnpCamera.ERROR_PPPP_OTHER_MANUAL_RETRY:
                reason = REASON_SHOULD_RETRY;
                break;
            case TnpCamera.ERROR_PPPP_DEVICE_KICK:
                reason = REASON_OTHER;
                break;
            default:
                reason = REASON_OTHER;
                break;
        }

        return reason;
    }

    @Override
    public void receiveVideoFrameData(AVFrame avFrame) {
        super.receiveVideoFrameData(avFrame);

        // send log
        if(lastSendPlayTimestamp != -1 && avFrame.useCount == getUseCount()){
            long costTime = System.currentTimeMillis() - lastSendPlayTimestamp;
            TnpStatistic.onVideoXiaoyiEvent(getUID(), getP2PID(), avFrame.liveFlag == 0, costTime);
            if(avFrame.liveFlag == 0){
                TnpStatistic.onRealTimeStartVideoUmengTimeEvent(costTime);
            }else{
                TnpStatistic.onRecordStartVideoUmengTimeEvent(costTime);
            }
            lastSendPlayTimestamp = -1;
        }
    }

    @Override
    public void setEncrypted(boolean isEncrypted) {
        p2pDevice.isEncrypted = isEncrypted;
        if (mCamera != null) {
            mCamera.setEncrypted(isEncrypted);
        }
    }

    @Override
    public SessionInfo getSessionInfo() {
        SessionInfo info = new SessionInfo();
        st_PPPP_Session mSession = mCamera.getTNPSession();
        info.type = getCameraType();
        info.typeDes = getCameraTypeDes();
        info.version = getTnpVersion();
        if(mSession != null) {
            info.mode = (byte) mSession.getMode();

            String modeDes = AntsCamera.P2P_TYPE_P2P;
            if(mSession.getMode() == 1){
                modeDes = AntsCamera.P2P_TYPE_RELAY;
            }else if(mSession.getMode() == 2){
                modeDes = AntsCamera.P2P_TYPE_TCP;
            }else if(mSession.getMode() == 3){
                modeDes = AntsCamera.P2P_TYPE_SDEV;
            }
            info.modeDes = modeDes;

            info.RemoteIP = mSession.getRemoteIP();
        }
        return info;
    }

    @Override
    protected void getNetworkInfo(P2PMessage p2pMessage) {
        // this method will return immediately and will async invoke receiveNetworkCheck after network check finish
        mNetworkCheckP2PMessage = p2pMessage;
        mCamera.getNetworkInfo();
    }

    @Override
    protected void getOnlineStatus(P2PMessage p2pMessage) {
        mOnlineStatusQueue.add(p2pMessage);
        mCamera.getOnlineStatus();
    }

}
